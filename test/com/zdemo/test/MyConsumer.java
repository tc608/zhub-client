package com.zdemo.test;

import com.zdemo.Event;
import com.zdemo.redis.RedisConsumer;
import org.redkale.convert.json.JsonConvert;
import org.redkale.util.TypeToken;

import java.util.Collection;
import java.util.List;

public class MyConsumer extends RedisConsumer<Event<Integer>> {

    public String getGroupid() {
        return "group-test"; //quest、user、im、live
    }

    @Override
    public Collection<String> getSubscribes() {
        return List.of("a", "b", "c");
    }

    @Override
    public TypeToken<Event<Integer>> getTypeToken() {
        return new TypeToken<Event<Integer>>() {
        };
    }

    @Override
    public void accept(Event<Integer> event) {
        switch (event.getTopic()) {
            case "a" -> System.out.println("我收到了消息 主题A 事件：" + JsonConvert.root().convertTo(event));
            case "b" -> System.out.println("我收到了消息 主题B 事件：" + JsonConvert.root().convertTo(event));
            case "c" -> System.out.println("我收到了消息 主题C 事件：" + JsonConvert.root().convertTo(event));
        }

    }
}
