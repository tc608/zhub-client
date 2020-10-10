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

    private OutputStreamWriter osw;

    @Override
    public void init(AnyValue config) {
        try {
            Socket client = new Socket();
            client.connect(new InetSocketAddress(host, port));
            client.setKeepAlive(true);

            osw = new OutputStreamWriter(client.getOutputStream());
            osw.write("AUTH " + password + "\r\n");
            osw.flush();
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
        }
    }

    @Override
    public void send(T t) {
        try {
            osw.write("PUBLISH " + t.topic + " '" + JsonConvert.root().convertTo(t.value) + "' \r\n");
            osw.flush();
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);

        }
    }
}
