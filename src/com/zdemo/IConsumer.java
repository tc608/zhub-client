package com.zdemo;

import org.redkale.util.TypeToken;

import java.util.Collection;
import java.util.function.Consumer;

public interface IConsumer {
    TypeToken<String> TYPE_TOKEN_STRING = new TypeToken<String>() {
    };
    TypeToken<Integer> TYPE_TOKEN_INT = new TypeToken<Integer>() {
    };

    Collection<String> getTopics();

    void addEventType(EventType... eventType);

    void accept(String topic, String record);

    /**
     * 取消订阅
     *
     * @param topic
     */
    void unsubscribe(String topic);

    void subscribe(String topic, Consumer<String> consumer);
    
    <T> void subscribe(String topic, TypeToken<T> typeToken, Consumer<T> consumer);
}
