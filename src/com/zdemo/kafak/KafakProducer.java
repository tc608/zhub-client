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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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
        if (!file.exists()) {
            logger.warning(String.format("------\n%s (系统找不到指定的文件。)\n未初始化kafak 生产者，kafak发布消息不可用\n------", file.getPath()));
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            producer = new KafkaProducer(props);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(T... t) {
        for (T x : t) {
            logger.finest("send message: " + JsonConvert.root().convertTo(x));
            producer.send(new ProducerRecord(x.getTopic(), JsonConvert.root().convertTo(x)));
        }
    }

    @Override
    public void destroy(AnyValue config) {
        producer.close();
    }
}
