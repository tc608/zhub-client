package com.zdemo.kafak;

import com.zdemo.IConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.redkale.convert.json.JsonConvert;
import org.redkale.net.http.RestService;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * 消费
 *
 * @param <T>
 */
@RestService
public abstract class KafakConsumer<T> implements IConsumer<T>, Service {

    protected final Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    private String kafakServices = "122.112.180.156:6062";
    private KafkaConsumer<String, String> consumer;

    @Override
    public void init(AnyValue config) {
        CompletableFuture.runAsync(() -> {
            Properties props = new Properties();
            props.put("bootstrap.servers", kafakServices);
            props.put("group.id", "test");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("key.deserializer", StringDeserializer.class.getName());
            props.put("value.deserializer", StringDeserializer.class.getName());

            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(getSubscribes());
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        logger.finest(String.format("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value()));

                        T t = JsonConvert.root().convertFrom(getTypeToken().getType(), record.value());
                        accept(t);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
