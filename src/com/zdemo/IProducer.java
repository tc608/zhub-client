package com.zdemo;

import java.util.concurrent.CompletableFuture;

public interface IProducer<T extends Event> {


    default CompletableFuture sendAsync(String topic, T... t) {
        return CompletableFuture.runAsync(() -> send(t));
    }

    void send(T... t);

}
