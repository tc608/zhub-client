package com.zdemo.pulsar;

import com.zdemo.AbstractConsumer;
import com.zdemo.EventType;
import com.zdemo.IConsumer;
import org.apache.pulsar.client.api.*;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public abstract class PulsarConsumer extends AbstractConsumer implements IConsumer, Service {

    @Resource(name = "property.pulsar.serviceurl")
    private String serviceurl = "pulsar://127.0.0.1:6650";
    private PulsarClient client;
    private Consumer consumer;

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
            logger.log(Level.WARNING, "", e);
        }
    }

    @Override
    public void init(AnyValue config) {
        if (!preInit()) {
            return;
        }
        new Thread(() -> {
            try {
                client = PulsarClient.builder()
                        .serviceUrl(serviceurl)
                        .build();

                consumer = client.newConsumer()
                        .topics(new ArrayList<>(getTopics()))
                        .subscriptionName(getGroupid())
                        .subscriptionType(SubscriptionType.Shared)
                        .subscribe();

                while (true) {
                    // 动态新增订阅
                    while (!queue.isEmpty()) {
                        queue.clear();
                        consumer.unsubscribe();
                        consumer = client.newConsumer()
                                .topics(new ArrayList<>(getTopics()))
                                .subscriptionName(getGroupid())
                                .subscriptionType(SubscriptionType.Shared)
                                .subscribe();
                    }

                    // Wait for a message
                    Message msg = consumer.receive(10, TimeUnit.SECONDS);
                    if (msg == null) {
                        continue;
                    }

                    String topic = msg.getTopicName().replace("persistent://public/default/", "");
                    long offset = 0;
                    String value = new String(msg.getData());
                    try {
                        accept(topic, value);

                        consumer.acknowledge(msg); // Acknowledge the message so that it can be deleted by the message broker
                    } catch (Exception e) {
                        logger.log(Level.WARNING, String.format("topic[%s] event accept error, offset=%s,value:%s", topic, offset, value), e);
                        consumer.negativeAcknowledge(msg); // Message failed to process, redeliver later
                    }
                }
            } catch (PulsarClientException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void destroy(AnyValue config) {

    }
}
