package com.zdemo.zdb;

import com.zdemo.AbstractConsumer;
import com.zdemo.EventType;
import com.zdemo.IConsumer;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public abstract class ZHubConsumer extends AbstractConsumer implements IConsumer, Service {

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

    @Override
    public void init(AnyValue config) {
        boolean flag = initSocket();
        new Thread(() -> {
            while (flag) {
                String topic = "";
                String value = "";
                try {
                    String readLine = reader.readLine();
                    String type = "";

                    // 主题订阅消息
                    if ("*3".equals(readLine)) {
                        readLine = reader.readLine(); // $7 len()
                        type = reader.readLine(); // message
                        if (!"message".equals(type)) {
                            continue;
                        }
                        reader.readLine(); //$n len(key)
                        topic = reader.readLine(); // topic

                        reader.readLine(); //$n len(value)
                        value = reader.readLine(); // value
                        accept(topic, value);
                    }

                    // timer 消息
                    if ("*2".equals(readLine)) {
                        readLine = reader.readLine(); // $7 len()
                        type = reader.readLine(); // message
                        if (!"timer".equals(type)) {
                            continue;
                        }
                        reader.readLine(); //$n len(key)
                        topic = reader.readLine(); // name

                        Timer timer = timerMap.get(topic);
                        if (timer != null) {
                            timer.runnable.run();
                        }
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "reconnection ", e.getMessage());
                    if (e instanceof SocketException) {
                        while (!initSocket()) {
                            try {
                                Thread.sleep(1000 * 3);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "topic[" + topic + "] event accept error :" + value, e);
                }
            }
        }).start();
    }

    // ---------------------
    // 消息发送类
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
            buf.append("\r\n");

            // 重连 timer 订阅
            timerMap.forEach((name, timer) -> {
                send("timer", name, timer.expr, timer.single ? "a" : "x");
            });
            send(buf.toString());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Zdb Consumer 初始化失败！", e);
            return false;
        }

        return true;
    }

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

    public void timer(String name, String expr, Runnable run) {
        timerMap.put(name, new Timer(name, expr, run, false));
        send("timer", name, expr, "x");
    }

    public void timerSingle(String name, String expr, Runnable run) {
        send("timer", name, expr, "a");
        timerMap.put(name, new Timer(name, expr, run, true));
    }

    class Timer {
        String name;
        String expr;
        Runnable runnable;
        boolean single;

        public String getName() {
            return name;
        }

        public String getExpr() {
            return expr;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public boolean isSingle() {
            return single;
        }

        public Timer(String name, String expr, Runnable runnable, boolean single) {
            this.name = name;
            this.runnable = runnable;
            this.single = single;
        }
    }
}
