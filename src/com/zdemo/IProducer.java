package com.zdemo;

import java.util.logging.Logger;

public interface IProducer {
    Logger logger = Logger.getLogger(IProducer.class.getSimpleName());

    @Deprecated
    <T extends Event> void send(T t);

    <V> void publish(String topic, V v);
}
