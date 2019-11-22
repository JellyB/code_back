package com.huatu.tiku.util.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created by ht on 2016/11/23.
 */
public class FuncStr {

    public static String ToHex(int i, int j) {
        String s = Integer.toHexString(i);
        StringBuffer stringbuffer = new StringBuffer();
        if (s.length() < j) {
            for (int k = 0; k < (j - s.length()); k++)
                stringbuffer.append("0");
            return stringbuffer.toString() + s;
        } else {
            return s.substring(0, j);
        }
    }

    public static String ToHex(String str) {
        if (str.equals("") || (str == null)) {
            return "";
        }

        String hex = "";
        int CharAscNum = 0;
        byte[] bytes = str.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            CharAscNum = bytes[i];
            hex = hex + Integer.toHexString(CharAscNum).replaceAll("ff", "");
        }
        return hex;
    }

    /**
     * 将<span><span/> 标签中涉及表达式的替换为img标签，并过滤掉不用的标签
     *
     * @param str
     * @return
     */
    public static String htmlManage(String str) {
        if (StringUtils.isNotEmpty(str)) {
            str = HtmlConvertUtil.span2Img(str, false);
            String regex = "<p>|</p>|<br>|</br>|<br/>|";
            str = str.replaceAll(regex, "");
            return str;
        } else {
            return "";
        }
    }

    public static String replaceHtml(String value) {
        if (StringUtils.isNotBlank(value)) {
            String regex = "<p>|</p>|<br>|</br>|<u>|</u>|<br/>";//去除html中这几种标签
            value = value.replaceAll(regex, "");
            value = value.replaceAll("<img.*>.*</img>", "【图片】").replaceAll("<img.*/>", "【图片】").replaceAll("<img.*>", "【图片】");//img标签替换为【图片】
            return value;
        }
        return "";
    }

    public static List<Integer> castToList(String value) {
        List<Integer> ints = Lists.newArrayList();
        if (StringUtils.isNotEmpty(value)) {
            String[] s = value.split(",");
            for (String str : s) {
                ints.add(Integer.parseInt(str));
            }
        }
        return ints;
    }

    /**
     * 替换斜线
     *
     * @param str
     * @return
     */
    public static String replaceDiagonal(String str) {
        if (StringUtils.isNotEmpty(str)) {
            int index = str.indexOf("/");
            if (index > 0) {
                str = str.replaceAll("/", "、");
            }
            return str;
        } else {
            return "";
        }
    }

    public static boolean checkIsNotNull(String value) {
        if (StringUtils.isNotEmpty(value)) {
            return true;
        }
        return false;
    }

    public static Map<String, Integer> sortMap(String content) {
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        Map<String, Integer> map = Maps.newHashMap();
        map.put("png", content.indexOf("png"));
        map.put("jpg", content.indexOf("jpg"));
        map.put("gif", content.indexOf("gif"));
        List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(
                map.entrySet());
        Collections.sort(entryList, Comparator.comparing(Map.Entry::getValue));
        Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();
        Map.Entry<String, Integer> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public static void main(String[] args) {
        //String str="2009年5省市联考(福建/海南/辽宁/重庆/内蒙古)《行测》真题";
        String str = "2009年内蒙古(上半年)《行测》真题";
        System.out.println(FuncStr.replaceDiagonal(str));

        //FuncStr.SortMap();
        String content = "<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/c/c1b855885eda4e1d0bd342b112cefc02.jpg\" width=\"268\" height=\"184\"></img>将正方体展开，如上图，过F点作FO垂直于C1E与O点，FO的线段长度是爬行的最短距离，由于△FC1O与△C1EC都有一个直角，且由于A1C1与AC平行∠FC1O=∠C1EC，△FC1O与△C1EC是相似三角形。那么<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/5/5eafa4cabd4b60dcfa60ab0562a43ffc.jpg\" width=\"140\" height=\"93\"></img>，<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/4/444e08d0794d076231fc78feb4584f54.jpg\" width=\"262\" height=\"59\"></img>，<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/9/9644cc60ce6de5417d973724d240162d.jpg\" width=\"140\" height=\"95\"></img>，解得<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/8/89dd92bc0dd143d9cd721325d76e722d.jpg\" width=\"114\" height=\"46\"></img>。答案是B选项。注：FC1=<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/9/904fb2b723279102f0d0a0f099f79556.png\" width=\"67\" height=\"45\"></img>是最短距离，但是本题主要考察点到线段垂直距离最短。";
        Map<String, Integer> sortedMap = FuncStr.sortMap(content);
        for (String s : sortedMap.keySet()) {
            System.out.println("key=" + s + "value==" + sortedMap.get(s));
        }
    }
}
