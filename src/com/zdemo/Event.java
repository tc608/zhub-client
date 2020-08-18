package com.zdemo;

/**
 * 发布订阅 事件
 *
 * @param <V>
 */
public class Event<V> {
    private String topic;
    private String key;
    private V value;

    public Event() {
    }

    public Event(String topic, String key, V value) {
        this.topic = topic;
        this.key = key;
        this.value = value;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
