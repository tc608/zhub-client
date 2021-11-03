package com.zdemo.test;

import com.zdemo.IConsumer;
import com.zdemo.zhub.RpcResult;
import com.zdemo.zhub.ZHubClient;
import org.redkale.net.http.RestMapping;
import org.redkale.net.http.RestService;
import org.redkale.service.Service;
import org.redkale.util.AnyValue;
import org.redkale.util.TypeToken;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestService(automapping = true)
public class HelloService implements Service {

    @Resource(name = "zhub")
    private ZHubClient zhub;

    private net.tccn.zhub.ZHubClient zhubx = null;


    @Override
    public void init(AnyValue config) {

        CompletableFuture.runAsync(() -> {
            zhubx = new net.tccn.zhub.ZHubClient("127.0.0.1", 1216, "g-dev", "DEV-LOCAL");
            //zhubx = new net.tccn.zhub.ZHubClient("47.111.150.118", 6066, "g-dev", "DEV-LOCAL");
        });

        // Function<Rpc<T>, RpcResult<R>> fun
        /*zhub.rpcSubscribe("x", new TypeToken<String>() {
        }, r -> {

            return r.buildResp(Map.of("v", r.getValue().toUpperCase() + ": Ok"));
        });*/
        zhub.rpcSubscribe("y", new TypeToken<String>() {
        }, r -> {

            return r.buildResp(Map.of("v", r.getValue().toUpperCase() + ": Ok"));
        });

        zhub.subscribe("sport:reqtime", x -> {
            System.out.println(x);
        });
        zhub.subscribe("abx", x -> {
            System.out.println(x);
        });

        try {
            Thread.sleep(010);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*zhub.delay("sport:reqtime", "别✈人家的✦女娃子❤🤞🏻", 0);
        zhub.delay("sport:reqtime", "别人家的女娃子➾🤞🏻", 0);
        zhub.delay("sport:reqtime", "❤别人家✉�的女娃子❤🤞🏻", 0);
        zhub.delay("sport:reqtime", "中文特殊符号：『』 ＄ ￡ ♀ ‖ 「」\n" +
                "英文：# + = & ﹉ .. ^ \"\" ·{ } % – ' €\n" +
                "数学：＋× ＝ － ° ± ＜ ＞ ℃ ㎡ ∑ ≥ ∫ ㏄ ⊥ ≯ ∠ ∴ ∈ ∧ ∵ ≮ ∪ ㎝ ㏑ ≌ ㎞ № § ℉ ÷ ％ ‰ ㎎ ㎏ ㎜ ㏒ ⊙ ∮ ∝ ∞ º ¹ ² ³ ½ ¾ ¼ ≈ ≡ ≠ ≤ ≦ ≧ ∽ ∷ ／ ∨ ∏ ∩ ⌒ √Ψ ¤ ‖ ¶\n" +
                "特殊：♤ ♧ ♡ ♢ ♪ ♬ ♭ ✔ ✘ ♞ ♟ ↪ ↣ ♚ ♛ ♝ ☞ ☜ ⇔ ☆ ★ □ ■ ○ ● △ ▲ ▽ ▼ ◇ ◆ ♀ ♂ ※ ↓ ↑ ↔ ↖ ↙ ↗ ↘ ← → ♣ ♠ ♥ ◎ ◣ ◢ ◤ ◥ 卍 ℡ ⊙ ㊣ ® © ™ ㈱ 囍\n" +
                "序号：①②③④⑤⑥⑦⑧⑨⑩㈠㈡㈢㈣㈤㈥㈦㈧㈨㈩⑴ ⑵ ⑶ ⑷ ⑸ ⑹ ⑺ ⑻ ⑼ ⑽ ⒈ ⒉ ⒊ ⒋ ⒌ ⒍ ⒎ ⒏ ⒐ ⒑ Ⅰ Ⅱ Ⅲ Ⅳ Ⅴ Ⅵ Ⅶ Ⅷ ⅨⅩ\n" +
                "日文：アイウエオァィゥェォカキクケコガギグゲゴサシスセソザジズゼゾタチツテトダヂヅデドッナニヌネノハヒフヘホバビブベボパピプペポマミムメモャヤュユョラリヨルレロワヰヱヲンヴヵヶヽヾ゛゜ー、。「「あいうえおぁぃぅぇぉかきくけこがぎぐげごさしすせそざじずぜぞたちつてでどっなにぬねのはひふへ」」ほばびぶべぼぱぴぷぺぽまみむめもやゆよゃゅょらりるれろわをんゎ゛゜ー、。「」\n" +
                "部首：犭 凵 巛 冖 氵 廴 讠 亻 钅 宀 亠 忄 辶 弋 饣 刂 阝 冫 卩 疒 艹 疋 豸 冂 匸 扌 丬 屮衤 礻 勹 彳 彡", 0);
*/
    }

    @RestMapping
    public RpcResult x(String v) {
        if (v == null) {
            v = "";
        }

        List<CompletableFuture> list = new ArrayList();

        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            /*RpcResult<FileToken> x = zhub.rpc("rpc:file:up-token", Map.of(), new TypeToken<>() {
            });*/

            /*list.add(zhub.rpcAsync("x", v + i, new TypeToken<>() {
            }));*/
            zhub.publish("x", v + i);

            System.out.println("time: " + (System.currentTimeMillis() - start) + " ms");
            //System.out.println(x.getResult().get("v"));
        }

        return zhub.rpc("x", v, IConsumer.TYPE_TOKEN_STRING);
    }

    @RestMapping
    public RpcResult<String> d(String v) {
        RpcResult<String> rpc = zhub.rpc("x", v, IConsumer.TYPE_TOKEN_STRING);
        return rpc;
    }

    @RestMapping
    public String y(String v) {
        if (v == null) {
            v = "";
        }

        for (int i = 0; i < 100; i++) {
            long start = System.currentTimeMillis();
            /*RpcResult<FileToken> x = zhub.rpc("rpc:file:up-token", Map.of(), new TypeToken<>() {
            });*/

            net.tccn.zhub.RpcResult<Object> x = zhubx.rpc("y", v + i, new com.google.gson.reflect.TypeToken<>() {
            });

            System.out.println("time: " + (System.currentTimeMillis() - start) + " ms");

            //System.out.println(x.getResult());
        }

        return "ok";
    }


    public static void main(String[] args) {
        // "\"别人家的女娃子\uD83E\uDD1E\uD83C\uDFFB\""
        /*String s = "别人家的女娃子\uD83E\uDD1E\uD83C\uDFFB";
        System.out.println("别人家的女娃子🤞🏻".length());

        byte[] bytes = "别人家的女娃子🤞🏻".getBytes();
        System.out.println(bytes.length);

        System.out.println(unicodeToUtf8(s));
        System.out.println(utf8ToUnicode("别人家的女娃子🤞🏻"));
        */

        //ExecutorService pool = Executors.newFixedThreadPool(5);


    }


    public static String utf8ToUnicode(String inStr) {
        char[] myBuffer = inStr.toCharArray();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < inStr.length(); i++) {
            Character.UnicodeBlock ub = Character.UnicodeBlock.of(myBuffer[i]);
            if (ub == Character.UnicodeBlock.BASIC_LATIN) {
                //英文及数字等
                sb.append(myBuffer[i]);
            } else if (ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
                //全角半角字符
                int j = (int) myBuffer[i] - 65248;
                sb.append((char) j);
            } else {
                //汉字
                short s = (short) myBuffer[i];
                String hexS = Integer.toHexString(s);
                String unicode = "\\u" + hexS;
                sb.append(unicode.toLowerCase());
            }
        }
        return sb.toString();
    }

    public static String unicodeToUtf8(String theString) {
        char aChar;
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed   \\uxxxx   encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f')
                        aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }
}
