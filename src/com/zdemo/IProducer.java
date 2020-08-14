package com.zdemo;

import java.util.concurrent.CompletableFuture;

public interface IProducer<T> {


    default CompletableFuture sendAsync(String topic,T... t) {
        return CompletableFuture.runAsync(() -> send(topic, t));
    }

    void send(String topic,T... t);

}
