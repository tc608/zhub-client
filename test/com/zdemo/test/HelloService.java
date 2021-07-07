package com.zdemo.test;

import com.zdemo.IConsumer;
import com.zdemo.zhub.RpcResult;
import com.zdemo.zhub.ZHubClient;
import org.redkale.net.http.RestService;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;
import org.redkale.util.TypeToken;

import javax.annotation.Resource;

@RestService(automapping = true)
public class HelloService implements Service {

    @Resource(name = "zhub")
    private ZHubClient zhub;

    @Override
    public void init(AnyValue config) {
        // Function<Rpc<T>, RpcResult<R>> fun
        zhub.rpcSubscribe("x", new TypeToken<String>() {
        }, r -> {

            return r.buildResp(r.getValue().toUpperCase() + ": Ok");
        });
    }

    public RpcResult<String> x(String v) {
        if (v == null) {
            v = "";
        }
        RpcResult<String> x = zhub.rpc("x", v, IConsumer.TYPE_TOKEN_STRING).join();

        return x;
    }
}
