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
            Application.singleton(MyConsumer.class);
            Thread.sleep(60_000 * 60);
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
