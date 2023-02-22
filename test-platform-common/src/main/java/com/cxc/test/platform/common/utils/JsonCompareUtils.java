package com.cxc.test.platform.common.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxc.test.platform.common.domain.diff.JsonDiffField;
import com.cxc.test.platform.common.domain.diff.JsonDiffResult;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.regex.Pattern;

public class JsonCompareUtils {

    public static JsonDiffResult compareJson(String json1, String json2, Set<String> ignorePaths) {
        // 需要对ignorePaths做一些处理
        // Pattern.matches("\\$\\.aihao\\[.*\\]", "$.aihao[123]") 才能返回match上，返回true
        Set<String> ignorePathsParsed = new HashSet<>();
        if (CollectionUtils.isNotEmpty(ignorePaths)) {
            ignorePaths.forEach(s -> {
                s = s.replace("$", "\\$")
                    .replace(".", "\\.")
                    .replace("[", "\\[")
                    .replace("]", "\\]");
                s = s.replace("*", ".*");
                ignorePathsParsed.add(s);
            });
        }

        List<JsonDiffField> diffFields = new ArrayList<>();
        compareJson(JSONObject.parse(json1), JSONObject.parse(json2), "$", ignorePathsParsed, diffFields);

        JsonDiffResult jsonDiffResult = new JsonDiffResult();
        if (CollectionUtils.isNotEmpty(diffFields)) {
            jsonDiffResult.setIsEqual(false);
            jsonDiffResult.setDiffFields(diffFields);
        } else {
            jsonDiffResult.setIsEqual(true);
        }

        return jsonDiffResult;
    }

    private static void compareJson(Object obj1, Object obj2, String path, Set<String> ignorePaths, List<JsonDiffField> diffFields) {
        if (obj1 instanceof JSONObject) {
            compareJson((JSONObject) obj1, (JSONObject) obj2, path, ignorePaths, diffFields);
        } else if (obj1 instanceof JSONArray) {
            compareJson((JSONArray) obj1, (JSONArray) obj2, path, ignorePaths, diffFields);
        } else {
            // 其他类型，统统转为string对比
            compareJson(String.valueOf(obj1), String.valueOf(obj2), path, ignorePaths, diffFields);
        }
    }

    private static void compareJson(JSONObject json1, JSONObject json2, String path, Set<String> ignorePaths, List<JsonDiffField> diffFields) {
        boolean comparePass = commonCompare(json1, json2, path, ignorePaths, diffFields);
        if (!comparePass) {
            return;
        }

        // 去1和2中的key全集
        Set<String> mergeKeySet = new HashSet<>(json1.keySet());
        mergeKeySet.addAll(json2.keySet());

        for (String jsonKey : mergeKeySet) {
            compareJson(json1.get(jsonKey), json2.get(jsonKey), path + "." + jsonKey, ignorePaths, diffFields);
        }
    }

    private static void compareJson(JSONArray jsonArray1, JSONArray jsonArray2, String path, Set<String> ignorePaths, List<JsonDiffField> diffFields) {
        boolean comparePass = commonCompare(jsonArray1, jsonArray2, path, ignorePaths, diffFields);
        if (!comparePass) {
            return;
        }

        for (int i = 0; i < jsonArray1.size(); i++) {
            String innerPath = path + "[" + i + "]";
            Object o1 = jsonArray1.get(i);
            Object o2 = jsonArray2.contains(o1) ? jsonArray2.get(jsonArray2.indexOf(o1)) : null;

            boolean innerComparePass = commonCompare(o1, o2, innerPath, ignorePaths, diffFields);
            if (!innerComparePass) {
                continue;
            }

            compareJson(o1, o2, innerPath, ignorePaths, diffFields);
        }

        //数组存在无序的情况，所以在遍历1之后，还需要重新遍历2，反向去1中查一次
        for (int i = 0; i < jsonArray2.size(); i++) {
            String innerPath = path + "[" + i + "]";
            Object o2 = jsonArray2.get(i);
            Object o1 = jsonArray1.contains(o2) ? jsonArray1.get(jsonArray1.indexOf(o2)) : null;

            commonCompare(o1, o2, innerPath, ignorePaths, diffFields);

            //jsonArray1中有，jsonArray2中没有。以及jsonArray1和2中都有的情况不用比了
        }
    }

    private static void compareJson(String json1, String json2, String path, Set<String> ignorePaths, List<JsonDiffField> diffFields) {
        boolean comparePass = commonCompare(json1, json2, path, ignorePaths, diffFields);
        if (!comparePass) {
            return;
        }

        if (!Objects.equals(json1, json2)) {
            JsonDiffField diffField = JsonDiffField.builder()
                .jsonPath(path)
                .leftValue(json1)
                .rightValue(json2)
                .build();
            diffFields.add(diffField);
        }
    }

    // 返回true代表后续需要进一步对比值，返回false代表无需进行下一步值的对比
    private static boolean commonCompare(Object json1, Object json2, String path, Set<String> ignorePaths, List<JsonDiffField> diffFields) {
        // 对比是否通过，以及是否需要进行后续对比
        boolean comparePass = true;

        if (match(path, ignorePaths)) {
            return false;
        }

        if (json1 == null && json2 == null) {
            comparePass = false;
        }

        if (json1 == null && json2 != null) {
            JsonDiffField diffField = JsonDiffField.builder()
                .jsonPath(path)
                .leftValue(null)
                .rightValue(JSONObject.toJSONString(json2))
                .build();
            diffFields.add(diffField);

            comparePass = false;
        }

        if (json2 == null && json1 != null) {
            JsonDiffField diffField = JsonDiffField.builder()
                .jsonPath(path)
                .leftValue(JSONObject.toJSONString(json1))
                .rightValue(null)
                .build();
            diffFields.add(diffField);

            comparePass = false;
        }

        return comparePass;
    }

    private static boolean match(String path, Set<String> ignorePaths) {
        if (CollectionUtils.isEmpty(ignorePaths)) {
            return false;
        }

        for (String ignorePath : ignorePaths) {
            if (Pattern.matches(ignorePath, path)) {
                return true;
            }
        }
        
        return false;
    }

    public static void main(String[] args) {
        String str1 = "{\"username\":\"tom\",\"age\":18,\"address\":[{\"province\":\"上海市\"},{\"city\":\"上海市\"},{\"city\":\"上海市政府\"},{\"disrtict\":\"静安区\"}],\"aihao\":[\"打球\",\"唱歌\",\"读书\"]}";
        String str2 = "{\"username\":\"andy\",\"age\":18,\"address\":[{\"province\":\"上海市\"},{\"city\":\"上海市\"},{\"disrtict\":\"静安区\"}],\"aihao\":[\"写作\",\"唱歌\",\"打球\"]}";

        Set<String> ignorePaths = new HashSet<>();
//        ignorePaths.add("$.address");
//        ignorePaths.add("$.*name");
        ignorePaths.add("$.aihao");

        JsonDiffResult jsonDiffResult = compareJson(str1, str2, ignorePaths);
        System.out.println(jsonDiffResult);
    }

}