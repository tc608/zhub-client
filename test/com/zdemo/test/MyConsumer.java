package com.zdemo.test;

import com.zdemo.EventType;
import com.zdemo.kafak.KafakConsumer;
import org.redkale.convert.json.JsonConvert;
import org.redkale.util.TypeToken;

public class MyConsumer extends KafakConsumer {

    public String getGroupid() {
        return "group-test"; //消费组名称
    }

    @Override
    public void preInit() {
        addEventType(
                EventType.of("a1", new TypeToken<Float>() {
                }, r -> {
                    System.out.println("我收到了消息 主题a1 事件：" + JsonConvert.root().convertTo(r));
                }),

                EventType.of("bx", str -> {
                    System.out.println("我收到了消息 主题bx 事件：" + str);
                })
        );
    }
}
