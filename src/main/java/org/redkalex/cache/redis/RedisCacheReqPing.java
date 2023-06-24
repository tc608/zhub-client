/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.redkalex.cache.redis;

import org.redkale.net.client.ClientConnection;
import org.redkale.util.ByteArray;

import java.nio.charset.StandardCharsets;

/**
 * @author zhangjx
 */
public class RedisCacheReqPing extends RedisCacheRequest {

    private static final byte[] PS = "PING".getBytes(StandardCharsets.UTF_8);

    @Override
    public void writeTo(ClientConnection conn, ByteArray writer) {
        writer.put((byte) '*');
        writer.put((byte) '1');
        writer.put((byte) '\r', (byte) '\n');
        writer.put((byte) '$');
        writer.put((byte) '4');
        writer.put((byte) '\r', (byte) '\n');
        writer.put(PS);
        writer.put((byte) '\r', (byte) '\n');
    }
}
