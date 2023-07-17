package tccn.zhub;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

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

    public Rpc() {
    }

    public Rpc(String appname, String ruk, String topic, Object value) {
        this.ruk = appname + "::" + ruk;
        this.topic = topic;
        this.value = (T) gson.toJson(value);
    }

    public String getRuk() {
        return ruk;
    }

    public void setRuk(String ruk) {
        this.ruk = ruk;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }


    public RpcResult getRpcResult() {
        return rpcResult;
    }

    public void setRpcResult(RpcResult rpcResult) {
        this.rpcResult = rpcResult;
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
