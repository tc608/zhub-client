package com.zdemo;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public interface IProducer<T extends Event> {
    Logger logger = Logger.getLogger(IProducer.class.getSimpleName());

    default CompletableFuture sendAsync(T... t) {
        return CompletableFuture.runAsync(() -> send(t));
    }

    void send(T... t);

}
