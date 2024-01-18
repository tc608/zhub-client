package net.tccn;

/**
 * 发布订阅 事件
 *
 * @param <V>
 */
public class Event<V> {
    public final String topic;
    //public final String key;
    public final V value;

    private Event(String topic, V value) {
        this.topic = topic;
        this.value = value;
    }

    public static <V> Event<V> of(String topic, V value) {
        return new Event<>(topic, value);
    }


}
