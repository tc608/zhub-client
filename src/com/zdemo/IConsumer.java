package com.zdemo;

import org.redkale.util.TypeToken;

import java.util.Collection;
import java.util.logging.Logger;

public interface IConsumer<T extends Event> {
    Logger logger = Logger.getLogger(IConsumer.class.getSimpleName());

    Collection<String> getSubscribes();

    TypeToken<T> getTypeToken();

    void accept(T t);
}
