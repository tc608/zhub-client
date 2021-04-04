package com.zdemo.zhub;

import org.redkale.convert.ConvertColumn;
import org.redkale.convert.json.JsonConvert;

public class Rpc<T> {
    private String ruk;     // request unique key:
    private String topic;   // call topic
    private T value;        // call paras

    private RpcResponse response;

    public Rpc() {
    }

    public Rpc(String appname, String ruk, String topic, Object value) {
        this.ruk = appname + "::" + ruk;
        this.topic = topic;
        this.value = (T) JsonConvert.root().convertTo(value);
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

    public <R> RpcResponse<R> getResponse() {
        return response;
    }

    public void setResponse(RpcResponse response) {
        this.response = response;
    }

    @ConvertColumn(ignore = true)
    public String getBackTopic() {
        return ruk.split("::")[0];
    }

    public <R> RpcResponse<R> buildResp() {
        RpcResponse<R> response = new RpcResponse<>();
        response.setRuk(ruk);
        return response;
    }

    public <R> RpcResponse<R> buildResp(int retcode, String retinfo) {
        RpcResponse<R> response = new RpcResponse<>();
        response.setRuk(ruk);
        response.setRetcode(retcode);
        response.setRetinfo(retinfo);
        return response;
    }

    public <R> RpcResponse<R> buildError(String retinfo) {
        RpcResponse<R> response = new RpcResponse<>();
        response.setRuk(ruk);
        response.setRetcode(100);
        response.setRetinfo(retinfo);
        return response;
    }

    public <R> RpcResponse<R> buildResp(R result) {
        RpcResponse<R> response = new RpcResponse<>();
        response.setRuk(ruk);
        response.setResult(result);
        return response;
    }

    @ConvertColumn(ignore = true)
    public int getRetcode() {
        if (this.response == null) {
            return -1;
        }

        return response.getRetcode();
    }

    @ConvertColumn(ignore = true)
    public String getRetinfo() {
        if (this.response == null) {
            return "";
        }

        return response.getRetinfo();
    }
}
