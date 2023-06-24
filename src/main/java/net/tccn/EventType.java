package net.tccn;

import org.redkale.util.TypeToken;

import java.util.function.Consumer;

public class EventType<T> {
    public final String topic;
    public final TypeToken<T> typeToken;
    private final Consumer<T> consumer;

    private final static TypeToken<String> stringToken = new TypeToken<>() {
    };

    private EventType(String topic, TypeToken<T> typeToken, Consumer<T> consumer) {
        this.topic = topic;
        this.typeToken = typeToken;
        this.consumer = consumer;
    }

    public static <T> EventType of(String topic, TypeToken<T> typeToken, Consumer<T> consumer) {
        return new EventType<>(topic, typeToken, consumer);
    }

    public static EventType of(String topic, Consumer<String> consumer) {
        return new EventType(topic, stringToken, consumer);
    }

    public void accept(T t) {
        consumer.accept(t);
    }
}
