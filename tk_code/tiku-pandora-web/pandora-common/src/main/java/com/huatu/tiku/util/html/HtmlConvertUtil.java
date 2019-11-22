package com.huatu.tiku.util.html;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.util.file.FormulaConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import tk.mybatis.mapper.util.StringUtil;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huatu.tiku.constant.BaseConstant.QUESTION_PARSE_ERROR;
import static com.huatu.tiku.service.impl.BaseServiceImpl.throwBizException;
import static java.util.regex.Pattern.compile;

/**
 * 富文本标签解析或者包装的工具类
 * Created by huangqp on 2018\5\17 0017.
 */
@Slf4j
public class HtmlConvertUtil {

    /**
     * 表达式图片链接模板(前端展示所用)
     */
    //private static final String formula_link = "<img src=\"http://latex.codecogs.com/gif.latex?{}\"  />";
    private static final String formula_link = "<img src=\"http://private.codecogs.com/gif.latex?\\inline&space;{}\"  />";
    /**
     * 表达式图片链接模板（客户端展示、mongog存储所用）
     */
    private static final String formula_link_with_size = "<img src=\"{}\" style={}/>";

    /**
     * 表达式图片链接（生成cdn链接所用）
     */
    //private static final String formula_link_url = "http://latex.codecogs.com/gif.latex?&space;{}";
    private static final String formula_link_url = "http://private.codecogs.com/gif.latex?\\inline&space;{}";
    public static final ErrorResult ANALYSIS_FAILURE = ErrorResult.create(1000602, "解析失败");
    /**
     * 批量录入拆分字符串标识（依次判断下一个结点是否开始）
     */
    public static final String STRING_SEPARATOR = "\n";
    /**
     * 选项内容开头标识正则(回车换行空白符+选项字符+非选项字符)
     */
    public static final String CHOICE_HEAD_FLAG = "([\r|\n|\\s]?)([A-Z])[^(a-z)(A-Z)(0-9)(\u4e00-\u9fa5)]";

    /**
     * 将复制的选项内容解析拆分成多个
     * 拆分规则：
     * 1、以A开头作为选项的开始标识
     * 2、当匹配到第二个 空字符（回车，换行，空白字符）+ 字母字符 + 非字母|数字|汉字字符 时，作为第二个适配点
     * 3、判断适配点的字母部分是不是按顺序计算的那个字符，如果是，则作为第二个选项的开始标识，依次类推……
     *
     * @param choiceContent
     * @return
     */
    public static List<Map<String, Object>> parseChoiceContent(String choiceContent) {
        choiceContent = choiceContent.replaceAll("<br>", "\n");
        //去除标签后的字符串
        StringBuilder stringBuilder = new StringBuilder(giveUpTag(choiceContent));
        //选项匹配
        Pattern pattern = compile(CHOICE_HEAD_FLAG);
        Matcher matcher = pattern.matcher(stringBuilder);
        int i = 0;
        char choice = 'A';
        int start = 0;
        List<Map<String, Object>> result = Lists.newArrayList();
        while (matcher.find(i)) {
            /**
             * 从选项A开始，match.start()==0,match.group(1)可以为空
             * 其他选项开始前,match.start()>0,match.group(1)不能为空，即必须匹配到/n|/r|//s字符，不然不是选项
             */
            if (matcher.start() > 0 && StringUtil.isEmpty(matcher.group(1))) {
                i = matcher.end() - 1;    //防止非空字符被前一个字符匹配到
                continue;
            }
            if (choice == matcher.group(2).charAt(0)) {
                //从B选项开始，截取从上一选项到才选项开始出的内容作为上一选项内容
                if (choice != 'A') {
                    Map map = Maps.newHashMap();
                    map.put("key", (char) (choice - 1));
                    map.put("value", "<p>" + stringBuilder.substring(start, matcher.start()).trim() + "</p>");
                    result.add(map);
                }
                start = matcher.end();
                choice += 1;
            }
            i = matcher.end();
        }
        if (choice > 'A') {
            Map map = Maps.newHashMap();
            map.put("key", (char) (choice - 1));
            map.put("value", "<p>" + stringBuilder.substring(start).trim() + "</p>");
            result.add(map);
        } else {
            throw new BizException(ANALYSIS_FAILURE);
        }
        return result;
    }

    /**
     * 去掉除img和span之外的所有标签
     * span标签只保留 含有“mathquill-embedded-latex”的
     * <span class="mathquill-embedded-latex" style="width: 37px; height: 39px;">\oint_1^2</span>
     *
     * @param content
     * @return
     */
    public static String giveUpTag(String content) {
        StringBuilder sb = new StringBuilder(content);
        Pattern pattern = compile("<[^>]+>");
        Matcher matcher = pattern.matcher(sb);
        int i = 0;
        boolean skipFlag = false;
        while (matcher.find(i)) {
            if (matcher.group().indexOf("img") == -1 && matcher.group().indexOf("span") == -1) {
                i = matcher.start();
                sb.replace(matcher.start(), matcher.end(), "");
                continue;
            }
            //针对span标签如果有"mathquill-embedded-latex"则跳过，否则删除
            if (matcher.group().indexOf("span") != -1) {
                if (matcher.group().indexOf("mathquill-embedded-latex") != -1) {
                    i = matcher.end();
                    skipFlag = true;
                    continue;
                } else if (skipFlag) {
                    i = matcher.end();
                    skipFlag = false;
                    continue;
                } else {
                    i = matcher.start();
                    sb.replace(matcher.start(), matcher.end(), "");
                    continue;
                }
            }
            i = matcher.end();
        }
        return sb.toString().replace("&nbsp;", " ");
    }

    /**
     * 切换<span>公式标签为<img>标签
     * <span class="mathquill-embedded-latex" style="width: 37px; height: 39px;">\oint_1^2</span>
     *
     * @param stem      带有公式的内容
     * @param mongoFlag img是否保留大小属性（false 不保留大小为前端专用 true 保留大小为客户端专用）
     * @return
     */
    public static String span2Img(String stem, boolean mongoFlag) {
        if (StringUtils.isBlank(stem)) {
            return StringUtils.EMPTY;
        }
//        String delPattern = "</span><span class=\"mathquill-embedded-latex\" style=\"width: \\d+px; height: \\d+px;\">";
//        stem = stem.replaceAll(delPattern, "");
        StringBuilder sb = new StringBuilder(stem);
        Pattern pattern = compile("<span([^>]+)>(((?!</span>).)+)</span>");
        Matcher matcher = pattern.matcher(sb);
        int i = 0;
        while (matcher.find(i)) {
            if (matcher.group(1).indexOf("mathquill-embedded-latex") != -1) {
                i = changeSpan2Img(sb, matcher, mongoFlag);
            } else if (matcher.group(1).indexOf("text-decoration:underline;") != -1) {
                String underStr = "<u>" + matcher.group(2) + "</u>";
                sb.replace(matcher.start(), matcher.end(), underStr);
            } else {
                /**
                 * 其他类型的span标签直接删除
                 */
                sb.delete(matcher.start(), matcher.end());
                i = matcher.start();
            }
        }
        return sb.toString();
    }

    /**
     * 根据匹配到的matcher，做span表达式转换img标签
     *
     * @param sb
     * @param matcher
     * @param mongoFlag
     * @return
     */
    private static int changeSpan2Img(StringBuilder sb, Matcher matcher, boolean mongoFlag) {
        String tmp = "";
        /**
         * <span class="mathquill-embedded-latex" style="width: 37px; height: 39px;">\oint_1^2</span>
         * 截取 “width: 37px; height: 39px;”字段
         */
        String style = Arrays.stream(matcher.group(1).split("\"")).filter(j -> j.indexOf("width") != -1).findAny().orElse("");
        /**
         * 截取 “\oint_1^2” 字段
         */
        String spanContent = matcher.group(2);
        if (mongoFlag) {
            /**
             * 前端得到的小于号值为“&lt;”，需要转换以适用于客户端图片生成
             * 前端大于号值为“&gt;”，需要转换以适用于客户端图片生成
             */
            spanContent = spanContent.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
            /**
             * 客户端使用
             * 1、拼出latex生成的图片链接
             * 2、生成cdn图片地址
             */
            String url = formula_link_url.replaceFirst("\\{\\}", spanContent);
            log.info("span:{},latex:{}", spanContent, url);

            //base64方式转换图片
            tmp = FormulaConvert.dealTheImage64(spanContent);

        } else {
            spanContent = spanContent.replace("\\", "\\\\");
            tmp = formula_link.replaceFirst("\\{\\}", spanContent);
        }
        //log.info("isApp = {} , style is null :{} , img ={}", mongoFlag, StringUtils.isBlank(style), tmp);
        sb.replace(matcher.start(), matcher.end(), tmp);
        return matcher.start();
    }


    private static void fillImgSize(StringBuilder sb) {
        Pattern pattern = Pattern.compile("<img src=\"([^\"]+)\"[^>]*>");
        Matcher matcher = pattern.matcher(sb);
        int i = 0;
        while (matcher.find(i)) {
            String url = matcher.group(1);
            /**
             * url中如果有斜线（\需要先被替换成\\不让可能会因为拼接字符串的问题丢失转义符）
             */
            url = url.replace("\\inline&space;", "").replace("\\", "\\\\");
            String img = matcher.group();
            if (img.indexOf("height") > -1 || img.indexOf("width") > -1) {
                sb.replace(matcher.start(1), matcher.end(1), url);
                i = matcher.start(1) + url.length();
                continue;
            }
            String width = "";
            String height = "";
            try {
                URL src = new URL(url);
                URLConnection connection = src.openConnection();
                connection.setDoOutput(true);
                BufferedImage image = ImageIO.read(connection.getInputStream());
                int widthSize = image.getWidth();      // 源图宽度
                int heightSize = image.getHeight();    // 源图高度

                width = "" + widthSize;
                height = "" + heightSize;
            } catch (Exception e) {
                log.error("width={},height={}", width, height);
                e.printStackTrace();
            }
            StringBuilder stringBuilder = new StringBuilder("style=\"");
            stringBuilder.append("width: ").append(width).append(";");
            stringBuilder.append("height: ").append(height).append(";");
            stringBuilder.append("\" ");
            stringBuilder.append("width=\"").append(width).append("\" ");
            stringBuilder.append("height=\"").append(height).append("\" ");
            log.info("style Info = {}", stringBuilder.toString());
            String tmp = formula_link_with_size.replaceFirst("\\{\\}", url).replaceFirst("style=\\{\\}", stringBuilder.toString());
            System.out.println(tmp);
            sb.replace(matcher.start(), matcher.end(), tmp);
            i = matcher.start() + tmp.length();
        }
    }


    private static String getImgSize(String style, String pattern) {
        Pattern pat = Pattern.compile(pattern);
        Matcher matcher = pat.matcher(style);
        if (matcher.find()) {
            return matcher.group(1).replace("px", "").trim();
        } else {
            return "0";
        }
    }

    /**
     * 从字符串首解析题型名称
     *
     * @param sb
     * @return
     */
    public static String getQuestionTypeName(StringBuilder sb) {
        if (null == sb || sb.length() == 0) {
            return StringUtils.EMPTY;
        }
        Pattern pattern = Pattern.compile("^（([^）]+)）");
        Matcher matcher = pattern.matcher(sb);
        if (matcher.find()) {
            String result = matcher.group(1);
            sb.delete(0, matcher.end());
//            System.out.println("剩余文本="+sb.toString());
            return result;
        }
        return StringUtils.EMPTY;
    }

    /**
     * 截取从开头到正则匹配处的内容
     * pat = "\n<pattern>"
     *
     * @param sb
     * @param reg
     * @return
     */
    public static String getContentByTail(StringBuilder sb, String reg) {
        int index = sb.indexOf(STRING_SEPARATOR, 0);
        if (index < 0) {
//            System.out.println("没有回车");
            return "";
        }
        StringBuilder temp = new StringBuilder(sb.substring(index).trim());
        if (isTail(temp, reg)) {
            return sb.substring(0, index);
        }
        return sb.substring(0, index) + STRING_SEPARATOR + getContentByTail(new StringBuilder(sb.substring(index).trim()), reg);
    }

    /**
     * 是否匹配结尾正则
     *
     * @param sb
     * @param reg
     * @return
     */
    private static boolean isTail(StringBuilder sb, String reg) {
        if (StringUtils.isBlank(sb)) {
            return true;
        }
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(sb);
        if (matcher.find()) {
//            System.out.println("结尾："+matcher.group());
            return true;
        }
        return false;
    }


    public static void main(String[] args) {
        String span2Img = span2Img("<p>电视里的姚明很小，但<span style=\"text-decoration:underline;\">我们依然<strong>直觉到他很</strong></span><strong>高大，这种</strong>现象是知觉（）</p>", true);
//        String span2Img = span2Img("<p>第一步，本题考查工程问题中的条件类问题，用方程法解题。<br/>第二步，设乙车间每天生产的件数为2x，则甲车间每天生产的件数为3x，根据题干可列方程： <span class=\"mathquill-embedded-latex\" style=\"width: 158px; height: 41px;\">\\frac{1200}{2x}-\\frac{1200}{3x}=10</span>，解得x=20，即乙车间每天可以生产2×20=40（件），甲车间每天可以生产<br/><span class=\"mathquill-embedded-latex\" style=\"width: 61px; height: 41px;\">\\frac{1200}{3x}</span><span class=\"mathquill-embedded-latex\" style=\"width: 28px; height: 26px;\">-</span><span class=\"mathquill-embedded-latex\" style=\"width: 61px; height: 41px;\">\\frac{1200}{4x}</span><span class=\"mathquill-embedded-latex\" style=\"width: 36px; height: 26px;\">=</span>10，解得x=20<br/>3×20=60（件）。<br/>第三步，甲乙两个车间合作生产3000件产品所需时间为<span class=\"mathquill-embedded-latex\" style=\"width: 110px; height: 41px;\">\\frac{3000}{40+60}=30</span>（天）。<span class=\"mathquill-embedded-latex edui-formula-active\" style=\"width: 134px; height: 41px;\">\\frac{a+b+c+d+E}{20+20+30}</span><span id=\"_baidu_bookmark_start_9\" style=\"display: none; line-height: 0px;\">\u200D</span>因此，选择C选项。</p>\n",true);
//        String span2Img = span2Img ("<p>第一步，根据“与2011年相比，2013年……”可确定本题为间隔增长率计算问题。<br/>第二步，定位图形材料。<br/>第三步，根据公式R＝r1＋r2＋r1×r2，r1＝12%，r2＝15.3%，代入公式可得R＝12%＋15.3%＋12%×15.3%＝27.3%＋12%×15.3%<span class=\"mathquill-embedded-latex\">&gt;</span>27.3%，仅有D符合。<br/>因此，选择D选项。</p>", true);
//        String span2Img = span2Img("<p>第一步，&lt;本题考查工程问题中的条件类问题，用方程法解题。<br/>第二步，设乙车间每天生产的件数为2x，则甲车间每天生产的件数为3x，根据题干可列方程： <span class=\"mathquill-embedded-latex\" style=\"width: 158px; height: 41px;\">\\frac{1200}{2x}-\\frac{1200}{3x}=10</span><span id=\"_baidu_bookmark_start_0\" style=\"display: none; line-height: 0px;\">\u200D</span>，解得x=20，即乙车间每天可以生产2×20=40（件），甲车间每天可以生产3×20=60（件）。<br/>第三步，甲乙两个车间合作生产3000件产品所需时间为<span class=\"mathquill-embedded-latex\" style=\"width: 110px; height: 41px;\">\\frac{3000}{40+60}=30</span>（天）。<br/>因此，选择C选项。</p>", true);
//        String span2Img = span2Img("<p>第一步，本题考查工程问题中的条件类问题，用方程法解题。<br/>第二步，设乙车间每天生产的件数为2x，则甲车间每天生产的件数为3x，根据题干可列方程： <img src=\"http://tiku.huatu.com/cdn/pandora/img/0e5b57a5339d46eb9207795904354387.png\" width=\"118.5\" height=\"30.75\" style=\"width:118.5;height:30.75\">，解得x=20，即乙车间每天可以生产2×20=40（件），甲车间每天可以生产<br/><img src=\"http://tiku.huatu.com/cdn/pandora/img/d3aca72aab3142ddaae52e71cea51354.png\" width=\"45.75\" height=\"30.75\" style=\"width:45.75;height:30.75\"><img src=\"http://tiku.huatu.com/cdn/pandora/img/de67e70f3d3e43cf8dfb5ff0a30be795.png\" width=\"21.0\" height=\"19.5\" style=\"width:21.0;height:19.5\"><img src=\"http://tiku.huatu.com/cdn/pandora/img/502637bcf6b943e3973d94f16e6743f1.png\" width=\"45.75\" height=\"30.75\" style=\"width:45.75;height:30.75\"><img src=\"http://tiku.huatu.com/cdn/pandora/img/14c0403429274125883349e5916a949b.png\" width=\"27.0\" height=\"19.5\" style=\"width:27.0;height:19.5\">10，解得x=20<br/>3×20=60（件）。<br/>第三步，甲乙两个车间合作生产3000件产品所需时间为<img src=\"http://tiku.huatu.com/cdn/pandora/img/1304acbd912448e7b0a824d5dd5618af.png\" width=\"82.5\" height=\"30.75\" style=\"width:82.5;height:30.75\">（天）。<img src=\"http://tiku.huatu.com/cdn/pandora/img/a02a63adae33484b802153b5e6846ff5.png\" width=\"100.5\" height=\"30.75\" style=\"width:100.5;height:30.75\">因此，选择C选项。</p>\n"

        System.out.println("span2Img：" + span2Img);


    }

    /**
     * @param sb         截取主题
     * @param targetName 需要截取内容的定义名称（报错时用）
     * @param preReg     截取上界限标识
     * @param tailReg    截取下界限标识
     * @return
     */
    public static String getContent(StringBuilder sb, String targetName, String preReg, String tailReg) {
        if (null == sb || sb.length() == 0) {
            throwBizException("解析到【" + targetName + "】时，已无对应的内容，查看是否有该标签，或者" + QUESTION_PARSE_ERROR);
        }
        String content = HtmlConvertUtil.getContentByTail(sb, tailReg);
        deleteBytailReg(sb, content, tailReg);
        if (StringUtils.isNotEmpty(content)) {
            content = content.trim().replaceAll(preReg, "").replace("\n", "<br>");
        }
        return content.trim();
    }

    private static void deleteBytailReg(StringBuilder sb, String content, String tailReg) {
        if (sb.indexOf(content.trim()) > -1) {
            sb.delete(0, sb.indexOf(content.trim()) + content.length());
            return;
        }
        tailReg = tailReg.replace("^", "").trim();
        Pattern pattern = Pattern.compile(tailReg);
        Matcher matcher = pattern.matcher(sb);
        if (matcher.find()) {
            sb.delete(0, matcher.start());
        } else {
            System.out.println("deleteBytailReg error ,sb = " + sb.toString() + "\ntailReg = " + tailReg);
        }
//        int index = sb.indexOf(tailReg);
//        if(index > -1){
//            sb.delete(0,index);
//        }else{
//            System.out.println("deleteBytailReg error ,sb = " + sb.toString() + "\ntailReg = " + tailReg);
//        }

    }

    /**
     * 解析mongo答案内容从1234到ABCD
     *
     * @param answer
     * @return
     */
    public static String parseMongoAnswer(int answer) {
        String target = answer + "";
        char[] chars = target.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            sb.append((char) (chars[i] - "1".charAt(0) + 'A'));
        }
        return sb.toString();
    }

    /**
     * 将前端选项组装为mysql数据
     *
     * @param choices
     * @return
     */
    public static String assertChoicesContent(List<String> choices) {
        StringBuilder sb = new StringBuilder();
        for (String choice : choices) {
            sb.append("<choices>").append(choice).append("<choices>");
        }
        return sb.toString();
    }

    /**
     * 将选项有数据库格式转为前端格式
     *
     * @param choices
     * @return
     */
    public static List<String> parseChoices(String choices) {
        if (StringUtils.isBlank(choices)) {
            return Lists.newArrayList();
        }
        return Arrays.stream(choices.split("<[/]?choices>")).filter(i -> StringUtils.isNotBlank(i)).collect(Collectors.toList());
    }

    /**
     * 获取map-》value值，并做格式转化（mysql->mongo专用）
     *
     * @param mapData
     * @param key
     * @return
     */
    public static String getContentFromMap(HashMap<String, Object> mapData, String key) {
        Object temp = mapData.get(key);
        if (temp == null) {
            return StringUtils.EMPTY;
        }
        //获取查询的数据，并将其做格式转化
        return span2Img(String.valueOf(temp), true);
    }


    /**
     * 转换答案格式从ABCD 到1234
     *
     * @param answer
     * @return
     */
    public static int formatAnswer2Mongo(String answer) {
        char[] chars = answer.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c - 'A' + 1);
        }
        return Integer.parseInt(sb.toString());
    }

    /**
     * 组装模考大赛活动时间
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public static String assertMatchTimeInfo(long startTime, long endTime) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date(startTime));

        int day = instance.get(Calendar.DAY_OF_WEEK);

        //考试时间：2017年8月20日（周日）09:00-11:00
        String timeInfo = DateFormatUtils.format(startTime, "yyyy年M月d日") + "（%s）%s-%s";
        String dayString = "";
        switch (day) {
            case Calendar.SUNDAY:
                dayString = "周日";
                break;

            case Calendar.MONDAY:
                dayString = "周一";
                break;

            case Calendar.TUESDAY:
                dayString = "周二";
                break;
            case Calendar.WEDNESDAY:
                dayString = "周三";
                break;
            case Calendar.THURSDAY:
                dayString = "周四";
                break;
            case Calendar.FRIDAY:
                dayString = "周五";
                break;

            case Calendar.SATURDAY:
                dayString = "周六";
                break;
        }

        timeInfo = String.format(timeInfo, dayString, DateFormatUtils.format(startTime, "HH:mm"),
                DateFormatUtils.format(endTime, "HH:mm"));

        return "考试时间：" + timeInfo;

    }

    public static String getAnswer(String answer) {
        char[] chars = answer.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char ch : chars) {
            char temp = (char) (Integer.parseInt(String.valueOf(ch)) - 1 + 'A');
            sb.append(temp);
        }
        return sb.toString();
    }
}