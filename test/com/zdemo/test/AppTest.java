package com.zdemo.test;

import com.zdemo.Event;
import com.zdemo.EventType;
import com.zdemo.IConsumer;
import com.zdemo.IProducer;
import com.zdemo.pulsar.PulsarProducer;
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
            IConsumer consumer = Application.singleton(MyConsumer.class);

            consumer.addEventType(
                    EventType.of("a1", new TypeToken<Float>() {
                    }, r -> {
                        System.out.println("我收到了消息 主题a1 事件：" + JsonConvert.root().convertTo(r));
                    })

                    , EventType.of("game-update", str -> {
                        System.out.println("我收到了消息 主题game-update 事件：" + str);
                    })

                    , EventType.of("http.req.hello", str -> {
                        System.out.println("我收到了消息 主题http.req.hello 事件：" + str);
                    })

                    , EventType.of("http.resp.node2004", str -> {
                        System.out.println("我收到了消息 主题http.resp.node2004 事件：" + str);
                    })
            );

            // 10s 后加入 bx主题
            Thread.sleep(1_000 * 10);
            System.out.println("加入新的主题订阅");
            consumer.addEventType(
                    EventType.of("bx", str -> {
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
            IProducer producer = Application.singleton(PulsarProducer.class);

            // 发送不同的 事件
            float v0 = 1f;
            Map v1 = Map.of("k", "v");
            List v2 = asList(1, 2, 3);

            //producer.send(Event.of("a1", v0));
            /*producer.send(Event.of("b1", v1));
            producer.send(Event.of("c1", v2));*/

            producer.send(Event.of("game-update", 23256));
            producer.send(Event.of("bx", 23256));

            /*try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
