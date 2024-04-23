package dev.zhub.client;

import com.google.gson.reflect.TypeToken;
import dev.zhub.*;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import dev.zhub.timer.Timers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ZHubClient extends AbstractConsumer implements IConsumer, IProducer {

    public Logger logger = Logger.getLogger(ZHubClient.class.getSimpleName());
    @Setter
    @Value("${zhub.addr}")
    private String addr = "127.0.0.1:1216";
    @Setter
    @Value("${zhub.groupid}")
    private String groupid = "";
    @Setter
    @Value("${zhub.auth}")
    private String auth = "";
    @Setter
    @Value("${zhub.appid}")
    protected String appid = "";

    @PostConstruct
    public void init() {
        init(null);
    }

    private Socket client;
    private OutputStream writer;
    private BufferedReader reader;

    private final LinkedBlockingQueue<Timer> timerQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Event<String>> topicQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Event<Object>> rpcBackQueue = new LinkedBlockingQueue<>(); // RPC BACK MSG [=> Object]
    private final LinkedBlockingQueue<Event<Object>> rpcCallQueue = new LinkedBlockingQueue<>(); // RPC CALL MSG [=> Object]
    private final LinkedBlockingQueue<String> sendMsgQueue = new LinkedBlockingQueue<>(); // SEND MSG

    private static Map<String, ZHubClient> mainHub = new HashMap<>(); // 127.0.0.1:1216 - ZHubClient

    public ZHubClient() {

    }

    public ZHubClient(String addr, String groupid, String appid, String auth) {
        this.addr = addr;
        this.groupid = groupid;
        this.appid = appid;
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
            appid = config.getOrDefault("appname", appid);
        }

        // 设置第一个启动的 实例为主实例
        if (!mainHub.containsKey(addr)) { // 确保同步执行此 init 逻辑
            mainHub.put(addr, this);
        }

        CompletableFuture.runAsync(() -> {
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

                        String type = "";

                        // +ping
                        if ("+ping".equals(readLine)) {
                            send("+pong");
                            continue;
                        }

                        // 主题订阅消息
                        if ("*3".equals(readLine)) {
                            readLine = reader.readLine(); // $7 len()
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
                                if (!value.isEmpty()) {
                                    value += "\r\n";
                                }
                                String s = reader.readLine();
                                value += s; // value
                            } while (clen > 0 && clen > strLength(value));

                            logger.finest("topic[" + topic + "]: " + value);

                            // lock msg
                            if ("lock".equals(topic)) {
                                Lock lock = lockTag.get(value);
                                if (lock != null) {
                                    synchronized (lock) {
                                        lock.success = true;
                                        lock.notifyAll();
                                    }
                                }
                                continue;
                            }
                            // trylock msg
                            if ("trylock".equals(topic)) {
                                Lock lock = lockTag.get(value);
                                if (lock != null) {
                                    synchronized (lock) {
                                        lock.success = false;
                                        lock.notifyAll();
                                    }
                                }
                                continue;
                            }

                            // rpc back msg
                            if (appid.equals(topic)) {
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
                            readLine = reader.readLine(); // $7 len()
                            type = reader.readLine(); // message
                            if (!"timer".equals(type)) {
                                continue;
                            }
                            reader.readLine(); //$n len(key)
                            String topic = reader.readLine(); // name

                            logger.finest("timer[" + topic + "]: ");
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
        }).thenAcceptAsync(x -> {
            // 定时调度事件，已加入耗时监控
            new Thread(() -> {
                ExecutorService pool = Executors.newFixedThreadPool(1);
                while (true) {
                    Timer timer = null;
                    try {
                        timer = timerQueue.take();

                        long start = System.currentTimeMillis();
                        pool.submit(timer.runnable).get(5, TimeUnit.SECONDS);
                        long end = System.currentTimeMillis();
                        logger.finest(String.format("timer [%s] : elapsed time %s ms", timer.name, end - start));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        logger.log(Level.SEVERE, "timer [" + timer.name + "] time out: " + 5 + " S", e);
                        pool = Executors.newFixedThreadPool(1);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "timer [" + timer.name + "]", e);
                    }
                }
            }).start();

            // topic msg，已加入耗时监控
            new Thread(() -> {
                ExecutorService pool = Executors.newFixedThreadPool(1);
                while (true) {
                    Event<String> event = null;
                    try {
                        event = topicQueue.take();

                        String topic = event.topic;
                        String value = event.value;
                        pool.submit(() -> accept(topic, value)).get(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        logger.log(Level.SEVERE, "topic[" + event.topic + "] event deal time out: " + 5 + " S, value: " + toStr(event.value), e);
                        pool = Executors.newFixedThreadPool(1);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "topic[" + event.topic + "] event accept error :" + toStr(event.value), e);
                    }
                }
            }).start();

            // rpc back ,仅做数据解析，暂无耗时监控
            new Thread(() -> {
                while (true) {
                    Event<Object> event = null;
                    try {
                        event = rpcBackQueue.take();
                        //if (event)
                        logger.finest(String.format("rpc-back:[%s]: %s", event.topic, toStr(event.value)));
                        rpcAccept(event.value);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "rpc-back[" + event.topic + "] event accept error :" + toStr(event.value), e);
                    }
                }
            }).start();

            // rpc call，已加入耗时监控
            new Thread(() -> {
                ExecutorService pool = Executors.newFixedThreadPool(1);
                while (true) {
                    Event<Object> event = null;
                    try {
                        event = rpcCallQueue.take();

                        logger.finest(String.format("rpc-call:[%s] %s", event.topic, toStr(event.value)));
                        String topic = event.topic;
                        Object value = event.value;
                        pool.submit(() -> rpcAccept(topic, value)).get(5, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        logger.log(Level.SEVERE, "topic[" + event.topic + "] event deal time out: " + 5 + " S, value: " + toStr(event.value), e);
                        pool = Executors.newFixedThreadPool(1);
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "rpc-call[" + event.topic + "] event accept error :" + toStr(event.value), e);
                    }
                }
            }).start();

            // send msg
            new Thread(() -> {
                while (true) {
                    String msg = null;
                    try {
                        msg = sendMsgQueue.take();

                        // logger.log(Level.FINEST, "send-msg: [" + msg + "]");
                        writer.write(msg.getBytes());
                        writer.flush();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "send-msg[" + msg + "] event accept error :", e);
                    }
                }
            }).start();
        });
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

    protected boolean initSocket(int retry) {
        for (int i = 0; i <= retry; i++) {
            try {
                String[] hostPort = addr.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);

                client = new Socket();
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

                StringBuilder buf = new StringBuilder("subscribe lock trylock");
                if (mainHub.containsValue(this)) {
                    buf.append(" ").append(appid);
                }
                for (String topic : getTopics()) {
                    buf.append(" ").append(topic);
                }
                send(buf.toString());

                // 重连 timer 订阅
                timerMap.forEach((name, timer) -> send("timer", name));
                if (retry > 0) {
                    logger.warning(String.format("ZHubClient[%s][%s] %s Succeed！", getGroupid(), i + 1, "reconnection"));
                } else {
                    logger.fine(String.format("ZHubClient[%s] %s Succeed！", getGroupid(), "init"));
                }
                return true;
            } catch (Exception e) {
                if (i == 0) {
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
        /*if (eventMap.containsKey(topic)) { // 本地调用
            topicQueue.add(Event.of(topic, v));
            return true;
        }*/
        return send("publish", topic, toStr(v));
    }

    public void broadcast(String topic, Object v) {
        send("broadcast", topic, toStr(v)); // 广播必须走远端模式
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

        if ("M".equals(endchar)) {
            delay *= (1000 * 60 * 60 * 24 * 30);
        } else if ("d".equals(endchar)) {
            delay *= (1000 * 60 * 60 * 24);
        } else if ("H".equals(endchar)) {
            delay *= (1000 * 60 * 60);
        } else if ("m".equals(endchar)) {
            delay *= (1000 * 60);
        } else if ("s".equals(endchar)) {
            delay *= 1000;
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
    private Map<String, Lock> lockTag = new ConcurrentHashMap<>();

    /**
     * 尝试加锁，立即返回，
     *
     * @param key
     * @param duration
     * @return Lock: lock.success 锁定是否成功标识
     */
    public Lock tryLock(String key, int duration) {
        return lock("trylock", key, duration);
    }

    public Lock lock(String key, int duration) {
        return lock("lock", key, duration);
    }

    /**
     * @param cmd      lock|trylock
     * @param key      加锁 key
     * @param duration 锁定时长
     * @return
     */
    private Lock lock(String cmd, String key, int duration) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Lock lock = new Lock(key, uuid, duration, this);
        lockTag.put(uuid, lock);

        try {
            // c.send("lock", key, uuid, strconv.Itoa(duration))
            send(cmd, key, uuid, String.valueOf(duration));
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return lock;
    }

    // ================================================== timer ==================================================
    private ConcurrentHashMap<String, Timer> timerMap = new ConcurrentHashMap();

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

    @Deprecated
    public void reloadTimer() {
        send("cmd", "reload-timer");
    }

    // ================================================== rpc ==================================================
    // -- 调用端 --
    private static final Map<String, Rpc> rpcMap = new ConcurrentHashMap<>();
    // private static final Map<String, TypeToken> rpcRetType = new ConcurrentHashMap<>();

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
        Rpc rpc = new Rpc<>(appid, topic, v);
        rpc.setTypeToken(typeToken);

        String ruk = rpc.getRuk();
        rpcMap.put(ruk, rpc);
        try {
            if (eventMap.containsKey(topic)) { // 本地调用
                rpcCallQueue.add(Event.of(topic, rpc));
            } else {
                rpc.setValue(toStr(rpc.getValue()));
                publish(topic, rpc); // send("rpc", topic, toStr(rpc));
            }
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

                        RpcResult rpcResult = rpc.retError(505, "请求超时");
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
            RpcResult rpcResult = rpc.retError(501, "请求失败");
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
    private <T> void rpcAccept(T value) {
        // 接收到 本地调用返回的 RpcResult
        if (value instanceof RpcResult) {
            String ruk = ((RpcResult) value).getRuk();
            Rpc rpc = rpcMap.remove(ruk);
            if (rpc == null) {
                return;
            }

            // TODO, 本地模式下返回的数据对象类型需要和处理端一致，不然会出现类型转换异常 - 解决办法，当出现不一致的情况取数据做转换
            TypeToken typeToken = rpc.getTypeToken();
            if (typeToken.getType() != ((RpcResult<?>) value).getResult().getClass()) {
                Object result = gson.fromJson(toStr(((RpcResult<?>) value).getResult()), typeToken.getType());
                ((RpcResult<Object>) value).setResult(result);
            }

            rpc.setRpcResult((RpcResult) value);
            synchronized (rpc) {
                rpc.notify();
            }
            return;
        }

        RpcResult resp = gson.fromJson((String) value, new TypeToken<RpcResult<String>>() {
        }.getType());

        String ruk = resp.getRuk();
        Rpc rpc = rpcMap.remove(ruk);
        if (rpc == null) {
            return;
        }
        TypeToken typeToken = rpc.getTypeToken();

        Object result = resp.getResult();
        if (result != null && typeToken != null && !"java.lang.String".equals(typeToken.getType().getTypeName()) && !"java.lang.Void".equals(typeToken.getType().getTypeName())) {
            result = gson.fromJson((String) resp.getResult(), typeToken.getType());
        }

        resp.setResult(result);
        rpc.setRpcResult(resp);
        synchronized (rpc) {
            rpc.notify();
        }
    }

    // -- 订阅端 --
    private Set<String> rpcTopics = new HashSet();

    // rpc call consumer
    public <R> void rpcSubscribe(String topic, Function<Rpc<String>, RpcResult<R>> fun) {
        rpcSubscribe(topic, IType.STRING, fun);
    }

    // rpc call consumer
    public <T, R> void rpcSubscribe(String topic, TypeToken<T> typeToken, Function<Rpc<T>, RpcResult<R>> fun) {
        Consumer<T> consumer = v -> {
            Rpc<T> rpc = null;
            try {
                if (v instanceof String) {
                    rpc = gson.fromJson((String) v, new TypeToken<Rpc<String>>() {
                    }.getType());
                } else {
                    rpc = (Rpc<T>) v;
                }

                // 参数转换
                if (rpc.getValue() instanceof String && !"java.lang.String".equals(typeToken.getType().getTypeName())) {
                    T paras = gson.fromJson((String) rpc.getValue(), typeToken.getType());
                    rpc.setValue(paras);
                }

                RpcResult result = fun.apply(rpc);
                if (appid.equals(rpc.getBackTopic())) {
                    rpcBackQueue.add(Event.of(topic, result));
                } else {
                    result.setResult(toStr(result.getResult())); // 远程模式 结果转换
                    publish(rpc.getBackTopic(), result);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "rpc call consumer error: " + v, e);
                publish(rpc.getBackTopic(), rpc.retError("服务调用失败！"));
            }
            // back
        };

        rpcTopics.add(topic);
        subscribe(topic, typeToken, consumer);
    }

    public static void main(String[] args) {
        System.out.println(IType.INT.getType());
        Integer a = 1;
        System.out.println(a.getClass());
    }
}
