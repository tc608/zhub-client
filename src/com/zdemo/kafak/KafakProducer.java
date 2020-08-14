package com.zdemo.kafak;

import com.zdemo.IProducer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.redkale.convert.json.JsonConvert;
import org.redkale.net.http.RestService;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import java.util.Properties;

/**
 * 生产
 *
 * @param <T>
 */
@RestService
public class KafakProducer<T> implements IProducer<T>, Service {

    private String kafakServers = "122.112.180.156:6062";
    private KafkaProducer<String, String> producer;

    @Override
    public void init(AnyValue config) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafakServers);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer(props);
    }

    @Override
    public void send(String topic, T... t) {
        for (T t1 : t) {
            producer.send(new ProducerRecord(topic, JsonConvert.root().convertTo(t1)));
        }
    }

    @Override
    public void destroy(AnyValue config) {
        producer.close();
    }
}
