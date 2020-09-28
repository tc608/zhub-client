package com.zdemo.redis;

import com.zdemo.AbstractConsumer;
import com.zdemo.IConsumer;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

public abstract class RedisConsumer extends AbstractConsumer implements IConsumer, Service {

    @Resource(name = "property.redis.host")
    private String host = "127.0.0.1";
    @Resource(name = "property.redis.password")
    private String password = "";
    @Resource(name = "property.redis.port")
    private int port = 6379;

    @Override
    public void init(AnyValue config) {
        new Thread(() -> {
            try {
                Socket client = new Socket();
                client.connect(new InetSocketAddress(host, port));
                client.setKeepAlive(true);

                OutputStreamWriter oswSub = new OutputStreamWriter(client.getOutputStream());
                oswSub.write("AUTH " + password + "\r\n");
                oswSub.flush();

                StringBuffer buf = new StringBuffer("SUBSCRIBE");
                for (String topic : getSubscribes()) {
                    buf.append(" ").append(topic);
                }
                buf.append(" _ping\r\n");
                oswSub.write(buf.toString());
                oswSub.flush();

                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String type = "";
                String readLine;
                while ((readLine = br.readLine()) != null) {
                    if ("*3".equals(readLine)) {
                        br.readLine(); // $7 len()
                        type = br.readLine(); // message
                        if (!"message".equals(type)) {
                            continue;
                        }
                        br.readLine(); //$n len(key)
                        String topic = br.readLine(); // topic

                        br.readLine(); //$n len(value)
                        String value = br.readLine(); // value
                        try {
                            accept(topic, value);
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "topic[" + topic + "] event accept error :" + value, e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Redis Consumer 初始化失败！", e);
            }
        }).start();
    }
}
