package com.zdemo.test;

import com.zdemo.kafak.KafakConsumer;

public class MyConsumer extends KafakConsumer {

    public String getGroupid() {
        return "group-test"; //消费组名称
    }

    @Override
    public boolean preInit() {
        return true;
    }
}
