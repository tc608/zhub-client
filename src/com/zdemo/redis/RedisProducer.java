package com.zdemo.redis;

import com.zdemo.Event;
import com.zdemo.IProducer;
import org.redkale.convert.json.JsonConvert;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

public class RedisProducer<T extends Event> implements IProducer<T>, Service {

    @Resource(name = "property.redis.host")
    private String host = "127.0.0.1";
    @Resource(name = "property.redis.password")
    private String password = "";
    @Resource(name = "property.redis.port")
    private int port = 6379;

    private OutputStreamWriter oswPub;

    @Override
    public void init(AnyValue config) {
        try {
            Socket client = new Socket();
            client.connect(new InetSocketAddress(host, port));
            client.setKeepAlive(true);

            oswPub = new OutputStreamWriter(client.getOutputStream());
            oswPub.write("AUTH " + password + "\r\n");
            oswPub.flush();
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
        }
    }

    @Override
    public void send(T... t) {
        for (T x : t) {
            try {
                oswPub.write("PUBLISH " + x.topic + " '" + JsonConvert.root().convertTo(x.value) + "' \r\n");
                oswPub.flush();
            } catch (IOException e) {
                logger.log(Level.WARNING, "", e);
            }
        }
    }
}
