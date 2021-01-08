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
import java.util.logging.Level;

public class ZdbConsumer extends AbstractConsumer implements IConsumer, Service {

    @Resource(name = "property.zdb.host")
    private String host = "39.108.56.246";
    @Resource(name = "property.zdb.password")
    private String password = "";
    @Resource(name = "property.zdb.port")
    private int port = 1216;

    private Socket client;
    private OutputStream os;
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

    public boolean initSocket() {
        try {
            client = new Socket();
            client.connect(new InetSocketAddress(host, port));
            client.setKeepAlive(true);

            os = client.getOutputStream();

            StringBuffer buf = new StringBuffer("subscribe");
            for (String topic : getTopics()) {
                buf.append(" ").append(topic);
            }
            buf.append("\r\n");
            os.write(buf.toString().getBytes());
            os.flush();

            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Zdb Consumer 初始化失败！", e);
            return false;
        }

        return true;
    }

    @Override
    public String getGroupid() {
        return null;
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
                try {
                    os.write(("subscribe " + topic + "\r\n").getBytes());
                    os.flush();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "", e);
                }
            }
        }
    }

    @Override
    public void unsubscribe(String topic) {
        try {
            os.write(("unsubscribe " + topic + "\r\n").getBytes());
            os.flush();
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
        }
    }
}
