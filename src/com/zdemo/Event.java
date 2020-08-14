package com.zdemo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event<V> {
    private String topic;
    private String key;
    private V value;
}
