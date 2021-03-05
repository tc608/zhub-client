package com.zdemo.cachex;


import org.redkale.convert.Convert;
import org.redkale.service.Local;
import org.redkale.source.CacheSource;
import org.redkale.util.AutoLoad;
import org.redkale.util.ResourceType;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Local
@AutoLoad(false)
@ResourceType(CacheSource.class)
public class MyRedisCacheSource<V extends Object> extends RedisCacheSource<V> {
    //--------------------- oth ------------------------------
    public boolean setnx(String key, Object v) {
        byte[][] bytes = Stream.of(key, v).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        Serializable rs = send("SETNX", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
        return rs == null ? false : (long) rs == 1;
    }
    //--------------------- oth ------------------------------

    //--------------------- bit ------------------------------
    public boolean getBit(String key, int offset) {
        byte[][] bytes = Stream.of(key, offset).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        Serializable v = send("GETBIT", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
        return v == null ? false : (long) v == 1;
    }

    public void setBit(String key, int offset, boolean bool) {
        byte[][] bytes = Stream.of(key, offset, bool ? 1 : 0).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        send("SETBIT", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
    }

    //--------------------- bit ------------------------------
    //--------------------- lock ------------------------------
    // 尝试加锁，成功返回0，否则返回上一锁剩余毫秒值
    public int tryLock(String key, int millis) {
        byte[][] bytes = Stream.of("" +
                "if (redis.call('exists',KEYS[1]) == 0) then " +
                "redis.call('psetex', KEYS[1], ARGV[1], 1) " +
                "return 0; " +
                "else " +
                "return redis.call('PTTL', KEYS[1]); " +
                "end; ", 1, key, millis).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        int n = (int) send("EVAL", CacheEntryType.OBJECT, (Type) null, null, bytes).join();
        return n;
    }

    // 加锁
    public void lock(String key, int millis) {
        int i;
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

    public long getTtl(String key) {
        return (long) send("TTL", CacheEntryType.OBJECT, (Type) null, key, key.getBytes(StandardCharsets.UTF_8)).join();
    }

    public long getPttl(String key) {
        return (long) send("PTTL", CacheEntryType.OBJECT, (Type) null, key, key.getBytes(StandardCharsets.UTF_8)).join();
    }

    public int remove(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0;
        }
        List<String> para = new ArrayList<>();
        para.add("" +
                "    local args = ARGV;" +
                "    local x = 0;" +
                "    for i,v in ipairs(args) do" +
                "        local inx = redis.call('del', v);" +
                "        if(inx > 0) then" +
                "             x = x + 1;" +
                "        end" +
                "    end" +
                "    return x;");

        para.add("0");
        for (Object field : keys) {
            para.add(String.valueOf(field));
        }
        byte[][] bytes = para.stream().map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        return (int) send("EVAL", CacheEntryType.OBJECT, (Type) null, null, bytes).join();
    }

    //--------------------- hmget ------------------------------
    public <T extends Object> V getHm(String key, T field) {
        // return (V) send("HMGET", CacheEntryType.OBJECT, (Type) null, key, key.getBytes(StandardCharsets.UTF_8), field.getBytes(StandardCharsets.UTF_8)).join();
        Map<Object, V> map = getHms(key, field);
        return map.get(field);
    }

    public <T extends Object> Map<T, V> getHms(String key, T... field) {
        if (field == null || field.length == 0) {
            return new HashMap<>();
        }
        byte[][] bytes = Stream.concat(Stream.of(key), Stream.of(field)).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        Map<T, V> result = new HashMap<>();

        List<V> vs = (List) send("HMGET", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
        for (int i = 0; i < field.length; i++) { // /*vs != null && vs.size() > i &&*/
            if (vs.get(i) == null) {
                continue;
            }
            result.put(field[i], vs.get(i));
        }

        return result;
    }

    public Map<String, V> getHmall(String key) {
        List<V> vs = (List) send("HGETALL", CacheEntryType.OBJECT, (Type) null, key, key.getBytes(StandardCharsets.UTF_8)).join();
        Map<String, V> result = new HashMap<>(vs.size() / 2);
        for (int i = 0; i < vs.size(); i += 2) {
            result.put(String.valueOf(vs.get(i)), vs.get(i + 1));
        }

        return result;
    }

    //--------------------- hmset、hmdel、incr ------------------------------
    public <T> void setHm(String key, T field, V value) {
        byte[][] bytes = Stream.of(key, field, value).map(x -> x.toString().getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        send("HMSET", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
    }

    public <T> void setHms(String key, Map<T, V> kv) {
        List<String> args = new ArrayList();
        args.add(key);

        kv.forEach((k, v) -> {
            args.add(String.valueOf(k));
            args.add(String.valueOf(v));
        });

        byte[][] bytes = args.stream().map(x -> x.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        send("HMSET", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
    }

    public <T> Long incrHm(String key, T field, long n) {
        byte[][] bytes = Stream.of(key, String.valueOf(field), String.valueOf(n)).map(x -> x.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        return (Long) send("HINCRBY", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
    }

    public <T> Double incrHm(String key, T field, double n) {
        byte[][] bytes = Stream.of(key, String.valueOf(field), String.valueOf(n)).map(x -> x.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        Serializable v = send("HINCRBYFLOAT", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
        if (v == null) {
            return null;
        }
        return Double.parseDouble(String.valueOf(v));
    }

    public <T> void hdel(String key, T... field) {
        byte[][] bytes = Stream.concat(Stream.of(key), Stream.of(field)).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        send("HDEL", null, (Type) null, key, bytes).join();
    }

    public <T> List<T> zexists(String key, T... fields) {
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
        byte[][] bytes = para.stream().map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        return (List<T>) send("EVAL", CacheEntryType.OBJECT, (Type) null, null, bytes).join();
    }

    //--------------------- set ------------------------------
    public <T> T srandomItem(String key) {
        byte[][] bytes = Stream.of(key, 1).map(x -> formatValue(CacheEntryType.OBJECT, (Convert) null, (Type) null, x)).toArray(byte[][]::new);
        List<T> list = (List) send("SRANDMEMBER", null, (Type) null, key, bytes).join();
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    public <T> List<T> srandomItems(String key, int n) {
        byte[][] bytes = Stream.of(key, n).map(x -> formatValue(CacheEntryType.OBJECT, (Convert) null, (Type) null, x)).toArray(byte[][]::new);
        return (List) send("SRANDMEMBER", null, (Type) null, key, bytes).join();
    }

    //--------------------- list ------------------------------
    public CompletableFuture<Void> appendListItemsAsync(String key, V... values) {
        byte[][] bytes = Stream.concat(Stream.of(key), Stream.of(values)).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        return (CompletableFuture) send("RPUSH", null, (Type) null, key, bytes);
    }

    public CompletableFuture<Void> lpushListItemAsync(String key, V value) {
        return (CompletableFuture) send("LPUSH", null, (Type) null, key, key.getBytes(StandardCharsets.UTF_8), formatValue(CacheEntryType.OBJECT, (Convert) null, (Type) null, value));
    }

    public void lpushListItem(String key, V value) {
        lpushListItemAsync(key, value).join();
    }

    public void appendListItems(String key, V... values) {
        appendListItemsAsync(key, values).join();
    }

    public void appendSetItems(String key, V... values) {
        // todo:
        for (V v : values) {
            appendSetItem(key, v);
        }
    }

    // 1  2  3 4  5  6  7  8  9  10  11  12  13  14  15
    public CompletableFuture<Collection<V>> getCollectionAsync(String key, int offset, int limit) {
        return (CompletableFuture) send("OBJECT", null, (Type) null, key, "ENCODING".getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8)).thenCompose(t -> {
            if (t == null) return CompletableFuture.completedFuture(null);
            if (new String((byte[]) t).contains("list")) { //list
                return send("LRANGE", CacheEntryType.OBJECT, (Type) null, false, key, key.getBytes(StandardCharsets.UTF_8), String.valueOf(offset).getBytes(StandardCharsets.UTF_8), String.valueOf(offset + limit - 1).getBytes(StandardCharsets.UTF_8));
            } else {
                return send("SMEMBERS", CacheEntryType.OBJECT, (Type) null, true, key, key.getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    public Collection<V> getCollection(String key, int offset, int limit) {
        return getCollectionAsync(key, offset, limit).join();
    }

    public V brpop(String key, int seconds) {
        byte[][] bytes = Stream.concat(Stream.of(key), Stream.of(seconds)).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        return (V) send("BRPOP", null, (Type) null, key, bytes).join();
    }

    //--------------------- zset ------------------------------
    public <N extends Number> void zadd(String key, Map<V, N> kv) {
        if (kv == null || kv.isEmpty()) {
            return;
        }
        List<String> args = new ArrayList();
        args.add(key);

        kv.forEach((k, v) -> {
            args.add(String.valueOf(v));
            args.add(String.valueOf(k));
        });

        byte[][] bytes = args.stream().map(x -> x.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        send("ZADD", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
    }

    public <N extends Number> double zincr(String key, Object number, N n) {
        byte[][] bytes = Stream.of(key, n, number).map(x -> String.valueOf(x).getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        Serializable v = send("ZINCRBY", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
        return Double.parseDouble(String.valueOf(v));
    }

    public void zrem(String key, V... vs) {
        List<String> args = new ArrayList();
        args.add(key);
        for (V v : vs) {
            args.add(String.valueOf(v));
        }
        byte[][] bytes = args.stream().map(x -> x.getBytes(StandardCharsets.UTF_8)).toArray(byte[][]::new);
        send("ZREM", CacheEntryType.OBJECT, (Type) null, key, bytes).join();
    }

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

    // ----------
    protected byte[] formatValue(CacheEntryType cacheType, Convert convert0, Type resultType, Object value) {
        if (value == null) return "null".getBytes(StandardCharsets.UTF_8);
        if (convert0 == null) convert0 = convert;
        if (cacheType == CacheEntryType.LONG || cacheType == CacheEntryType.ATOMIC)
            return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
        if (cacheType == CacheEntryType.STRING) return convert0.convertToBytes(String.class, value);

        if (value instanceof String) return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
        if (value instanceof Number) return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
        return convert0.convertToBytes(resultType == null ? objValueType : resultType, value);
    }
}
