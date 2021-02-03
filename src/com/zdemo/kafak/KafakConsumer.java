package com.zdemo.kafak;

import com.zdemo.AbstractConsumer;
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
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 消费
 */
@RestService
public abstract class KafakConsumer extends AbstractConsumer implements IConsumer, Service {

    public Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Resource(name = "APP_HOME")
    protected File APP_HOME;

    private final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    @Override
    public final void init(AnyValue config) {
        if (!preInit()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(new File(APP_HOME, "conf/kafak.properties"));) {
            Properties props = new Properties();
            props.load(fis);

            new Thread(() -> {
                try {
                    props.put("group.id", getGroupid());
                    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
                    consumer.subscribe(getTopics());
                    while (true) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1_000));
                        records.forEach(record -> {
                            String topic = record.topic();
                            long offset = record.offset();
                            String value = record.value();
                            try {
                                accept(topic, value);
                            } catch (Exception e) {
                                logger.log(Level.WARNING, String.format("topic[%s] event accept error, offset=%s,value:%s", topic, offset, value), e);
                            }
                        });

                        if (!queue.isEmpty()) {
                            Runnable runnable;
                            while ((runnable = queue.poll()) != null) {
                                runnable.run();
                            }

                            consumer.unsubscribe();
                            consumer.subscribe(getTopics());
                        }
                    }
                } catch (WakeupException ex) {
                    System.out.println("WakeupException !!!!");
                }

            }, "thread-consumer-[" + getGroupid() + "]").start();
        } catch (IOException e) {
            logger.log(Level.WARNING, "", e);
        }
    }

    @Override
    public void unsubscribe(String topic) {
        queue.add(() -> super.removeEventType(topic)); // 加入延时执行队列（下一次订阅变更检查周期执行）
    }

    @Override
    protected void subscribe(String topic) {
        queue.add(() -> {
            // just set flag, nothing to do
        });
    }
}
