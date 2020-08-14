/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.haogames.core.util;

import org.redkale.boot.LogFileHandler;
import org.redkale.source.DataSource;
import org.redkale.source.FilterFunc;
import org.redkale.source.FilterNode;
import org.redkale.source.Flipper;
import org.redkale.util.Comment;
import org.redkale.util.Reproduce;
import org.redkale.util.SelectColumn;
import org.redkale.util.Utility;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * @author zhangjx
 */
public abstract class Utils {

    public static final String HEADNAME_WS_SNCP_ADDRESS = "WS-SncpAddress";

    private Utils() {
    }

    public static void initLogConfig() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            final PrintStream ps = new PrintStream(out);
            ps.println("handlers = java.util.logging.ConsoleHandler");
            ps.println(".level = FINEST");
            ps.println("java.util.logging.ConsoleHandler.level = FINEST");
            ps.println("java.util.logging.ConsoleHandler.formatter = " + LogFileHandler.LoggingFormater.class.getName());
            LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(out.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当天yyyyMMddHHmmss格式的long值
     *
     * @return yyyyMMddHHmmss格式的long值
     */
    public static long datetime14() {
        LocalDateTime day = LocalDateTime.now();
        return day.getYear() * 10000_000000L + day.getMonthValue() * 100_000000 + day.getDayOfMonth() * 1000000
                + day.getHour() * 10000 + day.getMinute() * 100 + day.getSecond();
    }

    /**
     * 获取当天yyMMddHHmmss格式的long值
     *
     * @return yyMMddHHmmss格式的long值
     */
    public static long datetime12() {
        LocalDateTime day = LocalDateTime.now();
        return day.getYear() % 100 * 10000_000000L + day.getMonthValue() * 100_000000 + day.getDayOfMonth() * 1000000
                + day.getHour() * 10000 + day.getMinute() * 100 + day.getSecond();
    }

    public static int[] calcIndexWeights(int[] weights) {
        int size = 0;
        for (int w : weights) {
            size += w;
        }
        int[] newWeights = new int[size];
        int index = -1;
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i]; j++) {
                newWeights[++index] = i;
            }
        }
        return newWeights;
    }

    /**
     * 判断对象是否为空
     *
     * @param obj 待判断的对象
     * @return
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null)
            return true;
        if (obj instanceof String)
            return ((String) obj).trim().isEmpty();
        if (obj instanceof Collection)
            return ((Collection) obj).isEmpty();
        if (obj instanceof Map)
            return ((Map) obj).isEmpty();

        if (obj.getClass().isArray() && Array.getLength(obj) == 0) {
            return true;
        }

        /*if (obj instanceof Object[]) {
            for (Object o : (Object[]) obj) {
                if (o != null) return false;
            }
            return true;
        }*/
        return false;
    }

    public static byte[] encodeBySHA1(String key, String content) {
        SecretKeySpec signKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String encodeByBase64WithUrlSafe(byte[] content) {
        return Base64.getEncoder().encodeToString(content).replaceAll("\\+", "-").replace("/", "_");
    }


    private static Map<String, Reproduce> reproduceMap = new HashMap<>();

    /**
     * @param d   目标对象
     * @param s   源对象
     * @param <D> 目标对象的数据类型
     * @param <S> 源对象的数据类型
     * @return
     */
    public static <D, S> D copy(D d, S s) {
        String reproductKey = d.getClass().getName() + "_" + s.getClass().getName();

        Reproduce<D, S> reproduce = reproduceMap.get(reproductKey);
        if (reproduce == null) {
            if (reproduce == null) {
                reproduceMap.put(reproductKey, reproduce = (Reproduce<D, S>) Reproduce.create(d.getClass(), s.getClass()));
            }
        }

        return reproduce.apply(d, s);
    }

    /**
     * 将字符串第一个字母转大写
     *
     * @param str 待转换字符串
     * @return
     */
    public static String toUpperCaseFirst(String str) {
        Objects.requireNonNull(str);
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 将字符串第一个字母转小写
     *
     * @param str 待转换字符串
     * @return
     */
    public static String toLowerCaseFirst(String str) {
        Objects.requireNonNull(str);
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 获取子集中最大序号
     *
     * @param codes      参与比较的子集
     * @param parentCode 所属父节点
     * @return
     */
    public static String buildMaxCode(List<String> codes, String parentCode) {
        String maxCode = "";
        //父级为几级
        int parentLevel = isEmpty(parentCode) ? 0 : parentCode.split("-").length;
        //获取下一级编号最大code
        for (int i = 0; i < codes.size(); i++) {
            boolean flag = false;
            if (i == 0) {
                flag = true;
            } else if (maxCode.split("-").length == parentLevel + 1) {
                int endMaxVal = Integer.parseInt(maxCode.split("-")[parentLevel]);
                int endThisVal = Integer.parseInt(codes.get(i).split("-")[parentLevel]);
                flag = endThisVal > endMaxVal;
            }
            if (flag) {
                maxCode = codes.get(i);
            }
        }
        return maxCode;
    }

    /**
     * 获取下个序号[100-100 to 100-101]
     *
     * @param code
     * @return
     */
    public static String buildNextCode(String code) {
        if (!Utility.contains(code, '-')) {
            code = String.valueOf(Integer.parseInt(code) + 1);
        } else {
            String startCode = code.substring(0, code.lastIndexOf('-') + 1);
            String endCode = String.valueOf(Integer.parseInt(code.substring(code.lastIndexOf('-') + 1)) + 1);
            code = startCode + endCode;
        }
        return code;
    }

    /**
     * 判断字符串是否由数字组成
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) return false;

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static <T> List<T> strToArr(String str, Class<T> clazz) {
        if (isEmpty(str)) {
            return new ArrayList<>(0);
        }
        List<T> list = Arrays.stream(str.split(","))
                .filter(f -> !isEmpty(f))
                .map(x -> Kv.toAs(x.trim(), clazz))
                .collect(Collectors.toList());
        return list;
    }

    public static String arrToStr(Object[] array) {
        if (array == null) return "";
        return arrToStr(asList(array));
    }

    public static String arrToStr(Collection<?> array) {
        if (isEmpty(array)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        array.stream().filter(f -> !isEmpty(f)).forEach(x -> builder.append(",").append(x instanceof String ? x : x.toString()));
        return builder.append(",").toString();
    }

    public static List<String> parseHtmlImage(String html) {
        Pattern pattern = Pattern.compile("(?<=(<img\\s?src\\s?=\\s?\"))\\S+\\.[A-Za-z]+");
        Matcher match = pattern.matcher(html);
        List<String> ls = new ArrayList<>();
        while (match.find()) {
            ls.add(match.group());
        }
        return ls;
    }

    /**
     * 根据pattern格式化给定时间
     *
     * @param accessor 指定时间
     * @param pattern  格式化pattern
     * @return
     */
    public static String formatByPattern(TemporalAccessor accessor, String pattern) {
        if (isEmpty(pattern)) pattern = "yyyy-MM-dd";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(accessor);
    }

    /**
     * 获取给定时间距离1970-1-1 00:00:00的毫秒数
     *
     * @param time 时间
     * @return
     */
    public static long getEpochMilliByTime(LocalDateTime time) {
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * @param rs
     * @param type
     * @param <T>
     * @return
     */
    public static <T> List<T> queryList(ResultSet rs, Class<T> type) {
        try {
            List list = new ArrayList();
            ResultSetMetaData metaData = rs.getMetaData();
            int count = metaData.getColumnCount();
            while (rs.next()) {
                Map row = new HashMap();
                for (int i = 1; i <= count; i++) {
                    String columnTypeName = metaData.getColumnTypeName(i);
                    //String columnName = metaData.getColumnName(i);
                    String columnLabel = metaData.getColumnLabel(i);
                    row.put(columnLabel, null);

                    if (rs.getObject(i) != null) {
                        switch (columnTypeName) {
                            case "DATETIME", "TIMESTAMP", "DATE" -> row.put(columnLabel, rs.getTimestamp(i).getTime());
                            default -> row.put(columnLabel, rs.getObject(i));
                        }
                    }
                }
                list.add(Map.class == type ? row : Kv.toBean(row, type));
            }

            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询 第一条的 第一列数据 值
     *
     * @param rs
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T findColumn(ResultSet rs, Class<T> type) {
        try {
            Object v = null;
            while (rs.next()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int count = metaData.getColumnCount();

                for (int i = 1; i <= count; i++) {
                    String columnTypeName = metaData.getColumnTypeName(i);
                    if (rs.getObject(i) != null) {
                        switch (columnTypeName) {
                            case "DATETIME", "TIMESTAMP", "DATE" -> v = rs.getTimestamp(i).getTime();
                            default -> v = rs.getObject(i);
                        }
                    }
                    break;
                }
            }

            return Kv.toAs(v, type);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> void batchQueryExecute(DataSource dataSource, Class<T> clz, SelectColumn column, FilterNode node, Flipper flipper, Consumer<Collection<T>> consumer) {
        Number count = dataSource.getNumberResult(clz, FilterFunc.COUNT, null, node);
        if (count == null)
            return;
        for (int offset = flipper.getOffset(); offset < count.intValue(); offset = offset + flipper.getLimit()) {
            flipper.setOffset(offset);
            consumer.accept(dataSource.queryList(clz, column, flipper, node));
        }
    }

    public static <T> void batchQueryExecute(DataSource dataSource, int limit, Class<T> clz, SelectColumn column, FilterNode node, Consumer<Collection<T>> consumer) {
        batchQueryExecute(dataSource, clz, column, node, new Flipper(limit), consumer);
    }

    public static <T> void batchExecute(Collection<T> data, int limit, Consumer<Collection<T>> consumer) {
        for (int offset = 0; offset < data.size(); offset = offset + limit) {
            consumer.accept(data.stream().skip(offset).limit(limit).collect(Collectors.toCollection(HashSet::new)));
        }
    }

    /**
     * 批量处理数据并返回数据流
     *
     * @param data     总数据
     * @param limit    每批处理数据量
     * @param parallel 是否使用异步处理。大批量使用
     * @param executor 执行器
     * @param <T>      传入对象类型
     * @param <R>      传出对象类型
     * @return <R> 对象流
     */
    public static <T, R> Stream<R> batchStream(Collection<T> data, int limit, boolean parallel, Function<Collection<T>, Stream<R>> executor) {
        Stream.Builder<Integer> builder = Stream.builder();
        for (int offset = 0; offset < data.size(); offset = offset + limit) {
            builder.accept(offset);
        }
        Stream<Integer> offsets = builder.build();
        if (parallel) {
            offsets = offsets.parallel();
        }
        return offsets.flatMap(offset -> executor.apply(data.stream().skip(offset).limit(limit).collect(Collectors.toCollection(ArrayList::new))));
    }

    /**
     * List 混排
     *
     * @param list
     * @return
     */
    public static <T> List<T> mix(List<T> list) {
        int len = list.size();
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            int r = random.nextInt(len);
            if (i == r) continue;

            T x = list.get(i);
            list.set(i, list.get(r));
            list.set(r, x);
        }
        return list;
    }

    @Comment("获取集合随机元素")
    public static <T> List<T> randomItems(List<T> list, int len) {
        List<Integer> randoms = getRandoms(list.size(), len);
        List<T> items = new ArrayList<>(randoms.size());

        randoms.forEach(x -> items.add(list.get(x)));
        return items;
    }

    @Comment("获取随机数集合")
    private static List<Integer> getRandoms(int max, int len) {
        Set<Integer> randoms = new HashSet<>();
        Random random = new Random();
        while (randoms.size() < len && randoms.size() < max) {
            randoms.add(random.nextInt(max));
        }
        List<Integer> list = randoms.stream().collect(Collectors.toList());
        return mix(list);
    }

    /**
     * unicode转中文
     *
     * @param str
     * @return
     */
    public static String unicodeToCn(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }

    /**
     * 计算字符串的字符长度
     *
     * @param value
     * @return
     */
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

    @Comment("解析文本得到 @[用户ID:用户名称] 的用户内容")
    public static List<String> parseNoticeUser(String content) {
        if (isEmpty(content)) {
            return new ArrayList<>(0);
        }

        List<String> ls = new ArrayList<>();
        Pattern compile = Pattern.compile("(?<=@\\[)\\d+:[A-Za-z0-9_\\u2E80-\\u9FFF]+(?=])");
        Matcher matcher = compile.matcher(content);
        while (matcher.find()) {
            ls.add(matcher.group());
        }
        return ls;
    }

    public static void main(String[] args) {
        System.out.println(randomIP());
    }

    public static String randomIP() {
        // aaa.aaa.aaa.aaa
        StringBuilder buf = new StringBuilder();

        Random r = new Random();
        buf.append("x").append(".");
        buf.append(r.nextInt(255)).append(".");
        buf.append(r.nextInt(255)).append(".");
        buf.append(r.nextInt(255));

        int n = r.nextInt(50);//
        System.out.println(n / 10f);

        return buf.toString();
    }

    public static String fmt36(int n) {
        return Integer.toString(n, 36);
    }

    public static String fmt36(long n) {
        return Long.toString(n, 36);
    }

    public static <T, V> Map<T, V> toMap(Collection<V> list, Function<V, T> fun) {
        Map<T, V> map = new HashMap<>(list.size());
        for (V v : list) {
            if (v == null) {
                continue;
            }
            map.put(fun.apply(v), v);
        }
        return map;
    }

    public static <T, V, T2> Map<T, T2> toMap(Collection<V> list, Function<V, T> fun, Function<V, T2> fun2) {
        Map<T, T2> map = new HashMap<>(list.size());
        for (V v : list) {
            if (v == null) {
                continue;
            }
            map.put(fun.apply(v), fun2.apply(v));
        }
        return map;
    }

    public static <T, V> List<V> toList(Collection<T> list, Function<T, V> fun) {
        List<V> list1 = new ArrayList<>(list.size());
        list.forEach(x -> list1.add(fun.apply(x)));
        return list1;
    }

    public static String getHtmlBody(String html) {
        String s = html.replaceAll("\n", "");
        int bodyIndex = s.indexOf("<body>");
        if (bodyIndex > -1) {
            bodyIndex = bodyIndex + 6;
            int lastIndexOf = s.lastIndexOf("</body>");
            if (lastIndexOf < bodyIndex) lastIndexOf = s.length();
            s = s.substring(bodyIndex, lastIndexOf);
        }
        return s;
    }

    public static String getHtmlText(String html) {
        return html.replaceAll("<([^ \\f\\n\\r\\t\\v<]| )+>", "");
    }

    // -----------------
    private static final MessageDigest sha1;
    private static final MessageDigest md5;
    private static final String AES_KEY = "HAOGAME_20200721";
    private static final Cipher aesEncrypter; //加密
    private static final Cipher aesDecrypter; //解密

    static {
        MessageDigest d = null;
        try {
            d = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new Error(ex);
        }
        sha1 = d;
        try {
            d = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new Error(ex);
        }
        md5 = d;

        Cipher cipher = null;
        final SecretKeySpec aesKey = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        } catch (Exception e) {
            throw new Error(e);
        }
        aesEncrypter = cipher;  //加密
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
        } catch (Exception e) {
            throw new Error(e);
        }
        aesDecrypter = cipher; //解密
    }

    //AES加密
    public static String encryptAES(String value) {
        if (value == null || value.isEmpty()) return value;
        try {
            synchronized (aesEncrypter) {
                return Utility.binToHexString(aesEncrypter.doFinal(value.getBytes()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //AES解密
    public static String decryptAES(String value) {
        if (value == null || value.isEmpty()) return value;
        byte[] hex = Utility.hexToBin(value);
        try {
            synchronized (aesEncrypter) {
                return new String(aesDecrypter.doFinal(hex));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
