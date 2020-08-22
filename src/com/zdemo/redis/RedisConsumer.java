package com.zdemo.redis;

import com.zdemo.Event;
import com.zdemo.IConsumer;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public abstract class RedisConsumer<T extends Event> implements IConsumer<T>, Service {

    @Resource(name = "property.redis.host")
    private String host = "127.0.0.1";
    @Resource(name = "property.redis.password")
    private String password = "";
    @Resource(name = "property.redis.port")
    private int port = 6379;

    public String getGroupid() {
        return "";
    }

    @Override
    public void init(AnyValue config) {
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
                        accept(value);
                    } catch (Exception e) {
                        logger.warning("event accept error :" + value);
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
