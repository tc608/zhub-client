package com.zdemo.test;

import com.zdemo.Event;
import com.zdemo.redis.RedisProducer;
import org.junit.Test;
import org.redkale.boot.Application;

import java.util.Map;

/**
 * 消息发布订阅测试
 */
public class AppTest {

    @Test
    public void runConsumer() {
        try {
            // 启动并开启消费监听
            Application.singleton(MyConsumer.class);

            try {
                Thread.sleep(15_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runProducer() {
        try {
            RedisProducer producer = Application.singleton(RedisProducer.class);

            Event<Map> event = new Event<>();
            event.setTopic("c");
            event.setKey("abx");
            event.setValue(Map.of("A", "a"));

            producer.send(event);

            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
