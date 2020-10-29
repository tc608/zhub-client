package com.zdemo.pulsar;

import com.zdemo.Event;
import com.zdemo.IProducer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.redkale.convert.json.JsonConvert;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;
import org.redkale.util.Comment;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PulsarProducer<T extends Event> implements IProducer<T>, Service {

    @Resource(name = "property.pulsar.serviceurl")
    private String serviceurl = "pulsar://127.0.0.1:6650";

    @Comment("消息生产者")
    private Map<String, Producer<byte[]>> producerMap = new HashMap();
    private PulsarClient client;

    @Override
    public void init(AnyValue config) {
        try {
            client = PulsarClient.builder()
                    .serviceUrl(serviceurl)
                    .build();
        } catch (PulsarClientException e) {
            e.printStackTrace();
        }
    }

    public Producer<byte[]> getProducer(String topic) {
        Producer<byte[]> producer = producerMap.get(topic);
        if (producer != null) {
            return producer;
        }

        synchronized (this) {
            if ((producer = producerMap.get(topic)) == null) {
                try {
                    producer = client.newProducer()
                            .topic(topic)
                            .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
                            .sendTimeout(10, TimeUnit.SECONDS)
                            .blockIfQueueFull(true)
                            .create();
                    producerMap.put(topic, producer);

                    return producer;
                } catch (PulsarClientException e) {
                    e.printStackTrace();
                }
            }
        }
        return producer;
    }

    @Override
    public void send(T t) {
        try {
            Producer<byte[]> producer = getProducer(t.topic);

            String v = JsonConvert.root().convertTo(t.value);
            if (v.startsWith("\"") && v.endsWith("\"")) {
                v = v.substring(1, v.length() - 1);
            }
            producer.newMessage()
                    .key("")
                    .value(v.getBytes())
                    .send();
        } catch (Exception e) {
            logger.log(Level.WARNING, "", e);
        }
    }
}
