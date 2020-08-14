package com.zdemo.test;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event<V> {
    private String key;
    private V value;
}
