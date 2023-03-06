package net.tccn.zhub;

import com.google.gson.reflect.TypeToken;
import net.tccn.AbstractConsumer;
import net.tccn.Event;
import net.tccn.IConsumer;
import net.tccn.IProducer;
import net.tccn.timer.Timers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ZHubClient extends AbstractConsumer implements IConsumer, IProducer {

    public Logger logger = Logger.getLogger(ZHubClient.class.getSimpleName());
    private String addr = "127.0.0.1:1216";
    //private String password = "";
    private String groupid = "";
    private String auth = "";

    private OutputStream writer;
    private BufferedReader reader;

    private final LinkedBlockingQueue<Timer> timerQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Event<String>> topicQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Event<String>> rpcBackQueue = new LinkedBlockingQueue<>(); // RPC BACK MSG
    private final LinkedBlockingQueue<Event<String>> rpcCallQueue = new LinkedBlockingQueue<>(); // RPC CALL MSG
    private final LinkedBlockingQueue<String> sendMsgQueue = new LinkedBlockingQueue<>(); // SEND MSG

    private final BiConsumer<Runnable, Integer> threadBuilder = (r, n) -> {
        for (int i = 0; i < n; i++) {
            new Thread(() -> r.run()).start();
        }
    };

    /*private static boolean isFirst = true;
    private boolean isMain = false;*/
    private static final Map<String, ZHubClient> mainHub = new HashMap<>(); // 127.0.0.1:1216 - ZHubClient

    public ZHubClient(String addr, String groupid, String appname, String auth) {
        this.addr = addr;
        this.groupid = groupid;
        this.APP_NAME = appname;
        this.auth = auth;
        init(null);
    }

    public void init(Map<String, String> config) {
        if (!preInit()) {
            return;
        }

        // 自动注入
        if (config != null) {
            addr = config.getOrDefault("addr", addr);
            groupid = config.getOrDefault("groupid", groupid);
            APP_NAME = config.getOrDefault("appname", APP_NAME);
        }

        // 设置第一个启动的 实例为主实例
        /*if (isFirst) {
            isMain = true;
            isFirst = false;
        }*/
        if (!mainHub.containsKey(addr)) { // 确保同步执行此 init 逻辑
            mainHub.put(addr, this);
        }

        if (!initSocket(0)) {
            return;
        }
        // 消息 事件接收
        new Thread(() -> {
            while (true) {
                try {
                    String readLine = reader.readLine();
                    if (readLine == null && initSocket(Integer.MAX_VALUE)) { // 连接中断 处理
                        continue;
                    }

                    String type;

                    // +ping
                    if ("+ping".equals(readLine)) {
                        send("+pong");
                        continue;
                    }

                    // 主题订阅消息
                    if ("*3".equals(readLine)) {
                        reader.readLine(); // $7 len()

                        type = reader.readLine(); // message
                        if (!"message".equals(type)) {
                            continue;
                        }
                        reader.readLine(); //$n len(key)
                        String topic = reader.readLine(); // topic

                        String lenStr = reader.readLine();//$n len(value)
                        int clen = 0;
                        if (lenStr.startsWith("$")) {
                            clen = Integer.parseInt(lenStr.replace("$", ""));
                        }

                        String value = "";
                        do {
                            if (value.length() > 0) {
                                value += "\r\n";
                            }
                            String s = reader.readLine();
                            value += s; // value
                        } while (clen > 0 && clen > strLength(value));


                        // lock msg
                        if ("lock".equals(topic)) {
                            Lock lock = lockTag.get(value);
                            if (lock != null) {
                                synchronized (lock) {
                                    lock.notifyAll();
                                }
                            }
                            continue;
                        }
                        // rpc back msg
                        if (APP_NAME.equals(topic)) {
                            rpcBackQueue.add(Event.of(topic, value));
                            continue;
                        }

                        // rpc call msg
                        if (rpcTopics.contains(topic)) {
                            rpcCallQueue.add(Event.of(topic, value));
                            continue;
                        }

                        // oth msg
                        topicQueue.add(Event.of(topic, value));
                        continue;
                    }

                    // timer 消息
                    if ("*2".equals(readLine)) {
                        reader.readLine(); // $7 len()

                        type = reader.readLine(); // message
                        if (!"timer".equals(type)) {
                            continue;
                        }
                        reader.readLine(); //$n len(key)
                        String topic = reader.readLine(); // name

                        timerQueue.add(timerMap.get(topic));
                        continue;
                    }

                    logger.finest(readLine);
                } catch (IOException e) {
                    if (e instanceof SocketException) {
                        initSocket(Integer.MAX_VALUE);
                    }
                    e.printStackTrace();
                }
            }
        }).start();

        // 定时调度事件
        threadBuilder.accept(() -> {
            while (true) {
                Timer timer = null;
                try {
                    if ((timer = timerQueue.take()) == null) {
                        return;
                    }
                    long start = System.currentTimeMillis();
                    timer.runnable.run();
                    long end = System.currentTimeMillis();
                    logger.finest(String.format("timer [%s] : elapsed time %s ms", timer.name, end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "timer [" + timer.name + "]", e);
                }
            }
        }, 1);

        threadBuilder.accept(() -> {
            while (true) {
                Event<String> event = null;
                try {
                    if ((event = topicQueue.take()) == null) {
                        continue;
                    }

                    accept(event.topic, event.value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "topic[" + event.topic + "] event accept error :" + event.value, e);
                }
            }
        }, 1);

        // rpc back
        threadBuilder.accept(() -> {
            while (true) {
                Event<String> event = null;
                try {
                    if ((event = rpcBackQueue.take()) == null) {
                        continue;
                    }
                    //if (event)
                    logger.info(String.format("rpc-back:[%s]: %s", event.topic, event.value));
                    rpcAccept(event.value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "rpc-back[" + event.topic + "] event accept error :" + event.value, e);
                }
            }
        }, 1);

        // rpc call
        threadBuilder.accept(() -> {
            while (true) {
                Event<String> event = null;
                try {
                    if ((event = rpcCallQueue.take()) == null) {
                        continue;
                    }
                    logger.info(String.format("rpc-call:[%s] %s", event.topic, event.value));
                    accept(event.topic, event.value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "rpc-call[" + event.topic + "] event accept error :" + event.value, e);
                }
            }
        }, 1);

        // send msg
        threadBuilder.accept(() -> {
            while (true) {
                String msg = null;
                try {
                    if ((msg = sendMsgQueue.take()) == null) {
                        continue;
                    }
                    writer.write(msg.getBytes());
                    writer.flush();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "send-msg[" + msg + "] event accept error :", e);
                }
            }
        }, 1);

    }

    // ---------------------
    // -- 消息发送 --
    protected boolean send(String... data) {
        if (data.length == 1) {
            sendMsgQueue.add(data[0] + "\r\n");
        } else if (data.length > 1) {
            StringBuilder buf = new StringBuilder();
            buf.append("*" + data.length + "\r\n");
            for (String d : data) {
                buf.append("$" + strLength(d) + "\r\n");
                buf.append(d + "\r\n");
            }
            sendMsgQueue.add(buf.toString());
        }
        return true;
    }

    /*protected boolean send(String... data) {
        try {
            lock.lock();
            if (data.length == 1) {
                writer.write((data[0] + "\r\n").getBytes());
            } else if (data.length > 1) {
                writer.write(("*" + data.length + "\r\n").getBytes());
                for (String d : data) {
                    writer.write(("$" + d.length() + "\r\n").getBytes());
                    writer.write((d + "\r\n").getBytes());
                }
            }
            writer.flush();
            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
        } finally {
            lock.unlock();
        }
        return false;
    }*/

    private int strLength(String str) {
        str = str.replaceAll("[^\\x00-\\xff]", "*");
        return str.length();
    }

    private String toStr(Object v) {
        if (v instanceof String) {
            return (String) v;
        } else if (v == null) {
            return null;
        }

        return gson.toJson(v);
    }

    protected boolean initSocket(int retry) {
        for (int i = 0; i <= retry; i++) {
            try {
                String[] hostPort = addr.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);

                //private ReentrantLock lock = new ReentrantLock();
                Socket client = new Socket();
                client.connect(new InetSocketAddress(host, port));
                client.setKeepAlive(true);

                writer = client.getOutputStream();
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String groupid = getGroupid();
                if (groupid == null || groupid.isEmpty()) {
                    throw new RuntimeException("ZHubClient groupid can not is empty");
                }
                send("auth", auth);
                send("groupid " + groupid);

                StringBuffer buf = new StringBuffer("subscribe lock");
                /*if (isMain) {
                }*/
                if (mainHub.containsValue(this)) {
                    buf.append(" ").append(APP_NAME);
                }
                for (String topic : getTopics()) {
                    buf.append(" ").append(topic);
                }
                send(buf.toString());

                // 重连 timer 订阅
                timerMap.forEach((name, timer) -> send("timer", name));
                if (retry > 0) {
                    logger.warning(String.format("ZHubClient[%s][%s] %s Succeed！", getGroupid(), i + 1, retry > 0 ? "reconnection" : "init"));
                } else {
                    logger.fine(String.format("ZHubClient[%s] %s Succeed！", getGroupid(), retry > 0 ? "reconnection" : "init"));
                }
                return true;
            } catch (Exception e) {
                if (retry == 0 || i == 0) {
                    logger.log(Level.WARNING, String.format("ZHubClient[%s] %s Failed 初始化失败！", getGroupid(), retry == 0 ? "init" : "reconnection"), e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                } else {
                    logger.log(Level.WARNING, String.format("ZHubClient[%s][%s] reconnection Failed！", getGroupid(), i + 1));
                    try {
                        Thread.sleep(1000 * 5);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void unsubscribe(String topic) {
        send("unsubscribe " + topic);
        super.removeEventType(topic);
    }

    public boolean publish(String topic, Object v) {
        return send("publish", topic, toStr(v));
    }

    public void broadcast(String topic, Object v) {
        send("broadcast", topic, toStr(v));
    }

    // 发送 publish 主题消息，若多次发送的 topic + "-" + value 相同，将会做延时重置
    public void delay(String topic, Object v, long millis) {
        send("delay", topic, toStr(v), String.valueOf(millis));
    }

    // 表达式支持：d+[d,H,m,s]
    public void delay(String topic, Object v, String delayExpr) {
        String endchar = "";
        int delay;
        if (delayExpr.matches("^\\d+[d,H,m,s]$")) {
            endchar = delayExpr.substring(delayExpr.length() - 1);
            delay = Integer.parseInt(delayExpr.substring(0, delayExpr.length() - 1));
        } else {
            if (!delayExpr.matches("^\\d+$")) {
                throw new IllegalArgumentException(String.format("ScheduledCycle period config error: [%s]", delayExpr));
            }

            delay = Integer.parseInt(delayExpr);
            if (delay <= 0L) {
                throw new IllegalArgumentException(String.format("ScheduledCycle period config error: [%s]", delayExpr));
            }
        }

        switch (endchar) {
            case "M":
                delay *= (1000 * 60 * 60 * 24 * 30);
                break;
            case "d":
                delay *= (1000 * 60 * 60 * 24);
                break;
            case "H":
                delay *= (1000 * 60 * 60);
                break;
            case "m":
                delay *= (1000 * 60);
                break;
            case "s":
                delay *= 1000;
                break;
        }

        delay(topic, v, delay);
    }

    @Override
    protected String getGroupid() {
        return groupid;
    }

    @Override
    protected void subscribe(String topic) {
        send("subscribe " + topic); //新增订阅
    }

    // ================================================== lock ==================================================
    private final Map<String, Lock> lockTag = new ConcurrentHashMap<>();

    public Lock tryLock(String key, int duration) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Lock lock = new Lock(key, uuid, duration, this);
        lockTag.put(uuid, lock);

        try {
            // c.send("lock", key, uuid, strconv.Itoa(duration))
            send("lock", key, uuid, String.valueOf(duration));
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lock;
    }

    // ================================================== timer ==================================================
    private final ConcurrentHashMap<String, Timer> timerMap = new ConcurrentHashMap();

    class Timer {
        String name;
        //String expr;
        Runnable runnable;
        //boolean single;

        public String getName() {
            return name;
        }


        public Runnable getRunnable() {
            return runnable;
        }

        public Timer(String name, Runnable runnable) {
            this.name = name;
            this.runnable = runnable;
        }
    }

    public void timer(String name, Runnable run) {
        timerMap.put(name, new Timer(name, run));
        send("timer", name);
    }

    public void reloadTimer() {
        send("cmd", "reload-timer");
    }

    // ================================================== rpc ==================================================
    // -- 调用端 --
    private static final Map<String, Rpc> rpcMap = new ConcurrentHashMap<>();
    private static final Map<String, TypeToken> rpcRetType = new ConcurrentHashMap<>();

    // rpc call
    public RpcResult<Void> rpc(String topic, Object v) {
        return rpc(topic, v, null);
    }

    // rpc call
    public <T, R> RpcResult<R> rpc(String topic, T v, TypeToken<R> typeToken) {
        return rpc(topic, v, typeToken, 0);
    }

    // rpc call
    public <T, R> RpcResult<R> rpc(String topic, T v, TypeToken<R> typeToken, long timeout) {
        Rpc rpc = new Rpc<>(APP_NAME, UUID.randomUUID().toString().replaceAll("-", ""), topic, v);
        String ruk = rpc.getRuk();
        rpcMap.put(ruk, rpc);
        if (typeToken != null) {
            rpcRetType.put(ruk, typeToken);
        }
        try {
            publish(topic, rpc); // send("rpc", topic, toStr(rpc));
            synchronized (rpc) {
                if (timeout <= 0) {
                    timeout = 1000 * 15;
                }
                // call timeout default: 15s
                Timers.delay(() -> {
                    synchronized (rpc) {
                        Rpc rpc1 = rpcMap.get(ruk);
                        if (rpc1 == null) {
                            return;
                        }

                        RpcResult rpcResult = rpc.buildResp(505, "请求超时");
                        rpc.setRpcResult(rpcResult);
                        logger.warning("rpc timeout: " + gson.toJson(rpc));
                        rpc.notify();
                    }
                }, timeout);

                rpc.wait();
                rpcMap.remove(ruk);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            // call error
            RpcResult rpcResult = rpc.buildResp(501, "请求失败");
            rpc.setRpcResult(rpcResult);
        }
        return rpc.getRpcResult();
    }

    public <T, R> CompletableFuture<RpcResult<R>> rpcAsync(String topic, T v) {
        return CompletableFuture.supplyAsync(() -> rpc(topic, v, null));
    }

    public <T, R> CompletableFuture<RpcResult<R>> rpcAsync(String topic, T v, TypeToken<R> typeToken) {
        return CompletableFuture.supplyAsync(() -> rpc(topic, v, typeToken));
    }

    public <T, R> CompletableFuture<RpcResult<R>> rpcAsync(String topic, T v, long timeout) {
        return CompletableFuture.supplyAsync(() -> rpc(topic, v, null, timeout));
    }

    public <T, R> CompletableFuture<RpcResult<R>> rpcAsync(String topic, T v, TypeToken<R> typeToken, long timeout) {
        return CompletableFuture.supplyAsync(() -> rpc(topic, v, typeToken, timeout));
    }

    // RpcResult: {ruk:xxx-xxxx, retcode:0}
    // rpc call back consumer
    private void rpcAccept(String value) {
        RpcResult resp = gson.fromJson(value, new TypeToken<RpcResult<String>>() {
        }.getType());

        String ruk = resp.getRuk();
        Rpc rpc = rpcMap.remove(ruk);
        if (rpc == null) {
            return;
        }
        TypeToken typeToken = rpcRetType.get(ruk);

        Object result = resp.getResult();
        if (result != null && typeToken != null && !"java.lang.String".equals(typeToken.getType().getTypeName())) {
            result = gson.fromJson((String) resp.getResult(), typeToken.getType());
        }

        resp.setResult(result);
        rpc.setRpcResult(resp);
        synchronized (rpc) {
            rpc.notify();
        }
    }

    // -- 订阅端 --
    private final HashSet rpcTopics = new HashSet();

    // rpc call consumer
    public <T, R> void rpcSubscribe(String topic, TypeToken<T> typeToken, Function<Rpc<T>, RpcResult<R>> fun) {
        Consumer<String> consumer = v -> {
            Rpc<T> rpc = null;
            try {
                rpc = gson.fromJson(v, new TypeToken<Rpc<String>>() {
                }.getType());

                // 参数转换
                T paras = gson.fromJson((String) rpc.getValue(), typeToken.getType());
                rpc.setValue(paras);
                RpcResult result = fun.apply(rpc);
                result.setResult(toStr(result.getResult()));
                publish(rpc.getBackTopic(), result);
            } catch (Exception e) {
                logger.log(Level.WARNING, "rpc call consumer error: " + v, e);
                publish(rpc.getBackTopic(), rpc.buildError("服务调用失败！"));
            }
            // back
        };

        rpcTopics.add(topic);
        subscribe(topic, consumer);
    }
}
