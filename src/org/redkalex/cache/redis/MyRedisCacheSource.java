package org.redkalex.cache.redis;

import org.redkale.annotation.AutoLoad;
import org.redkale.annotation.ResourceType;
import org.redkale.service.Local;
import org.redkale.source.CacheSource;
import org.redkale.util.AnyValue;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Local
@AutoLoad(false)
@ResourceType(CacheSource.class)
public class MyRedisCacheSource extends RedisCacheSource {

    @Override
    public void init(AnyValue conf) {
        super.init(conf);
    }

    //--------------------- bit ------------------------------
    public boolean getBit(String key, int offset) {
        return sendAsync("GETBIT", key, key.getBytes(StandardCharsets.UTF_8), String.valueOf(offset).getBytes(StandardCharsets.UTF_8)).thenApply(v -> v.getIntValue(0) > 0).join();
    }

    public void setBit(String key, int offset, boolean bool) {
        sendAsync("SETBIT", key, offset, bool ? 1 : 0).join();
    }
    //--------------------- bit ------------------------------

    //--------------------- lock ------------------------------
    // 尝试加锁，成功返回0，否则返回上一锁剩余毫秒值
    public long tryLock(String key, int millis) {
        Serializable[] obj = {"" +
                "if (redis.call('EXISTS',KEYS[1]) == 0) then " +
                "redis.call('PSETEX',KEYS[1],ARGV[1],1); " +
                "return 0; " +
                "else " +
                "return redis.call('PTTL',KEYS[1]); " +
                "end;", 1, key, millis
        };

        return sendAsync("EVAL", null, obj).thenApply(v -> v.getIntValue(1)).join();
    }

    // 加锁
    public void lock(String key, int millis) {
        long i;
        do {
            i = tryLock(key, millis);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (i > 0);
    }

    // 解锁
    public void unlock(String key) {
        remove(key);
    }

    //--------------------- key ------------------------------

    public String get(String key) {
        return get(key, String.class);
    }

    //--------------------- set ------------------------------
    /*public <T> void sadd(String key, Collection<T> args) {
        saddAsync(key, args.toArray(Serializable[]::new)).join();
    }*/

    public void sadd(String key, Serializable... args) {
        saddAsync(key, Arrays.stream(args).toArray(Serializable[]::new)).join();
    }

    public void srem(String key, Serializable... args) {
        sremAsync(key, args).join();
    }

    public CompletableFuture<RedisCacheResult> saddAsync(String key, Serializable... args) {
        return sendAsync("SADD", key, args);
    }

    public CompletableFuture<RedisCacheResult> sremAsync(String key, Serializable... args) {
        return sendAsync("SREM", key, args);
    }

    //--------------------- hm ------------------------------
    public void setHms(String key, Map kv) {
        setHmsAsync(key, kv).join();
    }

    public CompletableFuture<RedisCacheResult> setHmsAsync(String key, Map<Serializable, Serializable> kv) {
        List<Serializable> args = new ArrayList();
        kv.forEach((k, v) -> {
            args.add(k);
            args.add(v);
        });

        return sendAsync("HMSET", key, args.toArray(Serializable[]::new));
    }

    public String getHm(String key, String field) {
        return getHm(key, String.class, field);
    }

    public <T extends Serializable> T getHm(String key, Class<T> type, String field) {
        List<Serializable> list = super.hmget(key, type, field);
        if (list == null && list.isEmpty()) {
            return null;
        }
        return (T) list.get(0);
    }

    public Map<String, String> getHms(String key, String... field) {
        return getHms(key, String.class, field);
    }

    public <T extends Serializable> Map<String, T> getHms(String key, Class<T> type, String... field) {
        List<Serializable> list = super.hmget(key, type, field);
        if (list == null && list.isEmpty()) {
            return null;
        }
        Map<String, T> map = new HashMap<>(field.length);

        for (int i = 0; i < field.length; i++) {
            map.put(field[i], (T) list.get(i));
        }
        return map;
    }
}
