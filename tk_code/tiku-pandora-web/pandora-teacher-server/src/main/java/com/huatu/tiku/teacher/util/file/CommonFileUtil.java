package com.huatu.tiku.teacher.util.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.tiku.entity.download.BaseTool;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.teacher.service.impl.download.v1.WordWriteServiceImplV1;
import com.huatu.tiku.teacher.util.image.ImageUtil;
import com.huatu.tiku.util.file.FunFileUtils;
import com.huatu.tiku.util.file.FuncStr;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.rtf.RtfWriter2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import javax.management.Query;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 文件相关工具（下载文件用）
 * Created by huangqingpeng on 2018/8/14.
 */
@Slf4j
public class CommonFileUtil {

    /**
     * document生成
     *
     * @return
     */
    public static Document newDocument() {
        Document document = null;
        try {
            // 设置纸张大小
            document = new Document(PageSize.A4);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    /**
     * Font生成
     *
     * @return
     */
    public static Font newFont(int size, int style) {
        // 设置中文字体
        BaseFont bfChinese = null;
        try {
            bfChinese = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Font contentFont = new Font(bfChinese, size, style);
        return contentFont;
    }

    /**
     * 建立一个书写器与document对象关联，通过书写器可以将文档写入到输出流中
     *
     * @param document
     * @param file
     * @return
     */
    public static RtfWriter2 newRtfWriter2(Document document, File file) {
        try {
            RtfWriter2 instance = RtfWriter2.getInstance(document, new FileOutputStream(file));
            return instance;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 填充基本内容
     *
     * @param value
     * @param addText
     * @throws Exception
     */
    public static void fillContent(String value, BaseTool baseTool, BiConsumer<BaseTool, Object> addText) throws Exception {
        Pattern pattern = Pattern.compile("<[^>]+>");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            fileExistsImg("", value, baseTool, addText);
        } else {
            addText.accept(baseTool, value);
        }
    }

    /**
     * 填充包含图片的内容
     *
     * @param value
     * @param baseTool
     * @param addText
     */
    public static void fileExistsImg(String index, String value, BaseTool baseTool, BiConsumer<BaseTool, Object> addText) throws BizException {
        value = value.replaceAll("<[/]?br[/]?>", "\n");
        //替换换行
        value = value.replaceAll("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", "\n");
        //填充空格
        value = value.replace("&nbsp;", " ");
        String strTemp = FuncStr.htmlManage(value); //处理基本标签《br/》
        while (strTemp.indexOf("\n\n") > -1) {
            strTemp = strTemp.replace("\n\n", "\n");
        }
        java.util.List parts = Lists.newArrayList();
        try {
            splitContentAndAddToList1(index + strTemp, parts);
            addText.accept(baseTool, parts);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException(ErrorResult.create(1010101, e.getMessage()));
        }
    }

    public static void splitContentAndAddToList1(String strTemp, java.util.List<Object> parts) throws IOException, BadElementException, BizException {
        Pattern pattern = Pattern.compile("<[/]?img[^>]*>");
        StringBuilder sb = new StringBuilder(strTemp);
        Matcher matcher = pattern.matcher(sb);
        int i = 0;
        while (matcher.find(i)) {
            if (matcher.start() > 0) {
                //处理图片前的文本内容
                parts.add(sb.substring(0, matcher.start()).trim());
            }
            //图片先分离出来之后处理
            HashMap<Object, Object> hashMap = Maps.newHashMap();
            hashMap.put("imgTag", matcher.group());
            parts.add(hashMap);

            sb.delete(0, matcher.end());
        }
        //图片之后的数据处理
        parts.add(sb.toString().trim());
    }

    /**
     * 解析图片内容
     *
     * @param content "<img src=\"http://tiku.huatu.com/cdn/pandora/img/28ab2e2e-8d6f-445e-9ddc-6e1eaedbc249..jpg?imageView2/0/w/221/format/jpg\" style=\"width: 221;height: 169;\" width=\"221\" height=\"169\">"
     * @return
     * @throws BizException
     * @throws IOException
     * @throws BadElementException
     */
    public static Object assertImg(String content) throws BizException, IOException, BadElementException {
        long start = System.currentTimeMillis();
        StopWatch stopWatch = new StopWatch("assertImg: " + content);
        stopWatch.start("src get");
        if (StringUtils.isBlank(content)) {
            return null;
        }
        String imgUrl = subAttrString(content, "src");
        stopWatch.stop();
        if ("".equals(imgUrl)) {
            return "【图片暂缺】(" + content + ")";
        }
        stopWatch.start("width height get");
        Map<String, String> map = PdfUtil.handlerImageSize(content);
        String widthString = MapUtils.getString(map, "widthString");
        String heightString = MapUtils.getString(map, "heightString");
        //如果含有style属性，则直接解析style属性，否则解析各自的width和height属性
        Image tempImage = null;
        stopWatch.stop();
        if (StringUtils.isNotEmpty(imgUrl) && imgUrl.length() > 0) {
            stopWatch.start("get Image");
            try {
                tempImage = ImageUtil.getImage(imgUrl);
            } catch (Exception e) {
                log.error("无效的图片地址：imgUrl ={}", imgUrl);
            }
            stopWatch.stop();
        }
        if (tempImage != null) {
            stopWatch.start("tempImage handle");
            try {
                float width = Float.parseFloat(widthString.replace("px", ""));
                float height = Float.parseFloat(heightString.replace("px", ""));
                tempImage.scaleToFit(width * 0.7f, height * 0.7f);
                tempImage.setAlignment(Image.ALIGN_MIDDLE);
                if (width > 300) {
                    tempImage.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_MIDDLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            stopWatch.stop();
        }
        long time = System.currentTimeMillis() - start;
        log.info("assertImg stopWatch:{}", stopWatch.prettyPrint());
//        if(time > TimeUnit.MILLISECONDS.toMillis(400)){
//        }
        return tempImage;
    }

    /**
     * 获取img标签中，style值中的大小属性
     *
     * @param name    width|height
     * @param content
     * @return
     */
    public static String getSizeFromStyle(String name, String content) {
        Pattern pattern = Pattern.compile(name + ":([^;|\"]+)(;|\")");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    public static String subAttrString(String content, String attr) throws BizException {
        int index = content.indexOf(attr);
        if (index != -1) {
            String url = FunFileUtils.getQuoteContent(content.substring(index + 1));
            return url;
        } else {
            log.error("没有{}属性，content={}", attr, content);
        }
        return "";
    }

    /**
     * 解析文本文字格式
     *
     * @param content
     * @return
     */
    public static List<TextStyleElement> assertTextStyle(String content) {
        List<TextStyleElement> list = Lists.newArrayList();
        for (QuestionElementEnum.TextStyleEnum styleEnum : QuestionElementEnum.TextStyleEnum.values()) {
            Pattern pattern = styleEnum.getPattern();
            Matcher matcher = pattern.matcher(content);
            int index = 0;
            while (matcher.find(index)) {
                TextStyleElement textStyleElement = new TextStyleElement();
                textStyleElement.setTextStyleEnums(Sets.newHashSet(styleEnum));
                textStyleElement.setHeadStartIndex(matcher.start());
                textStyleElement.setHeadEndIndex(matcher.start(1));
                textStyleElement.setTailStartIndex(matcher.end(1));
                textStyleElement.setTailEndIndex(matcher.end());
                list.add(textStyleElement);
                index = matcher.end();
            }
        }
        return list;
    }

    public static void main(String[] args) {
        String content = "<p>电视里的姚明很小，但<u>我们依然<strong>直觉到他很</strong></u><strong>高大，这种</strong>现象是知觉（）</p>";
        List<TextStyleElement> elements = assertTextStyle(content);
        System.out.println("elements = " + JsonUtil.toJson(elements));
        List<Map<String, Integer>> regions = getContentRegions(elements, content);
        System.out.println("JsonUtil.toJson(regions) = " + JsonUtil.toJson(regions));
        for (Map<String, Integer> region : regions) {
            List<QuestionElementEnum.TextStyleEnum> styles = CommonFileUtil.getStyles(elements, region);
            int fontStyle = CommonFileUtil.countFontStyle(styles, new Font());
            System.out.println("fontStyle = " + fontStyle);
            System.out.println("region = " + content.substring(region.get("start"), region.get("end") + 1));

        }
    }
    public static List<Map<String, Integer>> getContentRegions(List<TextStyleElement> list, String content) {
        List<Integer> indexes = IntStream.range(0, content.length()).boxed().collect(Collectors.toList());
        for (TextStyleElement textStyleElement : list) {
            indexes.removeIf(i -> isTagRegions(textStyleElement, i));
        }
        indexes.sort(Comparator.comparing(Integer::intValue));
        List<Map<String,Integer>> result = Lists.newArrayList();
        int start = -1;
        for (int i = 0; i < indexes.size(); i++) {
            Integer index = indexes.get(i);
            if (start == -1) {
                start = index;
                continue;
            }
            Integer preIndex = indexes.get(i - 1);
            if(preIndex + 1 < index){
                HashMap<String, Integer> map = Maps.newHashMap();
                map.put("start",start);
                map.put("end",preIndex);
                result.add(map);
                start = index;
            }
        }
        if(CollectionUtils.isNotEmpty(indexes)){
            Integer index = indexes.get(indexes.size() - 1);
            if(start < index){
                HashMap<String, Integer> map = Maps.newHashMap();
                map.put("start",start);
                map.put("end",index);
                result.add(map);
            }
        }
        return result;
    }

    public static boolean isTagRegions(TextStyleElement textStyleElement, int index) {
        if (index >= textStyleElement.getHeadStartIndex() && index < textStyleElement.getHeadEndIndex()) {
            return true;
        }
        if (index >= textStyleElement.getTailStartIndex() && index < textStyleElement.getTailEndIndex()) {
            return true;
        }
        return false;
    }

    public static List<QuestionElementEnum.TextStyleEnum> getStyles(List<TextStyleElement> list, Map<String, Integer> region) {
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }
        Integer start = region.get("start");
        Integer end = region.get("end");
        List<TextStyleElement> collect = list.stream().filter(element -> start >= element.getHeadEndIndex() && end < element.getTailStartIndex()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(collect)){
            Set<QuestionElementEnum.TextStyleEnum> set = Sets.newHashSet();
            for (TextStyleElement textStyleElement : collect) {
                set.addAll(textStyleElement.getTextStyleEnums());
            }
            return set.stream().collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public static int countFontStyle(List<QuestionElementEnum.TextStyleEnum> styles, Font questionFont) {
        if(CollectionUtils.isEmpty(styles)){
            return questionFont.getStyle();
        }
        int questionStyle = 0;
        for (QuestionElementEnum.TextStyleEnum style : styles) {
            switch (style){
                case STRONG:
                    questionStyle = questionStyle | Font.BOLD;
                    break;
                case UNDERLINE:
                    questionStyle = questionStyle | Font.UNDERLINE;
                    break;
            }
        }
        return questionStyle;
    }
}
