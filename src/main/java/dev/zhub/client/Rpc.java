package dev.zhub.client;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Rpc<T> {
    /*public final static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();*/
    public final static Gson gson = new Gson();

    private String ruk;     // request unique key:
    private String topic;   // call topic
    private T value;        // call paras

    @Expose(deserialize = false, serialize = false)
    private RpcResult rpcResult;

    @Expose(deserialize = false, serialize = false)
    private TypeToken typeToken;

    /*public Rpc() {
    }*/

    protected Rpc(String appname, String topic, T value) {
        this.ruk = appname + "::" + UUID.randomUUID().toString().replaceAll("-", "");
        this.topic = topic;
        this.value = value;
    }

    public String getBackTopic() {
        return ruk.split("::")[0];
    }

    public <R> RpcResult<R> render() {
        RpcResult<R> response = new RpcResult<>();
        response.setRuk(ruk);
        return response;
    }

    public <R> RpcResult<R> render(R result) {
        RpcResult<R> response = new RpcResult<>();
        response.setRuk(ruk);
        response.setResult(result);
        return response;
    }

    public <R> RpcResult<R> retError(String retinfo) {
        RpcResult<R> response = new RpcResult<>();
        response.setRuk(ruk);
        response.setRetcode(100);
        response.setRetinfo(retinfo);
        return response;
    }

    public <R> RpcResult<R> retError(int retcode, String retinfo) {
        RpcResult<R> response = new RpcResult<>();
        response.setRuk(ruk);
        response.setRetcode(retcode);
        response.setRetinfo(retinfo);
        return response;
    }
}
