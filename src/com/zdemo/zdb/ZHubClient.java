package com.zdemo.zdb;

import com.zdemo.*;
import org.redkale.convert.json.JsonConvert;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;
import org.redkale.util.TypeToken;

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
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ZHubClient extends AbstractConsumer implements IConsumer, IProducer, Service {

    Logger logger = Logger.getLogger(IProducer.class.getSimpleName());

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
        }, 2);

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

    public boolean initSocket() {
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

    @Deprecated
    @Override
    public void addEventType(EventType... eventType) {
        for (EventType type : eventType) {
            String[] topics = type.topic.split(",");
            for (String topic : topics) {
                if (topic.isEmpty()) {
                    continue;
                }
                eventMap.put(topic, type);

                //新增订阅
                send("subscribe " + topic);
            }
        }
    }

    @Override
    public void unsubscribe(String topic) {
        send("unsubscribe " + topic);
    }

    // timer
    private ConcurrentHashMap<String, Timer> timerMap = new ConcurrentHashMap();

    public void timer(String name, Runnable run) {
        timerMap.put(name, new Timer(name, run));
        send("timer", name);
    }

    public void reloadTimer() {
        send("cmd", "reload-timer-config");
    }

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


    public <V> void publish(String topic, V v) {
        send("publish", topic, toStr(v));
    }

    public <V> void broadcast(String topic, V v) {
        send("broadcast", topic, toStr(v));
    }

    public <V> void delay(String topic, V v, int delay) {
        send("delay", topic, toStr(v), String.valueOf(delay));
    }

    @Override
    public void subscribe(String topic, Consumer<String> consumer) {
        addEventType(EventType.of(topic, consumer));
    }

    @Override
    public <T> void subscribe(String topic, TypeToken<T> typeToken, Consumer<T> consumer) {
        addEventType(EventType.of(topic, typeToken, consumer));
    }
}
