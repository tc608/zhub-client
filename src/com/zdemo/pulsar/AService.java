package com.zdemo.pulsar;

import com.zdemo.zhub.ZHubClient;
import org.redkale.net.http.RestMapping;
import org.redkale.net.http.RestService;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;
import org.redkale.util.Utility;

import javax.annotation.Resource;

@RestService
public class AService implements Service {

    @Resource(name = "zhub")
    private ZHubClient zhub;

    @Override
    public void init(AnyValue config) {
        zhub.timer("a", () -> {
            System.out.println(Utility.now() + " timer RANK-DATA-RELOADALL 执行了");
        });
    }

    @RestMapping
    public void x() {

    }
}
