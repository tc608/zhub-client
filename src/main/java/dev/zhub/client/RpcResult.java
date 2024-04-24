package dev.zhub.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcResult<R> {
    private String ruk;
    private int retcode;
    private String retinfo;
    private R result;
}
