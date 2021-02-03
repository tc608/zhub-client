package com.zdemo.test;

import com.zdemo.zhub.ZHubClient;

public class MyConsumer extends ZHubClient {

    public String getGroupid() {
        return "group-test"; //消费组名称
    }

    @Override
    protected boolean preInit() {
        return true;
    }
}
