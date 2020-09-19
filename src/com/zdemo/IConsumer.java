package com.zdemo;

import org.redkale.util.TypeToken;

import java.util.Collection;
import java.util.logging.Logger;

public interface IConsumer<T extends Event> {
    TypeToken TYPE_TOKEN_STRING = new TypeToken<String>() {
    };
    TypeToken<Integer> TYPE_TOKEN_INT = new TypeToken<Integer>() {
    };

    Logger logger = Logger.getLogger(IConsumer.class.getSimpleName());

    Collection<String> getSubscribes();

    <T> void accept(String topic, String record);
}
