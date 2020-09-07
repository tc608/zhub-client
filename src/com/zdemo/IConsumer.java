package com.zdemo;

import java.util.Collection;
import java.util.logging.Logger;

public interface IConsumer<T extends Event> {
    Logger logger = Logger.getLogger(IConsumer.class.getSimpleName());

    Collection<String> getSubscribes();

    <T> void accept(String topic, String record);
}
