package net.tccn.zhub;

public class RpcResult<R> {
    private String ruk;
    private int retcode;
    private String retinfo;
    private R result;

    public String getRuk() {
        return ruk;
    }

    public void setRuk(String ruk) {
        this.ruk = ruk;
    }

    public int getRetcode() {
        return retcode;
    }

    public void setRetcode(int retcode) {
        this.retcode = retcode;
    }

    public String getRetinfo() {
        return retinfo;
    }

    public void setRetinfo(String retinfo) {
        this.retinfo = retinfo;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
        this.result = result;
    }
}
