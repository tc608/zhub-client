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
public class RedisCacheReqAuth extends RedisCacheRequest {

    private static final byte[] PS = "AUTH".getBytes(StandardCharsets.UTF_8);

    protected String password;

    public RedisCacheReqAuth(String password) {
        this.password = password;
    }

    @Override
    public void writeTo(ClientConnection conn, ByteArray writer) {
        byte[] pwd = password.getBytes();
        writer.put((byte) '*');
        writer.put((byte) '2');
        writer.put((byte) '\r', (byte) '\n');
        writer.put((byte) '$');
        writer.put((byte) '4');
        writer.put((byte) '\r', (byte) '\n');
        writer.put(PS);
        writer.put((byte) '\r', (byte) '\n');

        writer.put((byte) '$');
        writer.put(String.valueOf(pwd.length).getBytes(StandardCharsets.UTF_8));
        writer.put((byte) '\r', (byte) '\n');
        writer.put(pwd);
        writer.put((byte) '\r', (byte) '\n');

    }
}
