package com.zdemo.test;

import com.zdemo.EventType;
import com.zdemo.kafak.KafakConsumer;
import org.redkale.convert.json.JsonConvert;
import org.redkale.util.TypeToken;

public class MyConsumer extends KafakConsumer {

    public String getGroupid() {
        return "group-test"; //消费组名称
    }

    {
        addEventType(
                EventType.of("a1", new TypeToken<Float>() {
                }, r -> {
                    System.out.println("我收到了消息 主题A 事件：" + JsonConvert.root().convertTo(r));
                })
        );
    }
}
