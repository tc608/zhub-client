package com.zdemo.test;

import com.zdemo.pulsar.PulsarConsumer;

public class MyConsumer extends PulsarConsumer {

    public String getGroupid() {
        return "group-test"; //消费组名称
    }

    @Override
    public boolean preInit() {
        return true;
    }
}
