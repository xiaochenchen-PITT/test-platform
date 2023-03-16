package com.cxc.test.platform.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class CommonUtils {

    public final static List<String> NULL_SAME_STRING_POOL = Arrays.asList("", "null", "无", null);
    public final static List<String> FALSE_SAME_STRING_POOL = Arrays.asList("0", "false", "FALSE", "False", "null", null);
    public final static List<String> TRUE_SAME_STRING_POOL = Arrays.asList("1", "true", "TRUE", "True");

    /**
     * 在同一个pool里的string均认为是相等的
     */
    public static boolean sameStringInPool(String a, String b) {
        boolean nullRet = NULL_SAME_STRING_POOL.contains(a) && NULL_SAME_STRING_POOL.contains(b);
        boolean falseRet = FALSE_SAME_STRING_POOL.contains(a) && FALSE_SAME_STRING_POOL.contains(b);
        boolean trueRet = TRUE_SAME_STRING_POOL.contains(a) && TRUE_SAME_STRING_POOL.contains(b);

        return nullRet || falseRet || trueRet;
    }

    /**
     * "2.0"和"2.00"被认为是相等的
     * @param a 需要为string类型的数字
     * @param b 需要为string类型的数字
     * @return
     */
    private static boolean numberStringEquals(String a, String b) {
        if (StringUtils.equals(a, b)) {
            return true;
        }

        try {
            BigDecimal aa = new BigDecimal(a);
            BigDecimal bb = new BigDecimal(b);

            return aa.compareTo(bb) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean generalEquals(Object a, Object b) {
        // 如果类型相同，值也相同
        if (Objects.equals(a, b)) {
            return true;
        }

        // 如果类型不同，但值和格式是相同的，转为string对比。例如数字2和"2"
        if (StringUtils.equals(String.valueOf(a), String.valueOf(b))) {
            return true;
        }

        // 如果为类型相同，值相同，但是格式不相同，例如"2.0"和"2.00"被认为是相等的
        if (numberStringEquals(String.valueOf(a), String.valueOf(b))) {
            return true;
        }

        // 例如"" 和 null 和 "null"被认为是相等的
        if (sameStringInPool(String.valueOf(a), String.valueOf(b))) {
            return true;
        }

        return false;
    }

    /**
     * 二者相除，返回格式化的百分比
     * 四舍五入，2位小数
     * 例如3/17=17.65%
     * @param a
     * @param b
     * @return
     */
    public static String getPrettyPercentage(Long a, Long b) {
        return String.format("%.2f", ((a.doubleValue() / b.doubleValue()) * 100)) + "%";
    }

    public static String getPrettyDate(Date date) {
        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(date);
    }

    public static Map<String, String> convterToMap(String mapStr) {
        Map<String, String> ret = new HashMap<>();

        if (StringUtils.isEmpty(mapStr) || StringUtils.isEmpty(mapStr.substring(1, mapStr.length() - 1))) {
            return ret;
        }

        String[] mapStrArr = mapStr.substring(1, mapStr.length() - 1).split(",");
        for (String s : mapStrArr) {
            ret.put(s.split("=")[0].trim(), s.split("=")[1].trim());
        }

        return ret;
    }

    /**
     * 调用valueOf(String) 方法进行cast
     * @param source 源数据，字符串类型
     * @param targetType java基础类型（Byte、Character、Short、Integer、Long、Float、Double、Boolean），需要有valueOf(String) 方法
     * @return
     */
    public static Object generalValueOf(Object source, Class<?> targetType) throws Exception {
        if (String.class.equals(targetType)) {
            return String.valueOf(source);
        }

        List<Class> validTypes = Arrays.asList(Byte.class, Character.class, Short.class, Integer.class,
            Long.class, Float.class, Double.class, Boolean.class);
        if (!validTypes.contains(targetType)) {
            return null;
        }

        try {
            Method method = targetType.getMethod("valueOf", String.class);
            Object obj = method.invoke(targetType, String.valueOf(source));
            return obj;
        } catch (Exception e) {
            log.error("failed to generalValueOf cast, ", e);
            throw e;
        }
    }
}
