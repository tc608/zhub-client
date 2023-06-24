package net.tccn;

import java.util.logging.Logger;

public interface IProducer {
    Logger logger = Logger.getLogger(IProducer.class.getSimpleName());

    boolean publish(String topic, Object v);
}
