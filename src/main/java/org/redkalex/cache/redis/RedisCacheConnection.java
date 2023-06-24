/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redkalex.cache.redis;

import org.redkale.net.AsyncConnection;
import org.redkale.net.WorkThread;
import org.redkale.net.client.Client;
import org.redkale.net.client.ClientCodec;
import org.redkale.net.client.ClientConnection;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author zhangjx
 */
public class RedisCacheConnection extends ClientConnection<RedisCacheRequest, RedisCacheResult> {

    public RedisCacheConnection(Client client, int index, AsyncConnection channel) {
        super(client, index, channel);
    }

    @Override
    protected ClientCodec createCodec() {
        return new RedisCacheCodec(this);
    }

    protected CompletableFuture<RedisCacheResult> writeRequest(RedisCacheRequest request) {
        return super.writeChannel(request);
    }

    protected <T> CompletableFuture<T> writeRequest(RedisCacheRequest request, Function<RedisCacheResult, T> respTransfer) {
        return super.writeChannel(request, respTransfer);
    }

    public RedisCacheResult pollResultSet(RedisCacheRequest request) {
        RedisCacheResult rs = new RedisCacheResult();
        return rs;
    }

    public RedisCacheRequest pollRequest(WorkThread workThread) {
        RedisCacheRequest rs = new RedisCacheRequest().currThread(workThread);
        return rs;
    }

}
