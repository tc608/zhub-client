package com.zdemo.kafak;

import com.zdemo.AbstractConsumer;
import com.zdemo.EventType;
import com.zdemo.IConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

import static java.util.Arrays.asList;

/**
 * 消费
 */
@RestService
public abstract class KafakConsumer extends AbstractConsumer implements IConsumer, Service {

    @Resource(name = "APP_HOME")
    protected File APP_HOME;

    protected Properties props;

    // 0:none 1:restart -1:stop
    //private int cmd = -1;

    public abstract String getGroupid();

    private final LinkedBlockingQueue<EventType> queue = new LinkedBlockingQueue<>();

    @Override
    public void addEventType(EventType... eventTypes) {
        super.addEventType(eventTypes);
        try {
            for (EventType eventType : eventTypes) {
                queue.put(eventType);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final void init(AnyValue config) {
        if (!preInit()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(new File(APP_HOME, "conf/kafak.properties"));) {
            props = new Properties();
            props.load(fis);

            if (logger.isLoggable(Level.INFO)) logger.info(getGroupid() + " consumer started!");

            new Thread(() -> {
                try {
                    props.put("group.id", getGroupid());
                    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
                    consumer.subscribe(asList("_"));
                    while (true) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10_000));
                        records.forEach(record -> {
                            String topic = record.topic();
                            long offset = record.offset();
                            String value = record.value();
                            try {
                                accept(topic, value);
                            } catch (Exception e) {
                                logger.warning(String.format("topic[%s] event accept error, offset=%s,value:%s", topic, offset, value));
                                e.printStackTrace();
                            }
                        });

                        // 动态新增订阅
                        while (!queue.isEmpty()) {
                            queue.clear();
                            consumer.unsubscribe();
                            consumer.subscribe(getSubscribes());
                        }
                    }
                } catch (WakeupException ex) {
                    System.out.println("WakeupException !!!!");
                }

            }, "thread-consumer-[" + getGroupid() + "]").start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
