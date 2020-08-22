package com.zdemo.kafak;

import com.zdemo.Event;
import com.zdemo.IConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.redkale.net.http.RestService;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/**
 * 消费
 *
 * @param <T>
 */
@RestService
public abstract class KafakConsumer<T extends Event> implements IConsumer<T>, Service {

    @Resource(name = "APP_HOME")
    protected File APP_HOME;

    public abstract String getGroupid();

    @Override
    public void init(AnyValue config) {
        new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(new File(APP_HOME, "conf/kafak.properties"));) {
                Properties props = new Properties();
                props.load(fis);
                props.put("group.id", getGroupid());
                KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
                consumer.subscribe(getSubscribes());

                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, String> record : records) {
                        String value = record.value();
                        try {
                            accept(value);
                        } catch (Exception e) {
                            logger.warning("event accept error :" + value);
                            e.printStackTrace();
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
