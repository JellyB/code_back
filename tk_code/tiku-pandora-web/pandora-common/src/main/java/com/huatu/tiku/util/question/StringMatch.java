package com.huatu.tiku.util.question;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Created by huangqp on 2018\7\2 0002.
 */
public class StringMatch {
    public static final  String pat = "[^(\u4e00-\u9fa5)(a-z)(A-Z)(0-9)]";
    public static final  String CHINESE_REG = "[^(\u4e00-\u9fa5)]([(\u4e00-\u9fa5)]+)[^(\u4e00-\u9fa5)]";

    public static final Pattern mat = Pattern.compile(pat);
    public static final Pattern CHINESE_PATTERN = Pattern.compile(CHINESE_REG);

    /**
     * 获得试题某个属性的相似度
     * @param checkMap
     * @param target
     * @return
     */
    public static Map<Long,Double> check(Map<Long, String> checkMap, String target){

        TreeMap<Long, Double> map = new TreeMap<>();
        for (Map.Entry<Long, String> entry : checkMap.entrySet()) {
            Long x = entry.getKey();
            String k = entry.getValue();
            //找到了重题
            if (kmp(k,target) >= 0) {
                DecimalFormat df = new DecimalFormat("0.00");
                String similar = df.format((float) target.length() / k.length());
                map.put(x, Double.parseDouble(similar));
            }
        }
        System.out.println("----原题："+target+"----------疑似重题列表如下：----------------");
        map.descendingMap().forEach((t, v) -> {
            System.out.println("重题题干：" + checkMap.get(t) + "          相似度：" + v);
        });
        return map;
    }

    public static void main(String[] args) {
//        Map<Long,String> checkMap = Maps.newHashMap();
//        //新录入的试题
//        String i_0 = "下列属于资本下乡的风险";
//        //新录入的试题
//        String i_2 = "下列属于资本下乡的";
//        //新录入的试题
//        String i_3 = "下列属于资本下乡的风";
//        //新录入的试题
//        String i_1 = "下列属于资本下乡的风险是";
//        checkMap.put(1L, i_0);
//        checkMap.put(2L, i_1);
//        checkMap.put(3L, i_2);
//        checkMap.put(4L, i_3);
//        //库里面已有的试题
//        String target = "下列属于";
//        check(checkMap,target);
        String str = " 下列属于，资23fqdeq3!W:LJP本%232()_@&*$@)(97下乡,的<p><img src='http://www.baidu.com' /></p>风(*&#JHWEF#]][].[se2f1险";
        System.out.println("StringMatch.htmlConvertContent = " + QuestionConvert.htmlConvertContent(str));
        Pattern pattern = CHINESE_PATTERN;
        Matcher matcher = pattern.matcher(str+".");
        int i = 0;
        ArrayList<String> objects = Lists.newArrayList();
        while(matcher.find(i)){
            int indexStart = matcher.start()+1;
            int indexEnd = matcher.end() - 1;
//            if(i < indexStart){
//                System.out.println("str0 = " + str.substring(i,indexStart));
//            }
            if(indexStart < indexEnd){
                System.out.println("str1 = " + str.substring(matcher.start()+1,matcher.end()-1));
                i = indexEnd;
            }
        }
        if(i<str.length()){
            System.out.println("str2 = " + str.substring(i));
        }

    }

    /**
     *删除掉除汉字，数字，字母之外的所以内容(只保留标签外的)
     * @param str
     */
    public static String replaceNotChinese(String str){

        if(StringUtils.isBlank(str)){
            return null;
        }
        str = str.replaceAll("<[^>]+>","").replaceAll("&nbsp"," ").replaceAll("nbsp"," ");
        Matcher mat= StringMatch.mat.matcher(str);

        String replaceNotChinese = mat.replaceAll("");

//        System.out.println("去除非中文后:"+replaceNotChinese);
        return  replaceNotChinese;
    }


    /**
     * KMP算法
     *
     * @param ss 主串
     * @param ps 模式串
     * @return 如果找到，返回在主串中第一个字符出现的下标，否则为-1
     */
    public static int kmp(String ss, String ps) {
        if(StringUtils.isAnyBlank(ss,ps)){
            return -1;
        }
        char[] s = ss.toCharArray();
        char[] p = ps.toCharArray();

        int i = 0; // 主串的位置
        int j = 0; // 模式串的位置
        int[] next = getNext(ps);
        while (i < s.length && j < p.length) {
            //①如果j=-1，或者当前字符匹配成功（即S[i]==P[j]），都令i++，j++
            if (j == -1 || s[i] == p[j]) { // 当j为-1时，要移动的是i，当然j也要归0
                i++;
                j++;
            } else {
                //②如果j!=-1，且当前字符匹配失败（即S[i]!=P[j]），则令i不变，j=next[j]，j右移i-next[j]
                j = next[j];
            }
        }
        return j == p.length ? i - j : -1;
    }

    //优化过后的next数组求法
    public static int[] getNext(String ps) {
        char[] p = ps.toCharArray();
        int[] next = new int[p.length];
        next[0] = -1;
        int j = 0;
        int k = -1;
        while (j < p.length - 1) {
            //p[k]表示前缀，p[j]表示后缀
            if (k == -1 || p[j] == p[k]) {
                //较之前next数组求法，改动在下面4行
                if (p[++j] == p[++k]) {
                    next[j] = next[k];// 当两个字符相等时要跳过
                } else {
                    next[j] = k;//之前只有这一行
                }
            } else {
                k = next[k];
            }
        }
        return next;
    }

    /**
     * 如果所有参数都为空，或者他们都没有汉字部分，则返回true
     * @param css
     * @return
     */
    public static boolean isAllBlank(final CharSequence... css) {
        if (ArrayUtils.isEmpty(css)) {
            return true;
        }
        for (final CharSequence cs : css){
            if (isNotBlank(cs)&&isNotBlank(replaceNotChinese(String.valueOf(cs)))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 查询多个字段的相似值，取最大值
     * @param target
     * @param contents
     */
    public static double getSimilar(String target, String ... contents) {
        double max = 0d;
        for (String content : contents) {
            if(kmp(content,target)>=0){
                DecimalFormat df = new DecimalFormat("0.00");
                String similar = df.format((float) target.length() / content.length());
                if(Double.parseDouble(similar)>max){
                    max = Double.parseDouble(similar);
                }
            }
        }
        return max;
    }

}

