package com.haogames.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author: liangxianyou
 */
public class IpKit {

    public static String toNum(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "";
        }
        if (isV4(ip)) {
            return v4Num(ip) + "";
        } else {
            return v6Num(ip);
        }
    }


    /**
     * IP区间包含IP数计算，含首尾
     *
     * @param startIp 其实ip
     * @param endIp   结束ip
     * @return
     */
    public static String ipCount(String startIp, String endIp) {
        if (startIp == null || startIp.isEmpty() || endIp == null || endIp.isEmpty()) {
            return "";
        }
        if (isV4(startIp, endIp)) {
            return v4Count(startIp, endIp) + "";
        } else {
            return v6Count(startIp, endIp);
        }
    }

    public static boolean isV4(String... ips) {
        for (String ip : ips) {
            String pattern = "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$";
            if (!Pattern.matches(pattern, ip)) {
                return false;
            }

            /*if (!ip.contains(".")) {
                return false;
            }*/
        }
        return true;
    }

    //------------------------------  ipv4 ----------------------------------

    /**
     * ipv4 区间包含Ip数，含首尾
     *
     * @param startIp
     * @param endIp
     * @return
     */
    private static String v4Count(String startIp, String endIp) {
        String start = v4Num(startIp);
        String end = v4Num(endIp);

        String v = subtract(end, start);
        if (v.startsWith("-")) {
            v = v.substring(1);
        }
        v = add(v, "1");
        return v;
    }

    /**
     * ipv4转数值
     *
     * @param ip ipv4
     * @return
     */
    private static String v4Num(String ip) {
        String[] ipArr = ip.trim().split("[.]");
        int[] vs = {16777216, 65536, 256, 1};
        String v = "0";
        for (int i = 0; i < 4; i++) {
            // v += vs[i] * Integer.parseInt(ipArr[i]);
            v = add(v, ride(vs[i] + "", ipArr[i]));
        }
        return v;
    }

    //------------------------------  ipv6 ----------------------------------

    /**
     * ipv6 区间包含Ip数，含首尾
     *
     * @param startIp
     * @param endIp
     * @return
     */
    private static String v6Count(String startIp, String endIp) {
        String start = v6Num(startIp);
        String end = v6Num(endIp);
        String subtract = subtract(start, end);
        if (subtract.startsWith("-")) {
            subtract = subtract.substring(1);
        }
        subtract = add(subtract, "1");
        return subtract;
    }

    /**
     * 将ipv6转为数值
     *
     * @param ip ipv6
     * @return ipv6 转换后的数值
     */
    private static String v6Num(String ip) {
        ip = restoreV6(ip);

        int[] ipArr = new int[8];
        String[] ipSlice = ip.split(":");

        for (int i = 0; i < ipSlice.length; i++) {
            ipArr[i] = Integer.parseInt(ipSlice[i], 16);
        }

        String[] baseNum = {
                "5192296858534827628530496329220096",
                "79228162514264337593543950336",
                "1208925819614629174706176",
                "18446744073709551616",
                "281474976710656",
                "4294967296",
                "65536",
                "1"};


        String v = "0";
        for (int i = 0; i < 8; i++) {
            v = add(v, ride(baseNum[i], ipArr[i] + ""));
        }
        return v;
    }


    //=================================================================

    /**
     * 两任意大小正整数相乘
     *
     * @param x 正整数 x
     * @param y 正整数 y
     * @return 返回乘积数字 字符串
     */
    private static String ride(String x, String y) {
        List<List<Integer>> tmp = new ArrayList<>();

        List<Integer> xArr = new ArrayList<>();
        List<Integer> yArr = new ArrayList<>();
        for (String s : String.valueOf(x).split("")) {
            xArr.add(Integer.parseInt(s));
        }
        for (String s : String.valueOf(y).split("")) {
            yArr.add(Integer.parseInt(s));
        }

        //分步 相乘
        int z = 0;
        for (int i = xArr.size() - 1; i >= 0; i--, z++) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < z; j++) {
                list.add(0);
            }

            int[] carry = {0};
            int a = xArr.get(i);
            for (int j = yArr.size() - 1; j >= 0; j--) {
                int b = yArr.get(j);
                list.add(a * b % 10 + carry[0]);
                carry[0] = a * b / 10;
            }
            if (carry[0] > 0) {
                list.add(carry[0]);
            }

            tmp.add(list);
        }

        //合并 相加
        String v = "";
        int carry = 0;
        boolean end = false;
        for (int i = 0; ; i++) {
            end = true;
            int _v = 0;
            for (int j = 0; j < tmp.size(); j++) {
                List<Integer> row = tmp.get(j);
                if (row.size() > i) {
                    end = false;
                    _v += row.get(i);
                }
            }
            if (end) {
                break;
            }

            _v = carry + _v;
            v = _v % 10 + v;
            carry = _v / 10;
        }
        if (carry > 0) {
            v = carry + v;
        }
        return v;
    }

    /**
     * 两任意大小正整数 相加
     *
     * @param x 正整数 x
     * @param y 正整数 y
     * @return 返回两数之和 字符串
     */
    private static String add(String x, String y) {
        List<Integer> xArr = toIntSlice.apply(x);
        List<Integer> yArr = toIntSlice.apply(y);

        String v = "";
        int carry = 0;
        for (int i = 0; i < xArr.size() || i < yArr.size(); i++) {
            int a = i < xArr.size() ? xArr.get(i) : 0;
            int b = i < yArr.size() ? yArr.get(i) : 0;

            int _v = a + b + carry;
            if (_v >= 10) {
                carry = 1;
                _v -= 10;
            } else {
                carry = 0;
            }
            v = _v + v;
        }
        if (carry > 0) {
            v = carry + v;
        }
        return v;
    }

    private static Function<String, List<Integer>> toIntSlice = (str) -> {
        String[] strArr = str.trim().split("");
        List<Integer> arr = new ArrayList<>();
        for (int i = strArr.length - 1; i >= 0; i--) {
            arr.add(Integer.parseInt(strArr[i]));
        }
        return arr;
    };
    /*private static Function<String, List<Integer>> _toIntSlice = (str) -> {
        String[] strArr = str.trim().split("");
        List<Integer> arr = new ArrayList<>(strArr.length);
        for (int i = 0; i < strArr.length; i++) {
            arr.add(Integer.parseInt(strArr[i]));
        }
        return arr;
    };*/

    /**
     * 任意两个大小正整数相减
     *
     * @param x 正整数 x
     * @param y 正整数 y
     * @return x-y 的差
     */
    private static String subtract(String x, String y) {
        List<Integer> xArr = toIntSlice.apply(x);
        List<Integer> yArr = toIntSlice.apply(y);

        // 值大小比较
        boolean yThanX = xArr.size() < yArr.size();
        if (xArr.size() == yArr.size()) {
            for (int i = xArr.size() - 1; i >= 0; i--) {
                if (xArr.get(i) > yArr.get(i)) {
                    yThanX = false;
                    break;
                } else if (xArr.get(i) < yArr.get(i)) {
                    yThanX = true;
                    break;
                }
            }
        }
        if (yThanX) {
            List<Integer> tmp = xArr;
            xArr = yArr;
            yArr = tmp;
        }

        // 计算
        String v = "";
        int subplus = 0; // 如：-1
        for (int i = 0; i < xArr.size(); i++) {
            int a = xArr.get(i);
            int b = yArr.size() > i ? yArr.get(i) : 0;

            int _v = a - b + subplus;
            if (_v < 0) {
                subplus = -1;
                _v = _v + 10;
            } else {
                subplus = 0;
            }

            v = _v + v;
        }
        // 去除首位0
        while (v.startsWith("0") && v.length() > 1) {
            v = v.substring(1);
        }
        if (yThanX) {
            v = "-" + v;
        }
        return v;
    }

    /**
     * ipv6还原，去掉压缩写法
     *
     * @param ip 原始ipv6
     * @return 标准IPV6
     */
    private static String restoreV6(String ip) {
        // 计算 :个数
        // 补全 省略的0
        String[] arr = ip.split("");
        int n = 0;
        for (String s : arr) {
            if (":".equals(s)) {
                n++;
            }
        }
        String _ip = ip;
        if (n < 7) {
            String b = "";
            for (int i = 0; i <= 7 - n; i++) {
                b += ":0";
            }
            b += ":";
            _ip = ip.replace("::", b);
        }
        if (_ip.startsWith(":")) {
            _ip = "0" + _ip;
        }
        if (_ip.endsWith(":")) {
            _ip = _ip + "0";
        }
        return _ip;
    }

    // 任意两个小数相加 x: 0.1213, y: 0.981
    /*private static String _add(String x, String y) {
        List<Integer> xArr = _toIntSlice.apply(x.substring(2));
        List<Integer> yArr = _toIntSlice.apply(y.substring(2));


        String v = "";
        int len = xArr.size() > yArr.size() ? xArr.size() : yArr.size();
        int carry = 0;
        for (int i = 0; i < len; i++) {
            int a = xArr.size() > (len - i - 1) ? xArr.get(len - i - 1) : 0;
            int b = yArr.size() > (len - i - 1) ? yArr.get(len - i - 1) : 0;
            int _v = a + b + carry;

            carry = _v / 10;
            v = _v % 10 + v;
        }
        v = carry + v;
        StringBuffer buf = new StringBuffer(v).insert(v.length() - len, ".");

        return buf.toString();
    }

    // 任意两个数相加
    public static String addx(String x, String y) {
        String str = _add(x, y);
        System.out.printf("%s + %s = %s \n", x, y, str);

        return str;
    }*/
}
