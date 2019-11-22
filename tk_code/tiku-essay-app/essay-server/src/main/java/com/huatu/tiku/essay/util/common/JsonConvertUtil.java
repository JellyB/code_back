package com.huatu.tiku.essay.util.common;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.tiku.essay.entity.vo.report.Match;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonConvertUtil {


    public static void main(String[] args) throws IOException {
        File file = new File("/Users/huangqingpeng/Documents/3.txt");
        String s = FileUtils.readFileToString(file);
        StringBuilder stringBuilder = new StringBuilder(s);
//        countId(stringBuilder);
        String result = convert2Zero(stringBuilder.toString(), "id", "paperId", "questionDetailId", "correspondingId", "questionId", "questionBaseId", "pid", "relationId");
        File file1 = new File("/Users/huangqingpeng/Documents/temp.txt");
        FileUtils.writeStringToFile(file1,result);
        System.out.println(result);
    }

    public static String convert2Zero(String source,String... keys){
        Map<String, Integer> map = Arrays.stream(keys).collect(Collectors.toMap(i -> i, i -> 0));
        return convert2AnyInt(source,map);
    }


    public static String convert2AnyInt(String source, Map<String,Integer> map){
        StringBuilder stringBuilder = new StringBuilder(source);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            convert2AnyInt(stringBuilder,entry.getKey(),entry.getValue());
        }
        return stringBuilder.toString();
    }

    public static void convert2AnyInt(StringBuilder sb, String key, Integer value) {
        Pattern pattern = Pattern.compile("\""+key+"\":"+"([^0][0-9]*)");
        Matcher matcher = pattern.matcher(sb);
        int index = 0;
        while (matcher.find(index)){
            String group = matcher.group(1);
            System.out.println(group + ">>>" + value);
            sb.replace(matcher.start(1),matcher.end(1),value+"");
            index = matcher.end();
        }
        System.out.println("sb.toString() = " + sb.toString());

    }

    public static void countId(StringBuilder sb){
        Pattern pattern = Pattern.compile("\"([^\"]*[I|i]d)\":"+"[0-9]*");
        Matcher matcher = pattern.matcher(sb);
        int index = 0;
        int total = 0;
        Set<String> ids = Sets.newHashSet();
        while (matcher.find(index)){
            String group = matcher.group(1);
            System.out.println(group + ">>>");
            ids.add(group);
            total ++;
            index = matcher.end();
        }
        System.out.println("ids = " + ids);
        System.out.println("total = " + total);
    }
}
