package com.zdemo.kafak;

import com.zdemo.Event;
import com.zdemo.IProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.redkale.convert.json.JsonConvert;
import org.redkale.net.http.RestService;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

/**
 * 生产
 *
 * @param <T>
 */
@RestService
public class KafakProducer<T extends Event> implements IProducer<T>, Service {
    private KafkaProducer<String, String> producer;

    @Resource(name = "APP_HOME")
    protected File APP_HOME;

    @Override
    public void init(AnyValue config) {
        File file = new File(APP_HOME, "conf/kafak.properties");
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            producer = new KafkaProducer(props);
        } catch (IOException e) {
            logger.log(Level.WARNING, "未初始化kafak 生产者，kafak发布消息不可用", e);
        }
    }

    @Override
    public void send(T... t) {
        for (T x : t) {
            String v = JsonConvert.root().convertTo(x.value);
            if (v.startsWith("\"") && v.endsWith("\"")) {
                v = v.substring(1, v.length() - 1);
            }
            producer.send(new ProducerRecord(x.topic, v));
        }
    }

    @Override
    public void destroy(AnyValue config) {
        producer.close();
    }
}
