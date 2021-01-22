package com.zdemo.redis;

import com.zdemo.AbstractConsumer;
import com.zdemo.EventType;
import com.zdemo.IConsumer;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;
import org.redkale.util.TypeToken;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisConsumer extends AbstractConsumer implements IConsumer, Service {

    public Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Resource(name = "property.redis.host")
    private String host = "127.0.0.1";
    @Resource(name = "property.redis.password")
    private String password = "";
    @Resource(name = "property.redis.port")
    private int port = 6379;

    private Socket client;
    private OutputStreamWriter writer;
    private BufferedReader reader;

    @Override
    public void init(AnyValue config) {
        try {
            client = new Socket();
            client.connect(new InetSocketAddress(host, port));
            client.setKeepAlive(true);

            writer = new OutputStreamWriter(client.getOutputStream());
            writer.write("AUTH " + password + "\r\n");
            writer.flush();

            StringBuffer buf = new StringBuffer("SUBSCRIBE");
            for (String topic : getTopics()) {
                buf.append(" ").append(topic);
            }
            buf.append("\r\n");
            writer.write(buf.toString());
            writer.flush();

            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Redis Consumer 初始化失败！", e);
        }

        new Thread(() -> {
            try {
                while (true) {
                    String readLine = reader.readLine();
                    String type = "";
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
                        try {
                            accept(topic, value);
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "topic[" + topic + "] event accept error :" + value, e);
                        }
                    }
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "", e);
            }
        }).start();
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
                    writer.write("SUBSCRIBE " + topic + "\r\n");
                    writer.flush();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "", e);
                }
            }
        }
    }

    @Override
    public void unsubscribe(String topic) {
        try {
            writer.write("UNSUBSCRIBE " + topic + "\r\n");
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
        }
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
