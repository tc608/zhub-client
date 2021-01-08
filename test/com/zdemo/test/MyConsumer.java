package com.zdemo.test;

import com.zdemo.zdb.ZdbConsumer;

public class MyConsumer extends ZdbConsumer {

    public String getGroupid() {
        return "group-test"; //消费组名称
    }

    @Override
    public boolean preInit() {
        return true;
    }
}
