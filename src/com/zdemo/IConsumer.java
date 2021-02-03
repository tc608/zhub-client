package com.zdemo;

import org.redkale.util.TypeToken;

import java.util.function.Consumer;

public interface IConsumer {
    TypeToken<String> TYPE_TOKEN_STRING = new TypeToken<String>() {
    };
    TypeToken<Integer> TYPE_TOKEN_INT = new TypeToken<Integer>() {
    };

    /**
     * 取消订阅
     *
     * @param topic
     */
    void unsubscribe(String topic);

    /**
     * 订阅， 接收数据类型 String
     *
     * @param topic
     * @param consumer
     */
    void subscribe(String topic, Consumer<String> consumer);

    /**
     * 订阅，接收类型为 <T>
     *
     * @param topic
     * @param typeToken
     * @param consumer
     * @param <T>
     */
    <T> void subscribe(String topic, TypeToken<T> typeToken, Consumer<T> consumer);
}
