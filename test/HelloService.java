import org.junit.Before;
import org.junit.Test;
import net.tccn.IType;
import net.tccn.zhub.Lock;
import net.tccn.zhub.ZHubClient;

// @RestService(automapping = true)
public class HelloService {

    // @Resource(name = "zhub")
    private ZHubClient zhub;

    @Before
    public void init() {



        //zhub = new ZHubClient("127.0.0.1:1216", "g-dev", "DEV-LOCAL", "zchd@123456");
        zhub = new ZHubClient("127.0.0.1:1216", "g-dev", "DEV-LOCAL", "token-12345");

        zhub.subscribe("tv:test", x -> {
            System.out.println(x);
        });

        Lock lock = zhub.tryLock("lock-a", 5);
        System.out.println("lock-1: " + lock.success());

        //zhub.init(Kv.of("host", "47.111.150.118", "port", "6066", "groupid", "g-dev", "appname", "DEV-LOCAL"));

        // Function<Rpc<T>, RpcResult<R>> fun
        /*zhub.rpcSubscribe("x", new TypeToken<String>() {
        }, r -> {
            return r.buildResp(Map.of("v", r.getValue().toUpperCase() + ": Ok"));
        });

        zhub.subscribe("sport:reqtime", x -> {
            //System.out.println(x);
        });
        zhub.subscribe("abx", x -> {
            System.out.println(x);
        });

        try {
            Thread.sleep(010);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        zhub.delay("sport:reqtime", "别✈人家的✦女娃子❤🤞🏻", 0);
        zhub.delay("sport:reqtime", "别人家的女娃子➾🤞🏻", 0);
        zhub.delay("sport:reqtime", "❤别人家✉�的女娃子❤🤞🏻", 0);*/
        /*zhub.delay("sport:reqtime", "中文特殊符号：『』 ＄ ￡ ♀ ‖ 「」\n" +
                "英文：# + = & ﹉ .. ^ \"\" ·{ } % – ' €\n" +
                "数学：＋× ＝ － ° ± ＜ ＞ ℃ ㎡ ∑ ≥ ∫ ㏄ ⊥ ≯ ∠ ∴ ∈ ∧ ∵ ≮ ∪ ㎝ ㏑ ≌ ㎞ № § ℉ ÷ ％ ‰ ㎎ ㎏ ㎜ ㏒ ⊙ ∮ ∝ ∞ º ¹ ² ³ ½ ¾ ¼ ≈ ≡ ≠ ≤ ≦ ≧ ∽ ∷ ／ ∨ ∏ ∩ ⌒ √Ψ ¤ ‖ ¶\n" +
                "特殊：♤ ♧ ♡ ♢ ♪ ♬ ♭ ✔ ✘ ♞ ♟ ↪ ↣ ♚ ♛ ♝ ☞ ☜ ⇔ ☆ ★ □ ■ ○ ● △ ▲ ▽ ▼ ◇ ◆ ♀ ♂ ※ ↓ ↑ ↔ ↖ ↙ ↗ ↘ ← → ♣ ♠ ♥ ◎ ◣ ◢ ◤ ◥ 卍 ℡ ⊙ ㊣ ® © ™ ㈱ 囍\n" +
                "序号：①②③④⑤⑥⑦⑧⑨⑩㈠㈡㈢㈣㈤㈥㈦㈧㈨㈩⑴ ⑵ ⑶ ⑷ ⑸ ⑹ ⑺ ⑻ ⑼ ⑽ ⒈ ⒉ ⒊ ⒋ ⒌ ⒍ ⒎ ⒏ ⒐ ⒑ Ⅰ Ⅱ Ⅲ Ⅳ Ⅴ Ⅵ Ⅶ Ⅷ ⅨⅩ\n" +
                "日文：アイウエオァィゥェォカキクケコガギグゲゴサシスセソザジズゼゾタチツテトダヂヅデドッナニヌネノハヒフヘホバビブベボパピプペポマミムメモャヤュユョラリヨルレロワヰヱヲンヴヵヶヽヾ゛゜ー、。「「あいうえおぁぃぅぇぉかきくけこがぎぐげごさしすせそざじずぜぞたちつてでどっなにぬねのはひふへ」」ほばびぶべぼぱぴぷぺぽまみむめもやゆよゃゅょらりるれろわをんゎ゛゜ー、。「」\n" +
                "部首：犭 凵 巛 冖 氵 廴 讠 亻 钅 宀 亠 忄 辶 弋 饣 刂 阝 冫 卩 疒 艹 疋 豸 冂 匸 扌 丬 屮衤 礻 勹 彳 彡", 0);*/

    }

    @Test
    public void rpcTest() {
        //RpcResult<String> rpc = zhub.rpc("wx:users", Map.of("appId", "wxa554ec3ab3bf1fc7"), IConsumer.TYPE_TOKEN_STRING);
        //RpcResult<String> rpc = zhub.rpc("a", "fa", IConsumer.TYPE_TOKEN_STRING);
        zhub.publish("tv:test", "hello ym!");

        zhub.subscribe("tv:abx", x -> {
            System.out.println(x);
        });

        zhub.rpcSubscribe("rpc-x", IType.STRING, x -> {
            return x.render(x.getValue().toUpperCase());
        });

        Lock lock = zhub.tryLock("lock-a", 5);
        System.out.println("lock-2: " + lock.success());

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Lock lock2 = zhub.tryLock("lock-a", 5);
        System.out.println("lock-3: " + lock2.success());
        /*try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        Lock lock3 = zhub.tryLock("lock-a", 5);
        System.out.println("lock-4: " + lock3.success());

        try {
            Thread.sleep(3000 * 30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void lockTest() {

    }

    /*RpcResult<FileToken> x = zhub.rpc("rpc:file:up-token", Map.of(), new TypeToken<>() {
            });*/
}
