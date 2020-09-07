package com.zdemo.test;

import com.zdemo.Event;
import com.zdemo.EventType;
import com.zdemo.kafak.KafakProducer;
import org.junit.Test;
import org.redkale.boot.Application;
import org.redkale.convert.json.JsonConvert;
import org.redkale.util.TypeToken;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * 消息发布订阅测试
 */
public class AppTest {

    @Test
    public void runConsumer() {
        try {
            //启动并开启消费监听
            MyConsumer consumer = Application.singleton(MyConsumer.class);

            //新增订阅主题 a1
            consumer.addEventType(EventType.of("a1", new TypeToken<Float>() {
            }, r -> {
                System.out.println("我收到了消息 主题A 事件：" + JsonConvert.root().convertTo(r));
            }));

            Thread.sleep(5_000);

            //新增订阅主题 b1、c1
            consumer.addEventType(
                    // 订阅主题 b1
                    EventType.of("b1", new TypeToken<Map<String, String>>() {
                    }, r -> {
                        System.out.println("我收到了消息 主题B 事件：" + JsonConvert.root().convertTo(r));
                    }),

                    // 订阅主题 c1
                    EventType.of("c1", new TypeToken<List<Integer>>() {
                    }, r -> {
                        System.out.println("我收到了消息 主题C 事件：" + JsonConvert.root().convertTo(r));
                    })
            );

            Thread.sleep(60_000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runProducer() {
        try {
            KafakProducer producer = Application.singleton(KafakProducer.class);

            // 发送不同的 事件
            float v0 = 1f;
            Map v1 = Map.of("k", "v");
            List v2 = asList(1, 2, 3);

            producer.send(Event.of("a1", v0));
            producer.send(Event.of("b1", v1));
            producer.send(Event.of("c1", v2));


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
