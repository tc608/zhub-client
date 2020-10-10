package com.zdemo;

import java.util.logging.Logger;

public interface IProducer<T extends Event> {
    Logger logger = Logger.getLogger(IProducer.class.getSimpleName());

    void send(T t);

}
