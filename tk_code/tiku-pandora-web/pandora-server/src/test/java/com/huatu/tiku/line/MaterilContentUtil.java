package com.huatu.tiku.line;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.tiku.enums.QuestionInfoEnum;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.GenericSubjectiveQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import com.huatu.ztk.question.util.ImageUtil;
import ij.ImagePlus;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by shaojieyue
 * Created time 2016-05-10 15:12
 */


public class MaterilContentUtil {
    //question meta信息，保留答案统计最多个数
    public static final int MAX_ANSWER_COUNT = 4;
    //试题图片前缀
    private static String imgprefix = "http://tiku.huatu.com/cdn/images/vhuatu/tiku/";




    public static final String HTML_TAG_BR = "br";
    public static final String HTML_TAG_IMG = "img";
    public static final String HTML_TAG_P = "p";
    public static final String HTML_TAG_SPAN = "span";
    public static final String HTML_TAG_U = "u";
    //question meta2 信息缓存
    public static final Cache<Integer, QuestionMeta> QUESTION_META_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.MINUTES)//调整为20分钟
            .maximumSize(100000)
            .initialCapacity(40000)
            .build();
    //试题映射信息查询
    public static final Cache<Integer, Integer> REFLECTION_CACHE = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.DAYS)
            .maximumSize(1000000)
            .initialCapacity(100000).build();





    /**
     * 格式化试题字符串
     *
     * @param source
     * @return
     */
    public static String format(String source) {
        source = StringUtils.trimToNull(source);
        if (source == null) {
            return "";
        }
        //<div>替换成<p>,多余的<p>将在cleanContent方法去掉，</div>不用处理
        source = source.replaceAll("<p></p>", "")
                .replaceAll("<p />", "")
                .replaceAll("<p/>", "")
                .replaceAll("<br></br>", "<br/>")
                .replaceAll("<br>", "<br/>")
                .replaceAll("<div>", "<p>");
        int oldLength = source.length();
        source = source.replaceAll("<br/><br/>", "<br/>");
        int newLength = source.length();
        //循环去掉多余的br
        while (oldLength > newLength) {
            oldLength = source.length();
            source = source.replaceAll("<br/><br/>", "<br/>");
            newLength = source.length();
        }
        //处理[img style=\"float:none;\"]这样的标签
        //source = source.replaceAll("\\[img.*?\\]", "[img]");

        //括号统一转为中文括号
        source = source.replaceAll("\\(", "（").replaceAll("\\)", "）");

        //防止形如“<!--[img]4924a387ebcb16fb837b7f4b821ddea30a9f2a50.jpg[/img]-->  东盟”的题目失去图片标签
        //去掉字符串头部的不可见字符
        String tmpSource = source.replaceAll("[\\s]", "");
        if (tmpSource.startsWith("<!--") && !tmpSource.endsWith("-->")) {
            source = "<p>" + source;
        }

        return source;
    }






    private QuestionMeta getInitQuestionMeta(GenericQuestion genericQuestion) {
        return QuestionMeta.builder()
                .avgTime(60)
                .count(1)
                .answers(new int[]{genericQuestion.getAnswer()})
                .counts(new int[]{1})
                .percents(new int[]{100})
                .yc(genericQuestion.getAnswer() == 1 ? 2 : 1)//易错项
                .rindex(0)
                .build();
    }



    /**
     * 转换成客户端适用的题型
     * 客户端题型判断：type==99,100,101,106都有对应的题型名称写死在代码中
     * type==106 如果没有teacherType则展示写死的主观题，否则展示teacherType
     * type==其他，如果没有teaherType则展示写死的单选题。否则展示teacherType
     * @param question
     */
    private void convert2MobileQuestionType(Question question) {
        if(null == question){
            return;
        }
        int type = question.getType();
        for (QuestionInfoEnum.QuestionTypeEnum questionTypeEnum : QuestionInfoEnum.QuestionTypeEnum.values()) {
            if(questionTypeEnum.getCode() == type){
                question.setTeachType(questionTypeEnum.getValue());
                break;
            }
        }
        if(question instanceof GenericSubjectiveQuestion){
            question.setType(QuestionInfoEnum.QuestionTypeEnum.SUBJECTIVE.getCode());
        }
    }



    /**
     * 将内容转为手机支持的原生格式
     *
     * @param content
     * @return
     */
    public static final String convert2MobileLayout(String content) {
        if (StringUtils.isBlank(content)) {
            return content;
        }

        content = format(content);
        Document document = Jsoup.parse(content);
        // Element body = document.body();
        List<Node> roots = Lists.newArrayList(document.childNodes());
        if (roots.size() == 0) {//body里面没有内容,说明只是存在图片
            StringBuilder stringBuilder = new StringBuilder();
            convert2MobileLayout(Jsoup.parse(content), stringBuilder);
            return stringBuilder.toString();
        }
        //移除以br结尾的标签
        while (roots.size() > 0 && "br".equalsIgnoreCase(roots.get(roots.size() - 1).nodeName())) {
            roots.remove(roots.size() - 1);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (Node root : roots) {//根的话,更换为p标签
            convert2MobileLayout(root, stringBuilder);
        }

        //如果内容不是以<p>开头,则添加<p>标签
        if (!stringBuilder.toString().startsWith("<p>")) {
            stringBuilder.insert(0, "<p>");
            stringBuilder.append("</p>");
        }
        String finalContent = stringBuilder.toString();
        finalContent = cleanContent(finalContent);
        //对html 标签做替换
        finalContent = StringEscapeUtils.unescapeHtml4(finalContent);
        return finalContent;
    }

    /**
     * 清除没有必要的内容
     *
     * @param finalContent
     * @return
     */
    private static String cleanContent(String finalContent) {
        //把不可见字符‍‍‍‍‍‍"\u200D"替换为空
        finalContent = finalContent.replaceAll("\u200D", "");
        //去掉换行
        finalContent = finalContent.replaceAll("\n", "").replaceAll("\r\n", "");

        //去掉空白p标签
        final String ptag = "<p></p>";

        while (finalContent.indexOf(ptag) >= 0) {//循环清除空白p标签,采用循环的目的是为了方式<p><p></p></p>形式的内容
            finalContent = finalContent.replaceAll(ptag, "");
        }
        return finalContent.replaceAll("(<br/></p>)$", "</p>");
    }

    /**
     * //20161205之前将《span style="text-decoration: underline"》替换为<underline>,后将其替换为<u></u>,
     *
     * @param node
     * @param stringBuilder
     */
    private static void convert2MobileLayout(Node node, StringBuilder stringBuilder) {
        if (node instanceof Element) {
            final Element element = (Element) node;
            final String tagName = element.tagName().toLowerCase();
            if (tagName.equalsIgnoreCase(HTML_TAG_BR)) {//br
                stringBuilder.append("<br/>");
            } else if (tagName.equalsIgnoreCase(HTML_TAG_IMG)) {
                stringBuilder.append(element.outerHtml());
            } else if (tagName.equalsIgnoreCase(HTML_TAG_P)) {
                stringBuilder.append("<p>");
                final List<Node> children = node.childNodes();
                for (Node sub : children) {
                    convert2MobileLayout(sub, stringBuilder);
                }
                stringBuilder.append("</p>");
            } else if (tagName.equalsIgnoreCase(HTML_TAG_SPAN)) {
                String span1 = element.attr("style");
                if (span1 != null) {
                    String tmp = span1.replaceAll(" ", "").replaceAll(" ", "");
                    //span包含underline字符串说明是下划线
                    if (tmp.indexOf("underline") > -1) {
                        stringBuilder.append("<u>");
                        final List<Node> children = node.childNodes();
                        for (Node sub : children) {
                            if (sub instanceof TextNode) {//如果是文本节点,则特殊处理
                                TextNode textNode = (TextNode) sub;
                                //把&nbsp;替换为_
                                String text = textNode.getWholeText().replaceAll(" ", "").replaceAll("\u00a0", "_");
                                if (StringUtils.isBlank(text)) {
                                    text = "___";
                                }
                                stringBuilder.append(text);
                            } else {
                                convert2MobileLayout(sub, stringBuilder);
                            }
                        }
                        stringBuilder.append("</u>");
                    } else {
                        //不包含underline字符串的处理方法跟p标签的相同
//                        stringBuilder.append("<p>");
                        final List<Node> children = node.childNodes();
                        for (Node sub : children) {
                            convert2MobileLayout(sub, stringBuilder);
                        }
//                        stringBuilder.append("</p>");
                    }
                }
            } else if (tagName.equalsIgnoreCase(HTML_TAG_U)) {
                stringBuilder.append("<u>");
                final List<Node> children = node.childNodes();
                for (Node sub : children) {
                    convert2MobileLayout(sub, stringBuilder);
                }
                stringBuilder.append("</u>");
            } else {//不识别的,则不添加任何标签
                final List<Node> children = node.childNodes();
                for (Node sub : children) {
                    convert2MobileLayout(sub, stringBuilder);
                }
            }
        } else if (node instanceof TextNode) {
            stringBuilder.append(((TextNode) node).getWholeText());
        } else if (node instanceof Comment) {//注释
            final Comment comment = (Comment) node;
            //<!--[img]34a88f0a1509b896e38a8e68e603a9765310bf10.png[/img]-->
            String data = comment.getData();
            if (data.startsWith("[img")) {
                stringBuilder.append(processImage(data));
            }
        }
    }

//    private static String processImage(String data) {
//        StringBuilder builder = new StringBuilder(data);
//        Pattern pattern = Pattern.compile("\\[img([^\\]]*)\\]([^\\[]+)\\[/img\\]");
//        Matcher matcher = pattern.matcher(builder);
//        int i = 0;
//        while (matcher.find(i)) {
//            String style = matcher.group(1);
//            String image = matcher.group(2);
//            String imgUrl = imgprefix + image.charAt(0) + "/" + image;
//            if(StringUtils.isBlank(style)){
//                final ImagePlus imagePlus = ImageUtil.parse(imgUrl);
//                final int height = imagePlus.getHeight();
//                final int width = imagePlus.getWidth();
//                style = " style=\"width:"+width+";height:"+height+"\"";
//            }
//            String imgContent = "<img src =\""+ imgUrl+"\"" +style+"/>";
//            builder.replace(matcher.start(),matcher.end(),imgContent);
//            i = matcher.start();
//        }
//        return builder.toString();
//    }


    private static String processImage(String data) {
        StringBuilder builder = new StringBuilder(data);
        Pattern pattern = Pattern.compile("\\[img([^\\]]*)\\]([^\\[]+)\\[/img\\]");
        Matcher matcher = pattern.matcher(builder);
        int i = 0;
        while (matcher.find(i)) {
            String styleAdapt = "";
            String style = matcher.group(1);
            String image = matcher.group(2);
            String imgUrl = imgprefix + image.charAt(0) + "/" + image;
            if (StringUtils.isBlank(style)) {
                final ImagePlus imagePlus = ImageUtil.parse(imgUrl);
                final int height = imagePlus.getHeight();
                final int width = imagePlus.getWidth();
                style = " width=\"" + width + "\" height=\"" + height + "\" ";
                styleAdapt = " style=\"width:" + width + ";height:" + height + "" + "\" ";
            } else {
                String width = getWidth(style);
                String height = getHeight(style);
                style = " width=\"" + width + "\" height=\"" + height + "\" ";
                styleAdapt = " style=\"width:" + width + ";height:" + height + "" + "\" ";
            }
            String imgContent = "<img src=\"" + imgUrl + "\"" + style + " " + styleAdapt + " />";
            builder.replace(matcher.start(), matcher.end(), imgContent);
            i = matcher.start();
        }
        return builder.toString();
    }

    private static String getWidth(String style) {
        StringBuilder builder = new StringBuilder(style);
        Pattern pattern = Pattern.compile("width:\\s*(\\d*)px");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String getHeight(String style) {
        StringBuilder builder = new StringBuilder(style);
        Pattern pattern = Pattern.compile("height:\\s*(\\d*)px");
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }






}
