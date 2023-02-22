package com.cxc.test.platform.common.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
}
