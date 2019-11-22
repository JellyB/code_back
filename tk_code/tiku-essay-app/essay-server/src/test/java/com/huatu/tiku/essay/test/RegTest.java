package com.huatu.tiku.essay.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhaoxi
 * @Description: 测试类
 * @date 2018/8/3下午3:46
 */
@Slf4j
public class RegTest {
    public static void main(String args[]) throws Exception {
//        String contentReg = "(\"|“)?(.*?)(\"|”)";
        String content = "1.“对这一观点，我表示赞同，理由有以下几点”（1分）";
//                "“销售”和“捷”";

//        String contentReg = "(\"|“)(.*?)(\"|”)";

//        boolean matches = content.matches(contentReg);

//        String scoreReg = "(”（|\"|”|）)(.*?)分";
//
////        String scoreReg = "[0-9]+(.[0-9]{0,1})?";
//        Pattern scorePattern = Pattern.compile(scoreReg);
//
//        Matcher m = scorePattern.matcher(content);
//        while (m.find()) {
//            System.out.println(m.group(1));
//
//            System.out.println(m.group(2));
//        }

//        Pattern p = Pattern.compile(contentReg);
//        Matcher m = p.matcher(content);
//        while (m.find()) {
//            System.out.println(m.group(2));
//        }
        Long zeroPointTimestamps = getZeroPointTimestamps(1546590903000L);
        Long endPointTimestamps = getEndPointTimestamps(1546590903000L);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        String zeroDate = dateFormat.format(zeroPointTimestamps);
        String endDate = dateFormat.format(endPointTimestamps);
        System.out.println(zeroDate);
        System.out.println(endDate);

    }


    /**
     * 获得“ 某天”零点时间戳 获得2点的加上2个小时的毫秒数就行
     * 00:00:01
     */
    public static Long getZeroPointTimestamps(Long timestamps) {
        Long oneDayTimestamps = Long.valueOf(60 * 60 * 24 * 1000);
        long time = timestamps % oneDayTimestamps;
        return timestamps - time - (8 * 60 * 60 * 1000) + 1000;
    }


    /**
     * 获得“ 某天”24点时间戳 获得2点的加上2个小时的毫秒数就行
     * 23:59:59
     */
    public static Long getEndPointTimestamps(Long timestamps) {
        Long oneDayTimestamps = Long.valueOf(60 * 60 * 24 * 1000);
        long time = timestamps % oneDayTimestamps;
        return timestamps - time - (8 * 60 * 60 * 1000) + oneDayTimestamps -1000;
    }


}
