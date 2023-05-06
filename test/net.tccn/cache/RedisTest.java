package org.redkalex.cache.redis.test;

import org.redkale.net.AsyncIOGroup;
import org.redkale.util.AnyValue;
import org.redkale.util.ResourceFactory;
import org.redkalex.cache.redis.MyRedisCacheSource;

import java.awt.*;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.redkale.boot.Application.RESNAME_APP_CLIENT_ASYNCGROUP;
import static org.redkale.source.AbstractCacheSource.*;

public class RedisTest {

    static MyRedisCacheSource source = new MyRedisCacheSource();

    static { // redis://:*Zhong9307!@47.111.150.118:6064?db=2
        AnyValue.DefaultAnyValue conf = new AnyValue.DefaultAnyValue().addValue(CACHE_SOURCE_MAXCONNS, "1");
        conf.addValue(CACHE_SOURCE_NODE, new AnyValue.DefaultAnyValue().addValue(CACHE_SOURCE_URL, "redis://:*Zhong9307!@47.111.150.118:6064?db=0"));
        final ResourceFactory factory = ResourceFactory.create();
        final AsyncIOGroup asyncGroup = new AsyncIOGroup(8192, 16);
        asyncGroup.start();
        factory.register(RESNAME_APP_CLIENT_ASYNCGROUP, asyncGroup);
        factory.inject(source);
        //source.defaultConvert = JsonFactory.root().getConvert();
        source.init(conf);

        //--------------------- bit ------------------------------
        /*boolean ax = source.getBit("ax", 6);
        System.out.println("ax:"+ ax);
        source.setBit("ax", 6, true);

        ax = source.getBit("ax", 6);
        System.out.println("ax:"+ ax);

        source.setBit("ax", 6, false);
        ax = source.getBit("ax", 6);
        System.out.println("ax:"+ ax);*/
        //--------------------- bit ------------------------------

        //--------------------- bit ------------------------------

        /*
        source.lock("lockx", 5000);
        */

        //--------------------- set ------------------------------
        source.del("setx");
        /*
        int[] ints = {1, 2, 3};
        source.sadd("setx", ints);
        */

        //source.sadd("setx", list.toArray(Integer[]::new));
        List<Integer> list = List.of(2, 3, 5);
        // source.sadd("setx", list.toArray(Integer[]::new));
        source.sadd("setx", list.toArray(Integer[]::new));
        source.sadd("setx", 12,2312,213);

        source.keys("setx*").forEach(x -> {
            System.out.println(x);
        });

        source.srem("setx", 213, 2312);


        Collection<String> setx1 = source.getCollection("setx", String.class);

        System.out.println(setx1);


        //source.getexLong()

        source.setHms("hmx", Map.of("a", "5","b", "51", "c", "ads"));

        List<Serializable> hmget = source.hmget("hmx", int.class, "a");

        System.out.println(hmget);

        Integer hm = source.getHm("hmx", int.class, "ads");
        System.out.println(hm);

        Map<String, String> hms = source.getHms("hmx", "a", "b");
        System.out.println(hms);



        /*AnyValue.DefaultAnyValue conf = new AnyValue.DefaultAnyValue();
        conf.addValue("node", new AnyValue.DefaultAnyValue().addValue("addr", "47.111.150.118").addValue("port", "6064").addValue("password", "*Zhong9307!").addValue("db", 2));

        source.defaultConvert = JsonFactory.root().getConvert();
        source.initValueType(String.class); //value用String类型
        source.init(conf);*/
    }

    public static void main(String[] args) {


        //source.setLong("a", 125);

        /*long a = source.getLong("a", 0);
        System.out.println(a);

        List<String> keys = source.keys("farm*");
        keys.forEach(x -> System.out.println(x));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/


        // ===========================================
        //System.out.println(source.remove("a", "b"));

        // bit
        /*source.initValueType(Integer.class);
        source.remove("a");
        boolean a = source.getBit("a", 1);
        System.out.println(a);

        source.setBit("a", 1, true);
        a = source.getBit("a", 1);
        System.out.println("bit-a-1: " + a);

        source.setBit("a", 1, false);
        a = source.getBit("a", 1);
        System.out.println("bit-a-1: " + a);*/

        /*source.remove("a");

        // setnx
        System.out.println(source.setnx("a", 1));
        source.remove("a");
        System.out.println(source.setnx("a", 1));

        // set
        source.remove("abx1");
        source.appendSetItems("abx1", "a", "b", "c");
        List<String> list = source.srandomItems("abx1", 2);
        String str = source.srandomItem("abx1"); //r
        System.out.println(list);//[r1, r2]  */

        /*int[] arr = {0};
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10_0000);
        for (int i = 0; i < 10_0000; i++) {
            executor.submit(() -> {
                try {
                    source.lock("a", 50000);
                    arr[0]++;
                    System.out.println("Thread: " + Thread.currentThread().getName());
                    Thread.sleep(2000);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    source.unlock("a");
                    latch.countDown();
                }
            });
        }
        try {
            latch.await();
            System.out.println("n=" + arr[0]);
            executor.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/


        /*List<String> list = (List) source.getCollection("gamerank-comment-stat");
        System.out.println(list);*/

        /*for (int i = 0; i < 10; i++) {
            String brpop = source.brpop("z", 2);
            System.out.println(brpop);
        }*/

        // key 测试
        /*source.set("a", "123321");
        System.out.println(source.get("a")); // 123321
        System.out.println(source.getTtl("a")); // -1
        System.out.println(source.getPttl("a")); // -1
        System.out.println(source.getPttl("x")); // -2*/

        // hashmap 测试
        /*source.remove("sk");
        source.setHm("sk", "a", "1");
        source.setHm("sk", "b", "2");
        System.out.println(source.getHm("sk", "a")); // 1
        source.remove("sk");

        source.setHms("sk", Map.of("b", "5", "c", "3", "a", "1"));
        source.hdel("sk", "a");

        Map map = source.getHms("sk", "a", "x", "b", "c", "f"); //  {b=5, c=3}
        System.out.println(map);
        System.out.println(source.getHmall("sk")); //{b=5, c=3}
        System.out.println(source.incrHm("sk", "b", 1.1d)); // b = 6.1
        System.out.println(source.incrHm("sk", "c", 1)); // c = 4
        System.out.println(source.getHmall("sk")); //{b=6.1, c=4}

        System.out.println("--------------");
        System.out.println(source.hexists("sk", "b")); // true
        System.out.println(source.getCollectionSize("sk")); // 2*/


        /*Map<String, String> hms = source.getHms("supportusers", "5-kfeu0f", "xxxx", "3-0kbt7u8t", "95q- ");
        hms.forEach((k, v) -> {
            System.out.println(k + " : " + v);
        });*/


        /*MyRedisCacheSource<String> source2 = new MyRedisCacheSource();
        source2.defaultConvert = JsonFactory.root().getConvert();
        source2.initValueType(String.class); //value用String类型
        source2.init(conf);*/

        /*Map<String, String> gcMap = source.getHmall("hot-gamecomment");
        gcMap.forEach((k,v) -> {
            System.out.println(k + " : " + v);
        });*/


        //Map<String, String> gameinfo = source.getHms("gameinfo", "22759", "22838", "10097", "22751", "22632", "22711", "22195", "15821", "10099", "16313", "11345", "10534", "22768", "22647", "22924", "18461", "15871", "17099", "22640", "22644", "10744", "10264", "18032", "22815", "13584", "10031", "22818", "22452", "22810", "10513", "10557", "15848", "11923", "15920", "22808", "20073", "22809", "15840", "12332", "15803", "10597", "22624", "17113", "19578", "22664", "22621", "20722", "16226", "10523", "12304", "10597","11923","10031");
        //Map<String, String> gameinfo = source.getHms("gameinfo", "22759","22838","10097","22751","22632","22711","22195","15821","10099","16313","11345","10534","22768","22647","22924","18461","15871","17099","22363","22640","22644","10744","10264","18032","22815","13584","22818","22452","22810","10513","10557","15848","15920","22808","20073","22809","15840","12332","15803","10597","22624","17113","19578","22627","22664","22621","20722","16226","10523","12304");

        /*gameinfo.forEach((k,v ) -> {
            System.out.println(v);
        });*/


        /*source.queryKeysStartsWith("articlebean:").forEach(x -> {
            System.out.println(x);
            //source.remove(x);
            //System.out.println(source.getHmall(x));
        });*/

        // list 测试
        /*MyRedisCacheSource<Integer> sourceInt = new MyRedisCacheSource();
        sourceInt.defaultConvert = JsonFactory.root().getConvert();
        sourceInt.initValueType(Integer.class); //value用String类型
        sourceInt.init(conf);
        sourceInt.remove("list");
        Collection<Integer> list = sourceInt.getCollection("list");
        System.out.println(list);
        for (int i = 1; i <= 10; i++) {
            sourceInt.appendListItem("list", i);
        }
        System.out.println(sourceInt.getCollection("list")); // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

        sourceInt.appendListItems("list", 11, 12, 13);
        System.out.println(sourceInt.getCollection("list")); // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13]
        System.out.println(sourceInt.getCollection("list", 0, 5)); // [1, 2, 3, 4, 5]
        System.out.println(sourceInt.getCollectionSize("list")); // 13

        List<Integer> ids = new ArrayList<>(100);
        for (int i = 0; i < 5000; i++) {
            ids.add(i);
        }
        sourceInt.remove("abx");
        sourceInt.appendListItems("abx", ids.toArray(Integer[]::new));
        System.out.println(sourceInt.getCollection("abx"));*/

        /*System.out.println(sourceInt.getCollectionSize("recommend-user-quality"));
        Collection<Integer> uid = sourceInt.getCollection("recommend-user-quality");
        System.out.println(uid);*/

        // zset 测试
        /*source.initValueType(String.class); //value用Integer类型
        source.remove("zx");
        source.zadd("zx", Map.of("a", 1, "b", 2));

        source.zadd("zx", Map.of("a", 1, "c", 5L));
        source.zadd("zx", Map.of("x", 20, "j", 3.5));
        source.zadd("zx", Map.of("f", System.currentTimeMillis(), "c", 5L));
        source.zadd("zx", Map.of("a", 1, "c", 5L));

        System.out.println(source.zincr("zx", "a", 1.34)); // 2.34
        System.out.println(source.getZsetDoubleScore("zx")); // {f=1592924555704, x=20, c=5, j=3, b=2.34, a=1}
        source.zrem("zx", "b", "c", "e", "x");
        System.out.println(source.getZsetLongScore("zx")); // {f=1592924555704, j=3, a=2}

        System.out.println("--------------");
        System.out.println(source.getZsetLongScore("zx", "f"));

        System.out.println(source.getZrevrank("zx", "f")); // 0
        System.out.println(source.getZrank("zx", "f")); // 2
        System.out.println(source.getZrank("zx", "Y")); // -1
        System.out.println(source.getCollectionSize("zx")); // 3

        System.out.println(source.getZset("zx"));
        System.out.println(source.zexists("zx", "f", "x", "a"));*/

        /*LocalDate date = LocalDate.of(2019, 12, 31);
        for (int i = 0; i < 60; i++) {
            LocalDate localDate = date.plusDays(-i);
            String day = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            System.out.println(String.format("mkdir %s; mv *%s*.zip %s", day, day, day));
        }*/


        /*MyRedisCacheSource<UserDetail> source = new MyRedisCacheSource();
        source.defaultConvert = JsonFactory.root().getConvert();
        source.initValueType(UserDetail.class);
        source.init(conf);


        Map<String, UserDetail> map = source.getHmall("user-detail");

        Integer[] array = map.values().stream().map(x -> x.getUserid()).toArray(Integer[]::new);

        System.out.println(JsonConvert.root().convertTo(array));

        Map<Integer, UserDetail> hms = source.getHms("user-detail", 11746, 11988, 11504, 11987, 11745, 11503, 11748, 11506, 11747, 11989, 11505, 11508, 11507, 11509, 11980, 11740, 11982, 11981, 11984, 11742, 11500, 11983, 11741, 11502, 11744, 11986, 11985, 11501, 11743, 11999, 11757, 11515, 1, 11514, 11998, 11756, 2, 11517, 11516, 11758, 3, 11519, 4, 5, 11518, 6, 7, 11991, 8, 11990, 9, 11993, 11751, 11750, 11992, 11753, 11511, 11995, 11994, 11510, 11752, 11755, 11513, 11997, 11512, 11996, 11754, 11724, 11966, 11965, 11723, 11968, 11726, 11967, 11725, 11728, 11969, 11727, 11729, 11960, 11720, 11962, 11961, 11722, 11964, 11721);

        System.out.println(hms.size());*/

        /*source.getCollection("article-comment-list", 19, 1).forEach(x -> System.out.println(x));


        while (true) {
            System.out.println("---" + Utility.now() + "---");
            source.getHmall("ck").forEach((k, v) -> {
                System.out.println(k + ":" + v);
            });
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }
}
