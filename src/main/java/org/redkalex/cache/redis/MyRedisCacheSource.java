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
/*
//--------------------- zset ------------------------------

    public int getZrank(String key, V v) {
        byte[][] bytes = Stream.of(key, v).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        Long t = (Long) send("ZRANK", CacheEntryType.OBJECT, (Type) null, key, bytes).join();

        return t == null ? -1 : (int) (long) t;
    }

    public int getZrevrank(String key, V v) {
        byte[][] bytes = Stream.of(key, v).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        Long t = (Long) send("ZREVRANK", CacheEntryType.OBJECT, (Type) null, key, bytes).join();

        return t == null ? -1 : (int) (long) t;
    }

    //ZRANGE/ZREVRANGE key start stop
    public List<V> getZset(String key) {
        byte[][] bytes = Stream.of(key, 0, -1).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        List<V> vs = (List<V>) send("ZREVRANGE", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
        List<V> vs2 = new ArrayList(vs.size());

        for (int i = 0; i < vs.size(); ++i) {
            if (i % 2 == 1) {
                vs2.add(this.convert.convertFrom(this.objValueType, String.valueOf(vs.get(i))));
            } else {
                vs2.add(vs.get(i));
            }
        }

        return vs2;
    }

    public List<V> getZset(String key, int offset, int limit) {
        byte[][] bytes = Stream.of(key, offset, offset + limit - 1).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        List<V> vs = (List<V>) send("ZREVRANGE", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
        List<V> vs2 = new ArrayList(vs.size());

        for (int i = 0; i < vs.size(); ++i) {
            if (i % 2 == 1) {
                vs2.add(this.convert.convertFrom(this.objValueType, String.valueOf(vs.get(i))));
            } else {
                vs2.add(vs.get(i));
            }
        }

        return vs2;
    }

    public LinkedHashMap<V, Long> getZsetLongScore(String key) {
        LinkedHashMap<V, Double> map = getZsetDoubleScore(key);
        if (map.isEmpty()) {
            return new LinkedHashMap<>();
        }

        LinkedHashMap<V, Long> map2 = new LinkedHashMap<>(map.size());
        map.forEach((k, v) -> map2.put(k, (long) (double) v));
        return map2;
    }

    public LinkedHashMap<V, Long> getZsetItemsLongScore(String key) {
        byte[][] bytes = Stream.of(key, 0, -1, "WITHSCORES").map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        List vs = (List) send("ZRANGE", CacheEntryType.OBJECT, (Type) null, key, bytes).join();

        LinkedHashMap<V, Long> map = new LinkedHashMap<>();
        for (int i = 0; i < vs.size(); i += 2) {
            map.put((V) vs.get(i), (long) Double.parseDouble((String) vs.get(i + 1)));
        }
        return map;
    }

    public Long getZsetLongScore(String key, V v) {
        Double score = getZsetDoubleScore(key, v);
        if (score == null) {
            return null;
        }
        return (long) (double) score;
    }

    public LinkedHashMap<V, Double> getZsetDoubleScore(String key) {
        byte[][] bytes = Stream.of(key, 0, -1, "WITHSCORES").map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        List vs = (List) send("ZREVRANGE", CacheEntryType.OBJECT, (Type) null, key, bytes).join();

        LinkedHashMap<V, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < vs.size(); i += 2) {
            map.put((V) vs.get(i), Double.parseDouble((String) vs.get(i + 1)));
        }
        return map;
    }

    public Double getZsetDoubleScore(String key, V v) {
        byte[][] bytes = Stream.of(key, v).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        Serializable zscore = send("ZSCORE", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
        if (zscore == null) {
            return null;
        }

        return Double.parseDouble(String.valueOf(zscore));
    }

    public LinkedHashMap<V, Long> getZsetLongScore(String key, int offset, int limit) {
        byte[][] bytes = Stream.of(key, offset, offset + limit - 1, "WITHSCORES").map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        List vs = (List) send("ZREVRANGE", CacheEntryType.OBJECT, (Type) null, key, bytes).join();

        LinkedHashMap<V, Long> map = new LinkedHashMap<>();
        for (int i = 0; i < vs.size(); i += 2) {
            map.put((V) vs.get(i), (long) Double.parseDouble((String) vs.get(i + 1)));
        }
        return map;
    }

    public LinkedHashMap<V, Double> getZsetDoubleScore(String key, int offset, int limit) {
        byte[][] bytes = Stream.of(key, offset, offset + limit - 1, "WITHSCORES").map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        List vs = (List) send("ZREVRANGE", CacheEntryType.OBJECT, (Type) null, key, bytes).join();

        LinkedHashMap<V, Double> map = new LinkedHashMap<>();
        for (int i = 0; i < vs.size(); i += 2) {
            map.put((V) vs.get(i), Double.parseDouble(vs.get(i + 1) + ""));
        }
        return map;
    }
* */

    // --------------------
    /*
    supper had support
    public <N extends Number> void zadd(String key, Map<Serializable, N> kv) {
        if (kv == null || kv.isEmpty()) {
            return;
        }
        List<Serializable> args = new ArrayList();
        kv.forEach((k, v) -> {
            args.add(k);
            args.add(v);
        });

        sendAsync(RedisCommand.ZADD, key, args.toArray(Serializable[]::new)).join();
    }

    public <N extends Number> double zincr(String key, Serializable number, N n) {
        return sendAsync(RedisCommand.ZINCRBY, key, number, n).thenApply(x -> x.getDoubleValue(0d)).join();
    }

    @Override
    public long zrem(String key, String... vs) {
        return sendAsync(RedisCommand.ZREM, key, keysArgs(key, vs)).thenApply(x -> x.getLongValue(0L)).join();
    }*/

    /*public <T> List<T> zexists(String key, T... fields) {
        if (fields == null || fields.length == 0) {
            return new ArrayList<>();
        }
        List<String> para = new ArrayList<>();
        para.add("" +
                "   local key = KEYS[1];" +
                "    local args = ARGV;" +
                "    local result = {};" +
                "    for i,v in ipairs(args) do" +
                "        local inx = redis.call('ZREVRANK', key, v);" +
                "        if(inx) then" +
                "             table.insert(result,1,v);" +
                "        end" +
                "    end" +
                "    return result;");
        para.add("1");
        para.add(key);
        for (Object field : fields) {
            para.add(String.valueOf(field));
        }

        // todo:
        //sendAsync("EVAL", null, para.toArray(Serializable[]::new)).thenApply(x -> x.).join();

        return null;
    }*/

    //--------------------- bit ------------------------------
    public boolean getBit(String key, int offset) {
        return sendAsync(RedisCommand.GETBIT, key, key.getBytes(StandardCharsets.UTF_8), String.valueOf(offset).getBytes(StandardCharsets.UTF_8)).thenApply(v -> v.getIntValue(0) > 0).join();
    }

    public void setBit(String key, int offset, boolean bool) {
        sendAsync(RedisCommand.SETBIT, key, keysArgs(key, offset + "", bool ? "1" : "0")).join();
    }
    //--------------------- bit ------------------------------

    //--------------------- lock ------------------------------
    // 尝试加锁，成功返回0，否则返回上一锁剩余毫秒值
    public long tryLock(String key, int millis) {
        String[] obj = {"" +
                "if (redis.call('EXISTS',KEYS[1]) == 0) then " +
                "redis.call('PSETEX',KEYS[1],ARGV[1],1); " +
                "return 0; " +
                "else " +
                "return redis.call('PTTL',KEYS[1]); " +
                "end;", 1 + "", key, millis + ""
        };

        return sendAsync(RedisCommand.EVAL, null, keysArgs(null, obj)).thenApply(v -> v.getIntValue(1)).join();
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

    public void set(String key, Serializable value) {
        sendAsync(RedisCommand.SET, key, keysArgs(key, value + "")).join();
    }

    //--------------------- set ------------------------------
    /*public <T> void sadd(String key, Collection<T> args) {
        saddAsync(key, args.toArray(T[]::new)).join();
    }*/

    /*public void sadd(String key, Serializable... args) {
        String[] arr = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            arr[i] = args[i] + "";
        }
        saddAsync(key, arr).join();
    }

    public void srem(String key, String... args) {
        sremAsync(key, args).join();
    }

    public CompletableFuture<RedisCacheResult> saddAsync(String key, Serializable... args) {
        return sendAsync(RedisCommand.SADD, key, keysArgs(key, args));
    }

    public CompletableFuture<RedisCacheResult> sremAsync(String key, String... args) {
        return sendAsync(RedisCommand.SREM, key, keysArgs(key, args));
    }*/

    //--------------------- hm ------------------------------

    /*public Long incrHm(String key, String field, int value) {
        return sendAsync("HINCRBY", key, field, value).thenApply(x -> x.getLongValue(0l)).join();
    }

    public Double incrHm(String key, String field, double value) {
        return sendAsync("HINCRBYFLOAT", key, field, value).thenApply(x -> x.getDoubleValue(0d)).join();
    }*/

    public void setHm(String key, String field, Serializable value) {
        setHmsAsync(key, Map.of(field, value)).join();
    }

    public void setHms(String key, Map kv) {
        setHmsAsync(key, kv).join();
    }

    public CompletableFuture<RedisCacheResult> setHmsAsync(String key, Map<String, Serializable> kv) {
        List<String> args = new ArrayList();
        kv.forEach((k, v) -> {
            args.add(k);
            args.add(v + "");
        });

        return sendAsync(RedisCommand.HMSET, key, keysArgs(key, args.toArray(String[]::new)));
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
            if (list.get(i) == null) {
                continue;
            }
            map.put(field[i], (T) list.get(i));
        }
        return map;
    }

    /*public Map<String, Object> getHmall(String key) {
        List<String> list = null;  // TODO:
        Map<String, Object> map = new HashMap<>();
        if (list.isEmpty()) {
            return map;
        }

        for (int i = 0; i + 1 < list.size(); i += 2) {
            map.put((String) list.get(i), list.get(i + 1));
        }
        return map;
    }*/
}
