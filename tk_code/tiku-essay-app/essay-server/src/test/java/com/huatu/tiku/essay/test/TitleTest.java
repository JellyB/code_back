package com.huatu.tiku.essay.test;

import com.huatu.tiku.essay.entity.EssayUserCorrectGoods;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.TextUtils;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.aspectj.util.FileUtil;
import org.assertj.core.util.Lists;
import org.junit.Test;
import sun.net.www.protocol.file.FileURLConnection;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhaoxi
 * @Description: 段落切分测试类
 * @date 2018/8/3下午3:46
 */
@Slf4j
public class TitleTest {
    public static void main(String args[]) throws Exception {
        String answerContent = "好";
        StringBuilder title = new StringBuilder();
        StringBuilder content = new StringBuilder();
        if (StringUtils.isNotEmpty(answerContent)) {
            List<String> paragraphList = cutParagraph(answerContent);
            if (CollectionUtils.isNotEmpty(paragraphList)) {
                for (int i = 0; i < paragraphList.size(); i++) {
                    if (paragraphList.get(i).length() <= 30 && i < 2) {
                        title.append(paragraphList.get(i) + "<br/>");
                    } else {
                        content.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + paragraphList.get(i) + "<br/>");
                    }
                }

            }
        }
        log.info("11111" + changeToChineseSym(delBrLabel(title.toString())));
        log.info("22222" + changeToChineseSym(delBrLabel(content.toString())));

    }

    private static String changeToChineseSym(String content) {
        content = content.replaceAll("\\(", "（").replaceAll("\\)", "）");

        return content;
    }

    /**
     * 截取掉结尾的br标签
     */
    private static String delBrLabel(String content) {
        if (content.endsWith("<br/>")) {
            int i = content.lastIndexOf("<br/>");
            content = content.substring(0, i);
        }

        return content;
    }

    /**
     * 切段落
     *
     * @param userAnswer
     * @return
     */
    private static List<String> cutParagraph(String userAnswer) {
        int[] charAscii = {160, 8232, 12288};
        char ch = (char) charAscii[0];
        String regex = ch + "";
        for (int i = 1, len = charAscii.length; i < len; i++) {
            regex += ("|" + charAscii[i]);
        }
        List<String> strings = new LinkedList<>();
        String content = userAnswer.replaceAll(regex, " ");
        String[] paragraphStrs = content.split("\\n{1,}|\\s{2,}");
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        for (int i = 0, len = paragraphStrs.length; i < len; i++) {
            if (paragraphStrs[i].trim().length() >= 1) {
                int start = findStart(0, content, paragraphStrs[i].trim(), starts, ends);
                int end = start + paragraphStrs[i].trim().length();
                starts.add(start);
                ends.add(end);
//                log.info("第{}段，内容为：{}",i,content.substring(start,end));
                strings.add(content.substring(start, end));

            }
        }
        return strings;
    }


    /**
     * @param firstStart 初始位置
     * @param firstStr
     * @param secondStr
     * @param starts
     * @param ends
     * @return
     */
    private static int findStart(int firstStart, String firstStr, String secondStr, List<Integer> starts, List<Integer> ends) {
        int len = secondStr.length();
        int len1 = firstStr.length();
        int start = firstStr.indexOf(secondStr) + firstStart;
        int end = start + len;
        while (isContainStart(start, end, starts, ends) && (start - firstStart) < len1) {
            start = firstStr.indexOf(secondStr, end - firstStart) + firstStart;
            end = start + len;
        }
        return start;
    }

    /**
     * 是否该起始终止位置已经被用
     *
     * @param start
     * @param starts
     * @param ends
     * @return
     */
    private static boolean isContainStart(int start, int end, List<Integer> starts, List<Integer> ends) {
        boolean result = false;
        for (int i = 0, size = starts.size(); i < size; i++) {
            int start1 = starts.get(i);
            int end1 = ends.get(i);
            if ((start1 <= start && end1 > start) || (start1 < end && end1 >= end)) {
                result = true;
            }
        }
        return result;
    }


    @Test
    public void test2() {
        File file = new File("/Users/huangqingpeng/Documents/1.txt");
        String temp = "";
        try {
            temp = FileUtil.readAsString(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String asciiFromHexStr = getASCIIFromHexStr(temp);
        System.out.println("asciiFromHexStr = " + asciiFromHexStr);
    }

    public static String getASCIIFromHexStr(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile("(\\\\[x|X]([(0-9)|(A-F)]{2}))+");
        Matcher matcher = pattern.matcher(str);
        int index = 0;
        while (matcher.find(index)) {
            int start = matcher.start();
            if (start > index) {
                sb.append(str, index, start);
            }
            String group = matcher.group();
            System.out.println("group = " + group);
            String[] split = group.split("\\\\");
            byte[] byteArr = new byte[split.length - 1];
            for (int i = 1; i < split.length; i++) {
                Integer hexInt = Integer.decode("0" + split[i]);
                byteArr[i - 1] = hexInt.byteValue();
            }
            try {
                String s = new String(byteArr, "UTF-8");
                sb.append(s);
                System.out.println(s);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            index = matcher.end();
        }
        if (index < str.length()) {
            sb.append(str.substring(index));
        }
        return sb.toString();
    }

    @Test
    public void test5() {
        int result = Integer.parseInt("E4BD", 16);
        System.out.println("result = " + (char) result);

    }

}
