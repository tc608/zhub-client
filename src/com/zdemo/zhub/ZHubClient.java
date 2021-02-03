package com.zdemo.zhub;

import com.zdemo.AbstractConsumer;
import com.zdemo.Event;
import com.zdemo.IConsumer;
import com.zdemo.IProducer;
import org.redkale.convert.json.JsonConvert;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ZHubClient extends AbstractConsumer implements IConsumer, IProducer, Service {

    public Logger logger = Logger.getLogger(ZHubClient.class.getSimpleName());

    @Resource(name = "property.zhub.host")
    private String host = "127.0.0.1";
    @Resource(name = "property.zhub.password")
    private String password = "";
    @Resource(name = "property.zhub.port")
    private int port = 1216;

    private ReentrantLock lock = new ReentrantLock();
    private Socket client;
    private OutputStream writer;
    private BufferedReader reader;

    private final LinkedBlockingQueue<Timer> timerQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Event<String>> topicQueue = new LinkedBlockingQueue<>();

    private BiConsumer<Runnable, Integer> threadBuilder = (r, n) -> {
        for (int i = 0; i < n; i++) {
            new Thread(() -> r.run()).start();
        }
    };

    @Override
    public void init(AnyValue config) {
        if (!preInit()) {
            return;
        }
        if (!initSocket()) {
            return;
        }
        // 消息 事件接收
        new Thread(() -> {
            while (true) {
                try {
                    String readLine = reader.readLine();
                    if (readLine == null) { // 连接中断 处理
                        while (!initSocket()) {
                            try {
                                Thread.sleep(1000 * 5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    String type = "";
                    // 主题订阅消息
                    if ("*3".equals(readLine)) {
                        readLine = reader.readLine(); // $7 len()
                        type = reader.readLine(); // message
                        if (!"message".equals(type)) {
                            continue;
                        }
                        reader.readLine(); //$n len(key)
                        String topic = reader.readLine(); // topic

                        reader.readLine(); //$n len(value)
                        String value = reader.readLine(); // value

                        topicQueue.put(Event.of(topic, value));
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

                        timerQueue.put(timerMap.get(topic));
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "reconnection ", e.getMessage());
                    if (e instanceof SocketException) {
                        while (!initSocket()) {
                            try {
                                Thread.sleep(1000 * 5);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        }
                    }
                } catch (InterruptedException e) {
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

    }

    // ---------------------
    // 消息发送
    private void send(String... data) {
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
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
        } finally {
            lock.unlock();
        }
    }

    private <V> String toStr(V v) {
        if (v instanceof String) {
            return (String) v;
        }
        return JsonConvert.root().convertTo(v);
    }

    protected boolean initSocket() {
        try {
            client = new Socket();
            client.connect(new InetSocketAddress(host, port));
            client.setKeepAlive(true);

            writer = client.getOutputStream();
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            send("groupid " + getGroupid());

            StringBuffer buf = new StringBuffer("subscribe");
            for (String topic : getTopics()) {
                buf.append(" ").append(topic);
            }
            send(buf.toString());

            // 重连 timer 订阅
            timerMap.forEach((name, timer) -> {
                send("timer", name);
            });
        } catch (IOException e) {
            logger.log(Level.WARNING, "Zdb Consumer 初始化失败！", e);
            return false;
        }

        return true;
    }

    @Override
    public void unsubscribe(String topic) {
        send("unsubscribe " + topic);
        super.removeEventType(topic);
    }

    public <V> void publish(String topic, V v) {
        send("publish", topic, toStr(v));
    }

    public <V> void broadcast(String topic, V v) {
        send("broadcast", topic, toStr(v));
    }

    // 发送 publish 主题消息，若多次发送的 topic + "-" + value 相同，将会做延时重置
    public <V> void delay(String topic, V v, int delay) {
        send("delay", topic, toStr(v), String.valueOf(delay));
    }

    // 表达式支持：d+[d,H,m,s]
    public <V> void delay(String topic, V v, String delayExpr) {
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
    protected void subscribe(String topic) {
        send("subscribe " + topic); //新增订阅
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

    public void reloadTimer() {
        send("cmd", "reload-timer-config");
    }
}
