package com.zdemo;

import org.redkale.util.TypeToken;

import java.util.Collection;

public interface IConsumer<T> {

    Collection<String> getSubscribes();

    TypeToken<T> getTypeToken();

    void accept(T t);
}
