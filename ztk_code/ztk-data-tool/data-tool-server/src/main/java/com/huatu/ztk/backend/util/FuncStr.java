package com.huatu.ztk.backend.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.huatu.ztk.backend.util.UploadFileUtil.IMG_BASE_URL;

/**
 * Created by ht on 2016/11/23.
 */
public class FuncStr {
    static synchronized public String GetGUID() {
        try {
            SecureRandom seeder;
            String midValue;
            String midValueUnformated;
            StringBuffer stringbuffer = new StringBuffer();
            StringBuffer stringbuffer1 = new StringBuffer();
            seeder = new SecureRandom();

            int i = 0;
            int j = 24;
            try {
                InetAddress inetaddress = InetAddress.getLocalHost();
                byte abyte0[] = inetaddress.getAddress();
                for (int k = 0; j >= 0; k++) {
                    int l = abyte0[k] & 0xff;
                    i += l << j;
                    j -= 8;
                }
            } catch (Exception exception) {
                i = seeder.nextInt();
            }
            String s = ToHex(i, 8);
            String s1 = ToHex(Integer.toString(i).hashCode(), 8);


            stringbuffer1.append(s.substring(0, 4));
            stringbuffer1.append(s.substring(4));
            stringbuffer1.append(s1.substring(0, 4));
            stringbuffer1.append(s1.substring(4));
            stringbuffer.append("-");
            stringbuffer.append(s.substring(0, 4));
            stringbuffer.append("-");
            stringbuffer.append(s.substring(4));
            stringbuffer.append("-");
            stringbuffer.append(s1.substring(0, 4));
            stringbuffer.append("-");
            stringbuffer.append(s1.substring(4));

            midValue = stringbuffer.toString();
            long l = System.currentTimeMillis();
            int i1 = (int) l & 0xffffffff;
            int j1 = seeder.nextInt();
            midValue = "{" + ToHex(i1, 8) + midValue + ToHex(j1, 8) + "}";

            midValueUnformated = stringbuffer1.toString();
            int i2 = (int) l & 0xffffffff;
            int j2 = seeder.nextInt();
            midValueUnformated = ToHex(i2, 8) + midValueUnformated
                    + ToHex(j2, 8);



            int GUIDHashCode = midValue.hashCode();
            if (GUIDHashCode < 0) {
                GUIDHashCode = -GUIDHashCode;
            }
            return Integer.toString(GUIDHashCode);
        } catch (Exception exception) {
            Calendar NewDate = Calendar.getInstance();
            int Year, Month, Day, Week, Hours, Minutes, Seconds;
            long Time;
            Year = NewDate.get(Calendar.YEAR) + 1900;
            String GUID = String.valueOf(Year);
            Month = NewDate.get(Calendar.MONTH) + 1;
            if (Month < 10) {
                GUID = GUID + "0" + String.valueOf(Month);
            } else {
                GUID = GUID + String.valueOf(Month);
            }
            Day = NewDate.get(Calendar.DAY_OF_MONTH);
            if (Day < 10) {
                GUID = GUID + "0" + String.valueOf(Day);
            } else {
                GUID = GUID + String.valueOf(Day);
            }
            Week = NewDate.get(Calendar.DAY_OF_WEEK);
            GUID = GUID + String.valueOf(Week);
            Hours = NewDate.get(Calendar.HOUR);
            if (Hours < 10) {
                GUID = GUID + "0" + String.valueOf(Hours);
            } else {
                GUID = GUID + String.valueOf(Hours);
            }
            Minutes = NewDate.get(Calendar.MINUTE);
            if (Minutes < 10) {
                GUID = GUID + "0" + String.valueOf(Minutes);
            } else {
                GUID = GUID + String.valueOf(Minutes);
            }
            Seconds = NewDate.get(Calendar.SECOND);
            if (Seconds < 10) {
                GUID = GUID + "0" + String.valueOf(Seconds);
            } else {
                GUID = GUID + String.valueOf(Seconds);
            }
            Time = NewDate.getTimeInMillis();
            if (Time < 10) {
                GUID = GUID + "0" + String.valueOf(Time);
            } else {
                GUID = GUID + String.valueOf(Time);
            }

            long l = System.currentTimeMillis();
            while (System.currentTimeMillis() == l) {
                l = System.currentTimeMillis();
            }
            return GUID;
        }
    }

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
     * 替换标签
     * @param str
     * @return
     */
    public static String htmlManage(String str){
        if (StringUtils.isNotEmpty(str)) {
            String regex="<p>|</p>|<br>|</br>|<u>|</u>|<br/>|";
            str = str.replaceAll(regex,"");
            // <!--[img style="height:202px; width:534px"]1486134276484.png[/img]-->
            StringBuilder sb = new StringBuilder(str);
            Pattern pattern = Pattern.compile("<!--\\[img[^\\]]*\\]([^\\[]*)\\[/img\\]-->");
            Matcher matcher = pattern.matcher(sb);
            int i = 0;
            while(matcher.find(i)){
                String img =  "<img src=\""+IMG_BASE_URL+matcher.group(1).substring(0,1)+ "/"+matcher.group(1)+"\"/>";
                sb = sb.replace(matcher.start(),matcher.end(),img);
                i = matcher.start() + img.length();
            }
            return sb.toString();
        }else{
            return "";
        }
    }
    public static String replaceHtml(String value){
        if(com.alibaba.dubbo.common.utils.StringUtils.isNotEmpty(value)){
            String regex="<p>|</p>|<br>|</br>|<u>|</u>|<br/>";//去除html中这几种标签
            value = value.replaceAll(regex,"");
            value = value.replaceAll("<img.*>.*</img>", "【图片】").replaceAll("<img.*/>", "【图片】").replaceAll("<img.*>", "【图片】");//img标签替换为【图片】
            return value;
        }
        return "";
    }

    public static List<Integer> castToList(String value){
        List<Integer> ints=Lists.newArrayList();
        if(StringUtils.isNotEmpty(value)){
            String[] s=value.split(",");
            for (String str:s){
                ints.add(Integer.parseInt(str));
            }
        }
        return ints;
    }

    /**
     * 替换斜线
     * @param str
     * @return
     */
    public static String replaceDiagonal(String str){
        if (StringUtils.isNotEmpty(str)) {
            int index=str.indexOf("/");
            if(index>0){
                str = str.replaceAll("/","、");
            }
            return str;
        }else{
            return "";
        }
    }

    public static boolean checkIsNotNull(String value){
        if(StringUtils.isNotEmpty(value)){
            return true;
        }
        return false;
    }

    public static void SortMap(){

        List<QuestionExtend> list = Lists.newArrayList();
        list.add(QuestionExtend.builder().qid(115807).sequence(1.0f).build());
        list.add(QuestionExtend.builder().qid(115809).sequence(3.0f).build());
        list.add(QuestionExtend.builder().qid(115808).sequence(2.0f).build());
        list.add(QuestionExtend.builder().qid(115811).sequence(5.0f).build());
        list.add(QuestionExtend.builder().qid(115813).sequence(6.0f).build());
        list.add(QuestionExtend.builder().qid(115812).sequence(51.0f).build());
        list.add(QuestionExtend.builder().qid(115815).sequence(26.0f).build());
        list.add(QuestionExtend.builder().qid(115814).sequence(66.0f).build());
        list.add(QuestionExtend.builder().qid(115817).sequence(7.0f).build());
        list.add(QuestionExtend.builder().qid(115816).sequence(52.0f).build());
        list.add(QuestionExtend.builder().qid(115819).sequence(53.0f).build());

            System.out.println("Original...");
            for(QuestionExtend q:list){
                System.out.println("qid:"+q.getQid()+"sequence:"+q.getSequence());
            }
            Map<String, Float> result = new LinkedHashMap<>();


        list.sort((QuestionExtend q1,QuestionExtend q2)-> (new Float(q1.getSequence())).compareTo(new Float(q2.getSequence())));

            System.out.println("Sorted...");
        for(QuestionExtend q:list){
            System.out.println("qid:"+q.getQid()+"sequence:"+q.getSequence());
        }

    }

    public static Map<String,Integer> sortMap(String content){
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        Map<String,Integer> map= Maps.newHashMap();
        map.put("png",content.indexOf("png"));
        map.put("jpg",content.indexOf("jpg"));
        map.put("gif",content.indexOf("gif"));
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

    public static void main(String[] args){
        //String str="2009年5省市联考(福建/海南/辽宁/重庆/内蒙古)《行测》真题";
        String str="2009年内蒙古(上半年)《行测》真题";
        System.out.println(FuncStr.replaceDiagonal(str));

        //FuncStr.SortMap();
        String content="<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/c/c1b855885eda4e1d0bd342b112cefc02.jpg\" width=\"268\" height=\"184\"></img>将正方体展开，如上图，过F点作FO垂直于C1E与O点，FO的线段长度是爬行的最短距离，由于△FC1O与△C1EC都有一个直角，且由于A1C1与AC平行∠FC1O=∠C1EC，△FC1O与△C1EC是相似三角形。那么<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/5/5eafa4cabd4b60dcfa60ab0562a43ffc.jpg\" width=\"140\" height=\"93\"></img>，<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/4/444e08d0794d076231fc78feb4584f54.jpg\" width=\"262\" height=\"59\"></img>，<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/9/9644cc60ce6de5417d973724d240162d.jpg\" width=\"140\" height=\"95\"></img>，解得<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/8/89dd92bc0dd143d9cd721325d76e722d.jpg\" width=\"114\" height=\"46\"></img>。答案是B选项。注：FC1=<img src=\"http://tiku.huatu.com/cdn/images/vhuatu/tiku/9/904fb2b723279102f0d0a0f099f79556.png\" width=\"67\" height=\"45\"></img>是最短距离，但是本题主要考察点到线段垂直距离最短。";
        Map<String,Integer> sortedMap=FuncStr.sortMap(content);
        for (String s:sortedMap.keySet()){
            System.out.println("key="+s+"value=="+sortedMap.get(s));
        }
    }
}
