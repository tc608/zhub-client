package net.tccn.mq;

import net.tccn.Event;
import net.tccn.timer.Timers;
import org.junit.Test;
import org.redkale.convert.json.JsonConvert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息发布订阅测试
 */
public class AppTest {
    Logger logger = Logger.getLogger("");

    @Test
    public void runConsumer() {
        try {
            // String str = ", response = {\"success\":true,\"retcode\":0,\"result\":{\"age\":0,\"explevel\":1,\"face\":\"https://aimg.woaihaoyouxi.com/haogame/202106/pic/20210629095545FmGt-v9NYqyNZ_Q6_y3zM_RMrDgd.jpg\",\"followed\":0,\"gender\":0,\"idenstatus\":0,\"matchcatelist\":[{\"catename\":\"足球\",\"catepic\":\"https://aimg.woaihaoyouxi.com/haogame/202107/pic/20210714103556FoG5ICf_7BFx6Idyo3TYpJQ7tmfG.png\",\"matchcateid\":1},{\"catename\":\"篮球\",\"catepic\":\"https://aimg.woaihaoyouxi.com/haogame/202107/pic/20210714103636FklsXTn1f6Jlsam8Jk-yFB7Upo3C.png\",\"matchcateid\":2}],\"matchcates\":\"2,1\",\"mobile\":\"18515190967\",\"regtime\":1624931714781,\"sessionid\":\"d1fc447753bd4700ad29674a753030fa\",\"status\":10,\"userid\":100463,\"username\":\"绝尘\",\"userno\":100463}}";
            String str = "hello你好";

            System.out.println(str.length());

            /*consumer.timer("a", () -> {
                System.out.println(Utility.now() + " timer a 执行了");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            consumer.timer("b", () -> {
                System.out.println(Utility.now() + " ----------------- timer b 执行了");
            });
            //consumer.delay("a", "1", 200);
            consumer.delay("a", "1", "2000");*/

            /*Consumer<String> con = x -> {
                logger.info("--->开始申请锁:" + System.currentTimeMillis());
                Lock lock = consumer.tryLock("a", 20);
                logger.info("===>成功申请锁:" + System.currentTimeMillis());
                for (int i = 0; i < 20; i++) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(x + ":" + i);
                }
                lock.unLock();
            };

            new Thread(() -> con.accept("x")).start();
            new Thread(() -> con.accept("y")).start();
            new Thread(() -> con.accept("z")).start();*/


            Thread.sleep(60_000 * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void runProducer() {

    }

    private static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue();

    @Test
    public void t() {
        List<String> list = new ArrayList<>();
        list.toArray(String[]::new);

        new Thread(() -> {
            while (true) {
                System.out.println("accept:");
                String peek = null;
                try {
                    System.out.println(!queue.isEmpty());
                    peek = queue.poll(100, TimeUnit.MILLISECONDS);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(peek);
            }
        }).start();

        for (int i = 0; i < 10; i++) {
            try {
                queue.put(i + "");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        try {
            System.out.println("---");
            Thread.sleep(1000 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void xx() {
        Function<String, String> fun = x -> {
            return x.toUpperCase();
        };

        System.out.println(fun.toString());
    }


    // (27+5*23)/(63-59)
    // [27+5*23] [/] [63-59]
    // [27] + [5*23] [/] [63-59]


    /**
     * 1. 按照优先级逐一拆解运算
     * 括号, 乘除，加减
     * 2. 逐一进行计算
     */
    class C {
        C A;
        C B;

        int c1; // 如果 A 只剩数字，将c1 转为整数存放到 c1
        int c2;
        String x; // + - * /
    }

    @Test
    public void x() {
        // (27+5*23)/(63-59)

        String str = "(27+5*23)/(63-59)";
        str = "27+5*23";
        str = "258/((35+17)/(5*3+18-29)+3)(138-134)*41-6+10+24";
        str = "5*3+18-29";

        //System.out.println("258/((35+17)/(5*3+18-29)+3)(138-134)*41-6+10+24".replaceAll("\\)\\(", ")*("));
        List<String> parse = parse(str.replaceAll("\\)\\(", ")\\*("));


        System.out.println(c(str));
    }

    // 因式分解：括号 -> 乘除 -> 加减
    public List<String> parse(String str) {
        // 找到一对
        /*

        258/()()
        [258, /, (35+17)/(5*3+18-29)+3, 138-134, *, 41, -, 6, +, 10, +, 24]

         */
        // 258/((35+17)/(5*3+18-29)+3)(138-134)*41-6+10+24
        //str = "258    /   (35+17)/(5*3+18-29)+3   138-134,   *   41-6+10+24";
        String[] strArr = str.split("");

        // 一级括号、加、减、乘、除分解
        List<String> arr = new ArrayList<>();
        String tmp = "";
        int n1 = 0; // 括号层级开始
        int m1 = 0; // 括号层级结尾
        for (String s : strArr) {
            if (n1 > 0) { // 一级括号分解
                if (")".equals(s) && (++m1) == n1) { // 一级括号结束
                    arr.add(tmp);
                    tmp = "";
                    n1 = 0;
                    m1 = 0;
                } else {
                    if ("(".equals(s)) {
                        n1++;
                    }
                    tmp += s;
                }
            } else { // 无括号
                if ("+".equals(s) || "-".equals(s) || "*".equals(s) || "/".equals(s)) {
                    if (!"".equals(tmp)) {
                        arr.add(tmp);
                    }
                    arr.add(s);
                    tmp = "";
                } else if ("(".equals(s)) {
                    n1 = 1;
                } else {
                    tmp += s;
                }
            }
        }
        if (!"".equals(tmp)) {
            arr.add(tmp);
        }
        return arr;
    }

    public int c(String str) {
        List<String> arr = parse(str); // 预期 length >= 3 基数的基数，如：[27+5*23, /, 63-59], [60, /, 2, *, 164-23*7]
        System.out.println(arr);
        if (arr == null || arr.size() < 3) {
            return -1; // 错误码-1：错误的计算式
        }

        List<String> _arr = new ArrayList<>();
        // 按照优先级做合并计算 乘除优先

        // 乘除
        for (int i = 1; i < arr.size() - 1; i += 2) {
            /*if ("*".equals(arr.get(i)) || "/".equals(arr.get(i))) {
                if (_arr.size() > 0) {
                    _arr.remove(_arr.size() - 1);
                }
                int c = c(arr.get(i - 1), arr.get(i + 1), arr.get(i));
                if (c < 0) {
                    return -1;
                }
                _arr.add(c + "");
            } else {
                _arr.add(arr.get(i - 1));
                _arr.add(arr.get(i));
                _arr.add(arr.get(i + 1));
            }*/

            if ("*".equals(arr.get(i)) || "/".equals(arr.get(i))) {
                int c = c(arr.get(i - 1), arr.get(i + 1), arr.get(i));
                if (c < 0) {
                    return c;
                }
                _arr.add(c + "");
            } else {


            }
        }

        if (_arr.size() == 1) { // 通过第一轮的 乘除计算，完成结果合并
            return Integer.parseInt(_arr.get(0));
        }
        int c = 0;
        for (int i = 1; i < _arr.size(); i += 2) {
            int _c = c(_arr.get(i - 1), _arr.get(i + 1), _arr.get(i));
            if (_c < 0) {
                return _c;
            }
            c += _c;
        }
        return c;
    }

    public int c(String a, String b, String x) {
        int _a = 0;
        if (a.contains("(") || a.contains("+") || a.contains("-") || a.contains("*") || a.contains("/")) {
            _a = c(a);
        } else { // 预期 无 ( + - * / 的结果为标准数字
            _a = Integer.parseInt(a);
        }
        int _b = 0;
        if (b.contains("(") || b.contains("+") || b.contains("-") || b.contains("*") || b.contains("/")) {
            _b = c(b);
        } else { // 预期 无 ( + - * / 的结果为标准数字
            _b = Integer.parseInt(b);
        }

        // 如果出现负数（错误码）直接返回对应的负数
        if (_a < 0) {
            return _a;
        }
        if (_b < 0) {
            return _b;
        }

        // 定义错误标识: -1错误的计算式，-2除不尽，-3除数为0，-4大于200,
        if ("+".equals(x)) {
            return _a + _b;
        } else if ("-".equals(x)) {
            return _a - _b;
        } else if ("*".equals(x)) {
            return _a * _b;
        } else if ("/".equals(x)) {
            return _a % _b > 0 ? -2 : _a / _b; // 除不尽
        }
        return 0;
    }

    @Test
    public void testxx() {
        Event of = Event.of("A", Map.of("b", 1));

        System.out.println(JsonConvert.root().convertTo(of));

        String str = "❦别人家的女娃子🤞🏻ꚢ";

        /*
        System.out.println("别人家的女娃子🤞🏻".length());*/
        System.out.println(strLength(str));
        System.out.println(getWordCount(str));
        /*try {
            System.out.println("别人家的女娃子🤞🏻".getBytes("UTF-8").length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("系统默认编码方式：" + System.getProperty("file.encoding"));*/
    }


    public static int strLength(String value) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }

    public int getWordCount(String str) {
        str = str.replaceAll("[^\\x00-\\xff]", "*");
        return str.length();
    }

    @Test
    public void delay() {
        DelayQueue<com.zdemo.zhub.Delays> delayQueue = new DelayQueue<>();

        logger.info("加入延时任务1");
        delayQueue.add(new com.zdemo.zhub.Delays(5000, () -> {
            logger.info("任务1 延时任务执行了!");
        }));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("加入延时任务2");
        delayQueue.add(new com.zdemo.zhub.Delays(5000, () -> {
            logger.info("任务2 延时任务执行了!");
        }));

        try {
            while (true) {
                com.zdemo.zhub.Delays delay = delayQueue.take();

                delay.run();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void regTest() {
        // 按指定模式在字符串查找
        String line = "This order was placed for QT3000! OK?";
        String pattern = "(\\D*)(\\d+)(.*)";

        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);

        // 现在创建 matcher 对象
        Matcher m = r.matcher(line);
        if (m.find()) {
            System.out.println("Found value: " + m.group(0));
            System.out.println("Found value: " + m.group(1));
            System.out.println("Found value: " + m.group(2));
            System.out.println("Found value: " + m.group(3));
        } else {
            System.out.println("NO MATCH");
        }
    }

    @Test
    public void timersTest() {
        Timers.tryDelay(() -> {
            logger.info("xx:" + System.currentTimeMillis());
            return true;
        }, 1000, 5);

        Timers.delay(() -> {
            System.out.println("11");
        }, 3000);

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
