package com.zdemo.test;

import com.zdemo.Event;
import com.zdemo.EventType;
import com.zdemo.IConsumer;
import com.zdemo.IProducer;
import com.zdemo.kafak.KafakProducer;
import org.junit.Test;
import org.redkale.boot.Application;

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
            IConsumer consumer = Application.singleton(MyConsumer.class);

            consumer.addEventType(
                    EventType.of("a", str -> {
                        System.out.println("我收到了消息 a 事件：" + str);
                    })

                    , EventType.of("bx", str -> {
                        System.out.println("我收到了消息 主题bx 事件：" + str);
                    })
            );


            Thread.sleep(60_000 * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runProducer() {
        try {
            IProducer producer = Application.singleton(KafakProducer.class);

            // 发送不同的 事件
            float v0 = 1f;
            Map v1 = Map.of("k", "v");
            List v2 = asList(1, 2, 3);

            //producer.send(Event.of("a1", v0));
            /*producer.send(Event.of("b1", v1));
            producer.send(Event.of("c1", v2));*/

            /*producer.send(Event.of("game-update", 23256));
            producer.send(Event.of("bx", 23256));*/
            for (int i = 0; i < 5; i++) {
                producer.send(Event.of("a", i + ""));
            }

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
