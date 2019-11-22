package com.huatu.tiku.essay.util.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.google.gson.Gson;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.vo.file.TagPosition;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.aspectj.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author huangqingpeng
 * @title: Intelligence2AppUtil
 * @description: 智能批改批改标签转换
 * @date 2019-07-1719:49
 */
public class Intelligence2AppUtil {

    public final static String thesis_tag = "<thesisScore value=\"%s\" id=\"%s\" description=\"%s\" score=\"%s\">%s</thesisScore>";
    public final static String evidence_tag = "<evidenceScore  value=\"%s\" id=\"%s\" description=\"%s\" score=\"%s\">%s</evidenceScore>";
    public final static String struct_tag = "<structScore value=\"%s\" id=\"%s\" description=\"%s\" score=\"%s\">%s</structScore>";
    public final static String sentence_tag = "<sentenceScore value=\"%s\" id=\"%s\" description=\"%s\" score=\"%s\">%s</sentenceScore>";
    public final static String literary_tag = "<literaryScore value=\"%s\" id=\"%s\" description=\"%s\" score=\"%s\">%s</literaryScore>";
    public final static String thought_tag = "<thoughtScore value=\"%s\" id=\"%s\" description=\"%s\" score=\"%s\">%s</thoughtScore>";
    public final static List<String> descList = Lists.newArrayList("准确", "强有力", "完整", "流畅", "有文采", "深刻");
    public final static Gson gson = new Gson();
    public final static String label_tag = "<%s seq=\"%s\" description=\"%s\" score=\"%s\" drawType=\"%s\">";

    public final static List<String> UNDER_LINE_TAG = Lists.newArrayList("titleScore", "thesisScore", "evidenceScore", "structScore");
    public final static List<String> HIGH_LIGHT_TAG = Lists.newArrayList("sentenceScore", "literaryScore", "thoughtScore");

    public final static String FINAL_TAG_NAME = "label";

    public final static String SPLIT_CHAR = "_";

    @AllArgsConstructor
    @Getter
    public enum LabelTag {
        THESIS_SCORE("论点", thesis_tag),
        EVIDENCE_SCORE("论据", evidence_tag),
        STRUCT_SCORE("结构", struct_tag),
        SENTENCE_SCORE("句子", sentence_tag),
        LITERARY_SCORE("文采", literary_tag),
        THOUGHT_SCORE("思想性", thought_tag),
        ;

        private String description;
        private String tagMode;

        /**
         * 写入特定标签（测试数据生成逻辑）
         *
         * @param sb
         * @param labelTag
         */
        @Deprecated
        public static void insertTag(StringBuilder sb, LabelTag labelTag) {
            System.out.println("insert labelTag = " + labelTag.getDescription());
            if (StringUtils.isEmpty(sb)) {
                return;
            }
            /**
             * 获取所有标签位置
             */
            List<TagPosition> tagPositions = getTagPositions(sb);
            boolean b = checkTagRule(tagPositions);
            if (!b) {
                System.out.println(sb.toString());
            }
            LabelTag newLabelTag = null;
            /**
             * 保留sb中标签所占用的下标位置
             */
            List<Integer> indexes = IntStream.range(0, sb.length()).filter(i -> {
                boolean present = tagPositions.stream().filter(position -> position.getStart() <= i && i < position.getEnd()).findAny().isPresent();
                return !present;
            }).boxed().collect(Collectors.toList());
            boolean flag = ThreadLocalRandom.current().nextInt(10) / 2 < 5;
            List<TagPosition> collect = tagPositions.stream()
                    .filter(i -> labelTag.getTagMode().contains(i.getTagName()))
                    .sorted(Comparator.comparing(TagPosition::getStart))
                    .collect(Collectors.toList());
            /**
             * 同名标签，按照一组为单位，计算标签覆盖的位置区间
             */
            Function<List<TagPosition>, List<String>> convert = (tags -> {
                List<String> result = Lists.newArrayList();
                if (CollectionUtils.isEmpty(tags) || tags.size() % 2 == 1) {
                    return result;
                }
                for (int i = 0; i < tags.size(); i += 2) {
                    TagPosition start = tags.get(i);
                    TagPosition end = tags.get(i + 1);
                    result.add(start.getStart() + SPLIT_CHAR + end.getEnd());
                }
                return result;
            });
            List<String> regions = convert.apply(collect);
            if (flag) {        //再随机嵌套一个标签过来
                LabelTag tempLabel = Arrays.stream(LabelTag.values()).filter(i -> i != labelTag).findAny().get();
                List<TagPosition> tempList = tagPositions.stream()
                        .filter(i -> tempLabel.getTagMode().contains(i.getTagName()))
                        .sorted(Comparator.comparing(TagPosition::getStart))
                        .collect(Collectors.toList());
                regions.addAll(convert.apply(tempList));
                newLabelTag = tempLabel;
            }
            indexes.removeIf(i -> regions.parallelStream().filter(region -> {
                String[] s = region.split(SPLIT_CHAR);
                return Integer.parseInt(s[0]) <= i && Integer.parseInt(s[1]) >= i;
            }).findAny().isPresent());

            if (CollectionUtils.isEmpty(indexes)) {
                return;
            }
            List<String> regionList = getIndexesRegionList(indexes);
            if (CollectionUtils.isEmpty(regionList)) {
                return;
            }

            String s = regionList.get(ThreadLocalRandom.current().nextInt(regionList.size()));
            //截取某一个区间的index做标签开始结束标签下标，这样才能保证同名的标签不互相嵌套
            indexes = IntStream.range(Integer.parseInt(s.split(SPLIT_CHAR)[0]), Integer.parseInt(s.split(SPLIT_CHAR)[1]) + 1).boxed().collect(Collectors.toList());
            Integer min = -1;
            Integer max = -1;
            while (min < 0 || max < min || max < 0) {
                int i = indexes.get(ThreadLocalRandom.current().nextInt(indexes.size()));
                if (min < 0) {
                    min = i;
                    continue;
                }
                if (min > i) {
                    max = min;
                    min = i;
                } else if (min < i) {
                    max = i;
                }
            }
            String substring = sb.substring(min, max);
            int tagNum = tagPositions.size() / 2;
            String format = createFormat(substring, labelTag, tagNum);
            if (flag && null != newLabelTag) {        //再随机嵌套一个标签过来
                format = createFormat(format, newLabelTag, tagNum + 1);

            }
            sb.replace(min, max, format);
        }

        private static List<String> getIndexesRegionList(List<Integer> indexes) {
            List<String> list = Lists.newArrayList();
            int min = indexes.parallelStream().mapToInt(Integer::new).min().getAsInt();
            int max = indexes.parallelStream().mapToInt(Integer::new).max().getAsInt();
            List<Integer> collect = IntStream.range(min - 1, max + 2).boxed().collect(Collectors.toList());
            int start = -1;
            for (Integer index : collect) {
                if (start == -1 && indexes.contains(index)) {
                    start = index;
                } else if (start != -1 && !indexes.contains(index)) {
                    if (start < index - 1) {
                        list.add(start + SPLIT_CHAR + (index - 1));
                    }
                    start = -1;
                }
            }
            return list;
        }

        private static String createFormat(String substring, LabelTag labelTag, int tagNum) {
            String tagMode = labelTag.getTagMode();
            String description = labelTag.getDescription() + descList.get(ThreadLocalRandom.current().nextInt(descList.size()));
            int i = ThreadLocalRandom.current().nextInt(4);
            int score = ThreadLocalRandom.current().nextInt(5);
            String value = "";
            switch (labelTag) {
                case THESIS_SCORE:
                    value = i + "-" + ThreadLocalRandom.current().nextInt(1000);
                    break;
                case EVIDENCE_SCORE:
                    value = i + SPLIT_CHAR + tagNum;
                    break;
                default:
                    value = i + "";

            }
            String format = String.format(tagMode, value, (tagNum + 1) + "", description, score + "", substring);
            return format;
        }
    }

    /**
     * 转换智能批改标签为APP展示标签
     *
     * @param content
     * @return
     */
    public static String convertIntellect2Mobile(StringBuilder content) {
        content.append("。");
        List<TagPosition> positions = getTagPositions(content);
        boolean b = checkTagRule(positions);
        if (!b) {
            throw new BizException(ErrorResult.create(1000101, "标签规则不对"));
        }
        List<TagPosition> mergePositions = mergePosition2Region(positions, content);
        mergeTag(content, mergePositions);
        content.deleteCharAt(content.length() - 1);
        return content.toString();
    }

    /**
     * 替换合并标签字符串
     *
     * @param content
     * @param positions
     */
    public static void mergeTag(StringBuilder content, List<TagPosition> positions) {
        positions.sort(Comparator.comparing(TagPosition::getStart));
        Map<String, TagPosition> map = Maps.newHashMap();        //开始标签或者结束标签所在区间和标签对象的映射关系
        List<String> regions = Lists.newArrayList();
        int seq = 1;
        for (TagPosition position : positions) {
            if (FINAL_TAG_NAME.equals(position.getTagName())) {
                position.setTagName(position.getTagName() + SPLIT_CHAR + seq);
            }
            String startRegion = position.getStart() + SPLIT_CHAR + position.getEnd();
            String endRegion = position.getEndTagRegion();
            regions.add(startRegion);
            regions.add(endRegion);
            map.put(startRegion, position);
            map.put(endRegion, position);
            seq++;
        }
        regions.sort(Comparator.comparing(i -> -Integer.parseInt(i.split(SPLIT_CHAR)[0])));      //倒序排列标签区间
        checkRegion(regions);
        System.out.println("regions = " + gson.toJson(regions));
        for (String region : regions) {
            TagPosition tagPosition = map.get(region);
            String[] s = region.split(SPLIT_CHAR);
            if (region.equals(tagPosition.getEndTagRegion())) {       //结束标签替换
                souReplaceInfo(content, Integer.parseInt(s[0]), Integer.parseInt(s[1]), String.format("</%s>", tagPosition.getTagName()));
            } else {      //开始标签替换
                int drawType = tagPosition.isHighLight() ? (tagPosition.isUnderLine() ? 2 : 1) : 0;
                souReplaceInfo(content, Integer.parseInt(s[0]), Integer.parseInt(s[1]),
                        String.format(label_tag,
                                tagPosition.getTagName(),
                                tagPosition.getTagName().split(SPLIT_CHAR)[1],
                                tagPosition.getDescription(),
                                tagPosition.getScore(),
                                drawType));

            }
        }

    }

    private static void checkRegion(List<String> regions) {
        int i = Integer.MAX_VALUE;
        for (String region : regions) {
            String[] s = region.split(SPLIT_CHAR);
            if (i >= Integer.parseInt(s[1]) && Integer.parseInt(s[1]) >= Integer.parseInt(s[0])) {
                i = Integer.parseInt(s[0]);
            } else {
                System.out.println(i + "|" + s[1] + "|" + s[0]);
                throw new BizException(ErrorResult.create(1001234, "标签区间重叠，解析失败"));
            }
        }
    }

    private static void souReplaceInfo(StringBuilder content, int start, int end, String format) {
        content.replace(start, end, format);
    }

    /**
     * 合并标签对象
     *
     * @param positions
     * @param content
     * @return
     */
    private static List<TagPosition> mergePosition2Region(List<TagPosition> positions, StringBuilder content) {
        int length = content.length();
        //文字内容的下标集合
        List<Integer> indexList = IntStream.range(0, length).filter(i -> !positions.stream()
                .filter(position -> position.getStart() <= i && i < position.getEnd())
                .findAny()
                .isPresent())
                .boxed().collect(Collectors.toList());
        indexList.sort(Integer::compareTo);
        Map<String, List<TagPosition>> tagPositionMap = Maps.newHashMap();
        for (int i = 0; i < indexList.size(); i++) {        //所有文字下标遍历
            Integer index = indexList.get(i);
            filterStartPosition(positions, index, tagPositionMap);
            if (i - 1 > 0) {
                filterEndPosition(positions, index, indexList.get(i - 1), tagPositionMap);
            }
        }
        positions.removeIf(i -> StringUtils.isBlank(i.getContentRegion()) || StringUtils.isBlank(i.getEndTagRegion()));       //删除结束标签，开始标签中已包含结束标签所需内容
        return getFinalTags(positions);

    }

    private static List<TagPosition> getFinalTags(List<TagPosition> positions) {
        Map<String, List<TagPosition>> regionMap = positions.stream().collect(Collectors.groupingBy(TagPosition::getContentRegion));        //对标签分组
        List<TagPosition> result = Lists.newArrayList();
        for (Map.Entry<String, List<TagPosition>> entry : regionMap.entrySet()) {
            String contentRegion = entry.getKey();
            if (StringUtils.isBlank(contentRegion)) {
                continue;
            }
            List<TagPosition> value = entry.getValue();
            int start = -1;
            int end = -1;
            int tailTagStart = -1;
            int tailTagEnd = -1;
            double score = 0D;
            StringBuilder description = new StringBuilder();
            BiFunction<Integer, Integer, Integer> getMax = ((a, b) -> Math.max(a, b));
            BiFunction<Integer, Integer, Integer> getMin = ((a, b) -> {
                if (a == -1 || b == -1) {
                    return Math.max(a, b);
                } else {
                    return Math.min(a, b);
                }
            });
            for (TagPosition tagPosition : value) {
                start = getMin.apply(start, tagPosition.getStart());
                end = getMax.apply(end, tagPosition.getEnd());
                tailTagStart = getMin.apply(tailTagStart, Integer.parseInt(tagPosition.getEndTagRegion().split(SPLIT_CHAR)[0]));
                tailTagEnd = getMax.apply(tailTagEnd, Integer.parseInt(tagPosition.getEndTagRegion().split(SPLIT_CHAR)[1]));
                score += tagPosition.getScore();
                description.append(tagPosition.getDescription()).append(",");
            }
            description.deleteCharAt(description.length() - 1);
            TagPosition label = TagPosition.builder().start(start).end(end).score(score).description(description.toString())
                    .contentRegion(contentRegion).contentStart(Integer.parseInt(contentRegion.split(SPLIT_CHAR)[0])).contentEnd(Integer.parseInt(contentRegion.split(SPLIT_CHAR)[1]))
                    .tagName(FINAL_TAG_NAME).endTagRegion(tailTagStart + SPLIT_CHAR + tailTagEnd).build();
            label.setUnderLine(value.stream().map(TagPosition::getTagName).anyMatch(i -> UNDER_LINE_TAG.stream().filter(name -> i.indexOf(name) > -1).findFirst().isPresent()));
            label.setHighLight(value.stream().map(TagPosition::getTagName).anyMatch(i -> HIGH_LIGHT_TAG.stream().filter(name -> i.indexOf(name) > -1).findFirst().isPresent()));
            result.add(label);
        }
        return result;
    }

    /**
     *
     * @param positions
     * @param index
     * @param preIndex
     * @param tagPositionMap
     */
    private static void filterEndPosition(List<TagPosition> positions, Integer index, Integer preIndex, Map<String, List<TagPosition>> tagPositionMap) {
        for (TagPosition position : positions) {
            String tagName = position.getTagName();
            List<TagPosition> tagPositions = tagPositionMap.get(tagName);
            if (position.getContentStart() > 0 ||        //contentStart有值表示是已被识别的开始标签
                    position.getContentEnd() > 0 ||  //contentEnd 有值表示已被识别的结束标签
                    position.getScore() > 0) {      //score大于0表示为为开始节点
                continue;
            }
            if (StringUtils.isBlank(position.getDescription()) &&
                    position.getStart() > preIndex &&
                    position.getEnd() <= index &&
                    position.getContentEnd() == 0) {
                if (CollectionUtils.isEmpty(tagPositions)) {      //tagPositions表示无对应的开始标签
                    System.out.println("position 无开始节点对应 = " + gson.toJson(position));
                    continue;
                }
                position.setContentEnd(preIndex + 1);   //结束节点为上个节点的index
                TagPosition tagPosition = tagPositions.get(tagPositions.size() - 1);
                tagPosition.setContentEnd(preIndex + 1);
                tagPosition.setContentRegion(tagPosition.getContentStart() + SPLIT_CHAR + (preIndex + 1));
                tagPosition.setEndTagRegion(position.getStart() + SPLIT_CHAR + position.getEnd());
                tagPositions.removeIf(i -> StringUtils.isNotBlank(i.getContentRegion()));

            }
        }
    }

    /**
     * 筛选刚开始的标签
     *
     * @param positions      所有标签对象
     * @param index          文字下标
     * @param tagPositionMap 带适配结束标签的标签集合
     * @return
     */
    private static void filterStartPosition(List<TagPosition> positions, Integer index, Map<String, List<TagPosition>> tagPositionMap) {
        for (TagPosition position : positions) {
            int contentStart = position.getContentStart();
            int end = position.getEnd();
            String description = position.getDescription();
            if (StringUtils.isNotBlank(description) &&      //开始标签
                    end <= index && contentStart == 0) {         //下标大于结束下标且文字内容未开始
                position.setContentStart(index);
                List<TagPosition> orDefault = tagPositionMap.getOrDefault(position.getTagName(), Lists.newArrayList());
                orDefault.add(position);
                tagPositionMap.put(position.getTagName(), orDefault);
            }
        }
    }

    /**
     * 将标签抽象成对象TagPosition，start+end+tagName,标签左边部分包含description+score,右边部分不包含
     *
     * @param content
     * @return
     */
    private static List<TagPosition> getTagPositions(StringBuilder content) {
        ArrayList<TagPosition> tagPositions = Lists.newArrayList();
        if (StringUtils.isEmpty(content)) {
            return tagPositions;
        }
        Pattern pattern = Pattern.compile("<[/]?([0-9|_|A-Z|a-z]+Score)[^>]*>");
        Matcher matcher = pattern.matcher(content);
        int index = 0;
        while (matcher.find(index)) {
            TagPosition build = TagPosition.builder()
                    .start(matcher.start())
                    .end(matcher.end())
                    .tagName(matcher.group(1)).build();
            fillTagName(build, matcher.group());
            tagPositions.add(build);
            index = matcher.end();
        }

        return tagPositions;
    }

    private static boolean checkTagRule(List<TagPosition> tagPositions) {
        if (CollectionUtils.isEmpty(tagPositions)) {
            return true;
        }
        Map<String, List<TagPosition>> collect = tagPositions.stream().collect(Collectors.groupingBy(TagPosition::getTagName));

        for (List<TagPosition> value : collect.values()) {
            if (value.size() % 2 == 1) {
                return false;
            }
            for (int i = 1; i < value.size(); i += 2) {     //判断第偶数个标签为结尾标签（description不为空）
                TagPosition tagPosition = value.get(i);
                if (StringUtils.isNotBlank(tagPosition.getDescription())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 分析标签内容，获得标签名称和属性内容
     *
     * @param position
     * @param group
     */
    private static void fillTagName(TagPosition position, String group) {
        BiFunction<String, String, String> getAttr = ((target, name) -> {
            int index = target.indexOf(name);
            int start = 0;
            int end = target.length();
            if (index > -1) {
                start = target.indexOf('\"', index + 1);
                if (start > -1) {
                    end = target.indexOf('\"', start + 1);
                }
            }
            if (end > start && index != -1) {
                return target.substring(start + 1, end);
            }
            return "";
        });
        position.setDescription(getAttr.apply(group, "description"));
        position.setScore(Doubles.tryParse(NumberUtils.isNumber(getAttr.apply(group, "score")) ? getAttr.apply(group, "score") : "0"));
    }


    public static void main(String[] args) {
        test1();
    }


    private static void test1() {
        /**
         * 生成测试数据
         */
        StringBuilder target = new StringBuilder("弘扬工匠精神\n" +
                "夯实大国地位\n" +
                "  何谓“工匠精神”？作为进年来热门的词汇，很多人只是人云亦云，并不知其所以然。<thesisScore value=\"3\" description=\"“其实，在我国国际地位日益提升的今天，工匠精神的传承是极其重要的，从非物质文化遗产的保护到政府群众工作的开展，“工匠精神”尤其发挥着它的作用。”基本体现了“国家的发展需要有工匠精神。”\" score=\"-1\">其实，在我国国际地位日益提升的今天，工匠精神的传承是极其重要的，从非物质文化遗产的保护到政府群众工作的开展，“工匠精神”尤其发挥着它的作用。</thesisScore>\n" +
                "    <thesisScore value=\"3\" description=\"“在我国经济实力日益发展壮大的今天，人民群众在享受物质生活水平日益提高的今天，在我国城市建设如火如荼紧锣密鼓进行的今天，“工匠精神”越来越被各行各业的人们所重视。”基本体现了“时代需要追求卓越，专业敬业的精神。”\" score=\"-1\">在我国经济实力日益发展壮大的今天，人民群众在享受物质生活水平日益提高的今天，在我国城市建设如火如荼紧锣密鼓进行的今天，“工匠精神”越来越被各行各业的人们所重视。</thesisScore>\n" +
                "    <thesisScore value=\"2\" description=\"“工匠精神”是不忘初心，坚守信念。”较充分体现了“时代发展需要工匠精神。”\" score=\"-1\">工匠精神”是不忘初心，坚守信念。</thesisScore><evidenceScore value=\"2-1\" description=\"“任何一件事情的成功都不是没有原因的，有的人在事业蒸蒸日上的时候逐渐迷失了自己，辨不清方向就会默默的毁……”比较充分论证了“工匠精神”是不忘初心，坚守信念。”这个思想\" score=\"-1\">任何一件事情的成功都不是没有原因的，有的人在事业蒸蒸日上的时候逐渐迷失了自己，辨不清方向就会默默的毁掉了自己；很多小企业满足于依靠廉价劳动力走量获得了收益，没有致力于发展创新，以提高自己的实力最终逐渐被市场淘汰；有的传统工艺人为了眼前的利益而不与时俱进，缺乏传承者的状况也不改善，忘记自己的初心最终不仅使得自己的利益得不到保护也损害了传统文化的发展。</evidenceScore>只有将“工匠精神”牢记于心，贯彻始终，个人、集体和国家的权利才能够有机统一发展。\n" +
                "    “工匠精神”是党政军群联系群众，联合群众，做好群众工作的利剑。一切依靠群众，一切为了群众，一切群众由人民共享。政府的工作离不开群众的支持，为了更好的开展工作，将“工匠精神”融入于平时的工作，减少群众的负担是我党和国家一直努力的方向。提高公务人员工作效率，使得政务公开透明，运用“互联网+”思维，建设一体化政务服务体系。\n" +
                "    <thesisScore value=\"2\" description=\"““工匠精神”是国家综合国力稳固提升的保障。”较充分体现了“时代发展需要工匠精神。”\" score=\"-1\">“工匠精神”是国家综合国力稳固提升的保障。</thesisScore>\n" +
                "\t<1_literaryScore value=\"1-5\" description=\"“国家的发展离不开“工匠精神”。国家的进步离不开“工匠精神”。”为对偶句\" score=\"-1\"><2_literaryScore value=\"1-1\" description=\"“国家的发展离不开“工匠精神”。国家的进步离不开“工匠精神”。国家的改革离不开“工匠精神”。”为排比句\" score=\"-1\"><evidenceScore value=\"3-1\" description=\"“国家的发展离不开“工匠精神”。”基本论证了““工匠精神”是国家综合国力稳固提升的保障。”这个思想\" score=\"-1\">国家的发展离不开“工匠精神”。国家的进步离不开“工匠精神”。</1_literaryScore>国家的改革离不开“工匠精神”。</evidenceScore></2_literaryScore>“工匠精神”所要求的自律性和传承性，就是国家发展进步中所要坚持的东西，如果缺少了它，那么必将阻碍我国发展改革的道路，使得发展之路充满荆棘。\n" +
                "    “工匠精神”是一种民族传承，是国家兴旺发达的不竭动力，是国家综合国力提升的重要保障。夯实国家地位，需要不断注入新的创新活力，不断突破陈旧，建立起自己的形象，国家的兴旺发达就需要始终贯彻“工匠精神”，将“工匠精神”融入于国家建设的方方面面，这样才有利于文化的传承，社会的进步，国家的兴旺。弘扬工匠精神，夯实大国地位。<structScore value=\"1\" description=\"“为社会主义全面建成小康社会而奋斗，为新时代中国特色社会主义而奋斗，为生生不息的中华民族而奋斗！”结束全文，结构完整\" score=\"-1\">为社会主义全面建成小康社会而奋斗，为新时代中国特色社会主义而奋斗，为生生不息的中华民族而奋斗！</structScore>");

//        System.out.println("target = " + target.toString());
//        Set<LabelTag> labelTags = Sets.newHashSet();
//        int length = LabelTag.values().length;
//        while (labelTags.size() < length) {
//            LabelTag labelTag = LabelTag.values()[ThreadLocalRandom.current().nextInt(length)];
//            LabelTag.insertTag(target, labelTag);
//            labelTags.add(labelTag);
//        }
//        for (LabelTag value : LabelTag.values()) {
//            LabelTag.insertTag(target, value);
//        }
        System.out.println("target.toString() = " + target.toString());
        /**
         * 将标签合并成
         */
        String result = convertIntellect2Mobile(target);
        System.out.println("result = " + result);

    }


    /**
     * 分析批注标签内容获取详细批注内容
     * @param correctedContent         答案批改后内容
     * @param document
     * @param isWriteContent        是否将批改后的文字答案写入pdf
     * @return
     */
    public List<TagPosition>  correctManual(String correctedContent, Document document, boolean isWriteContent) {
        StringBuilder sb = new StringBuilder(correctedContent);
        Pattern pattern = Pattern.compile("<[/]?label_([0-9]+)[^>]*>");
        Matcher matcher = pattern.matcher(sb);
        int index = 0;
//        LinkedList<TagPosition>
        if(matcher.find(index)){
//            if(){
//
//            }
//            matcher.group()
        }
        return null;
    }

    /**
     * Chunk字符串内容实例化
     *
     * @return
     */
    public static Chunk instantiateChunk(String content, Font font) {
        Chunk chunk = new Chunk(content, font);
        //中文内容标点出现在行首的处理方案
        chunk.setSplitCharacter(ChineseSplitCharacter.SplitCharacter);
        return chunk;
    }
}
