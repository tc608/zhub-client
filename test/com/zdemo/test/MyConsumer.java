package com.zdemo.test;

import com.zdemo.kafak.KafakConsumer;
import com.zdemo.kafak.KafakProducer;
import org.junit.Test;
import org.redkale.util.TypeToken;

import java.util.Collection;
import java.util.List;

public class MyConsumer extends KafakConsumer<Event<Integer>> {

    @Override
    public Collection<String> getSubscribes() {
        return List.of("a");
    }

    @Override
    public TypeToken<Event<Integer>> getTypeToken() {
        return new TypeToken<Event<Integer>>() {
        };
    }

    @Override
    public void accept(Event<Integer> event) {
        System.out.println("我收到了消息 key：" + event.getKey() + " value:" + event.getValue());
    }

    @Test
    public void run() {
        MyConsumer consumer = new MyConsumer();
        consumer.init(null);

        try {
            Thread.sleep(15_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runProducer() {
        KafakProducer<Event> producer = new KafakProducer();
        producer.init(null);

        Event<Integer> event = new Event<>();
        event.setKey("XXX");
        event.setValue(2314);

        producer.send("a", event);

        producer.destroy(null);
    }
}
