package com.zdemo.test;

import com.zdemo.Event;
import com.zdemo.kafak.KafakConsumer;
import org.redkale.convert.json.JsonConvert;
import org.redkale.util.TypeToken;

import java.util.Collection;
import java.util.List;

public class MyConsumer extends KafakConsumer<Event<String>> {

    public String getGroupid() {
        return "group-test"; //quest、user、im、live
    }

    @Override
    public Collection<String> getSubscribes() {
        return List.of("a", "b", "c", "vis-log");
    }

    @Override
    public TypeToken<Event<String>> getTypeToken() {
        return new TypeToken<Event<String>>() {
        };
    }

    @Override
    public void accept(Event<String> event) {
        switch (event.getTopic()) {
            case "a" -> System.out.println("我收到了消息 主题A 事件：" + JsonConvert.root().convertTo(event));
            case "b" -> System.out.println("我收到了消息 主题B 事件：" + JsonConvert.root().convertTo(event));
            case "c" -> System.out.println("我收到了消息 主题C 事件：" + JsonConvert.root().convertTo(event));
        }

    }
}
