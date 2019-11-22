package com.huatu.tiku.essay.util.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
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
     * 解析文本文字格式
     *
     * @param content
     * @return
     */
    public static List<TextStyleElement> assertTextStyle(String content) {
        List<TextStyleElement> list = Lists.newArrayList();
        for (TextStyleEnum styleEnum : TextStyleEnum.values()) {
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
            List<TextStyleEnum> styles = getStyles(elements, region);
            int fontStyle = countFontStyle(styles, new Font());
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
        Integer index = indexes.get(indexes.size() - 1);
        if(start < index){
            HashMap<String, Integer> map = Maps.newHashMap();
            map.put("start",start);
            map.put("end",index);
            result.add(map);
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

    public static List<TextStyleEnum> getStyles(List<TextStyleElement> list, Map<String, Integer> region) {
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }
        Integer start = region.get("start");
        Integer end = region.get("end");
        List<TextStyleElement> collect = list.stream().filter(element -> start >= element.getHeadEndIndex() && end < element.getTailStartIndex()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(collect)){
            Set<TextStyleEnum> set = Sets.newHashSet();
            for (TextStyleElement textStyleElement : collect) {
                set.addAll(textStyleElement.getTextStyleEnums());
            }
            return set.stream().collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public static int countFontStyle(List<TextStyleEnum> styles, Font questionFont) {
        if(CollectionUtils.isEmpty(styles)){
            return questionFont.getStyle();
        }
        int questionStyle = 0;
        for (TextStyleEnum style : styles) {
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
