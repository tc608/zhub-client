package net.tccn.zhub;

import org.redkale.convert.ConvertColumn;
import org.redkale.service.RetResult;

public class Rpc<T> {
    private String ruk;     // request unique key:
    private String topic;   // call topic
    private T value;        // call paras

    private RpcResult rpcResult;

    public Rpc() {
    }

    protected Rpc(String appname, String ruk, String topic, T value) {
        this.ruk = appname + "::" + ruk;
        this.topic = topic;
        this.value = value;
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

    @ConvertColumn(ignore = true)
    public RpcResult getRpcResult() {
        return rpcResult;
    }

    public void setRpcResult(RpcResult rpcResult) {
        this.rpcResult = rpcResult;
    }

    @ConvertColumn(ignore = true)
    public String getBackTopic() {
        return ruk.split("::")[0];
    }

    public <R> RpcResult<R> render(int retcode, String retinfo) {
        RpcResult<R> response = new RpcResult<>();
        response.setRuk(ruk);
        response.setRetcode(retcode);
        response.setRetinfo(retinfo);
        return response;
    }

    public <R> RpcResult<R> render() {
        RpcResult<R> response = new RpcResult<>();
        response.setRuk(ruk);
        return response;
    }

    public RpcResult render(RetResult result) {
        RpcResult resp = new RpcResult<>();
        resp.setRuk(ruk);
        resp.setRetcode(result.getRetcode());
        resp.setRetinfo(result.getRetinfo());
        resp.setResult(result.getResult());
        return resp;
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
