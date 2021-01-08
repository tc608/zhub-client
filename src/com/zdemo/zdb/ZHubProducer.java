package com.zdemo.zdb;

import com.zdemo.Event;
import com.zdemo.IProducer;
import org.redkale.convert.json.JsonConvert;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class ZHubProducer<T extends Event> implements IProducer<T>, Service {

    @Resource(name = "property.zdb.host")
    private String host = "39.108.56.246";
    @Resource(name = "property.zdb.password")
    private String password = "";
    @Resource(name = "property.zdb.port")
    private int port = 1216;

    private ReentrantLock lock = new ReentrantLock();

    private OutputStream os;

    @Override
    public void init(AnyValue config) {
        Socket client = new Socket();
        try {
            client.connect(new InetSocketAddress(host, port));
            client.setKeepAlive(true);
            os = client.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(T t) {
        String v = JsonConvert.root().convertTo(t.value);
        if (v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1);
        }
        send("publish", t.topic, v);
    }

    private void send(String... data) {
        try {
            lock.lock();
            if (data.length == 1) {
                os.write((data[0] + "\r\n").getBytes());
            } else if (data.length > 1) {
                os.write(("*" + data.length + "\r\n").getBytes());
                for (String d : data) {
                    os.write(("$" + d.length() + "\r\n").getBytes());
                    os.write((d + "\r\n").getBytes());
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
        } finally {
            lock.unlock();
        }
    }

    private byte[] toBytes(int v) {
        byte[] result = new byte[4];
        result[0] = (byte) ((v >> 24) & 0xFF);
        result[1] = (byte) ((v >> 16) & 0xFF);
        result[2] = (byte) ((v >> 8) & 0xFF);
        result[3] = (byte) (v & 0xFF);
        return result;
    }

}
