package com.zdemo.test;

import com.zdemo.zdb.ZHubConsumer;

public class MyConsumer extends ZHubConsumer {

    public String getGroupid() {
        return "group-test"; //消费组名称
    }

    @Override
    public boolean preInit() {
        return true;
    }
}
