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
import java.util.logging.Level;

public class ZdbProducer<T extends Event> implements IProducer<T>, Service {

    @Resource(name = "property.zdb.host")
    private String host = "39.108.56.246";
    @Resource(name = "property.zdb.password")
    private String password = "";
    @Resource(name = "property.zdb.port")
    private int port = 1216;

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
        try {
            String v = JsonConvert.root().convertTo(t.value);
            if (v.startsWith("\"") && v.endsWith("\"")) {
                v = v.substring(1, v.length() - 1);
            }

            os.write("*3\r\n".getBytes());
            os.write("$7\r\n".getBytes());
            os.write("publish\r\n".getBytes());
            os.write(("$" + t.topic.length() + "\r\n").getBytes());
            os.write((t.topic + "\r\n").getBytes());
            os.write(("$" + v.length() + "\r\n").getBytes());
            os.write((v + "\r\n").getBytes());
            os.flush();
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
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
