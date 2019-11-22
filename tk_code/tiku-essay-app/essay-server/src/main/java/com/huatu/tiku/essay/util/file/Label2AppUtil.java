package com.huatu.tiku.essay.util.file;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.vo.file.TagPosition;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author huangqingpeng
 * @title: Label2AppUtil
 * @description: 人工批改标签转换
 * @date 2019-07-2210:33
 */
public class Label2AppUtil {

    private static final Gson gson = new Gson();

    public static final Function<Long, Map<String, String>> DEFAULT_FUNCTION_GET_COMMENT = (id -> Maps.newHashMap());

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/huangqingpeng/Documents/q.txt");
        String correctContent = FileUtils.readFileToString(file);
        Map<Integer, TagPosition> tagMap = getTagPositions(new StringBuilder(correctContent));        //标签序号和标签位置的对应关系
        System.out.println("tagMap = " + new Gson().toJson(tagMap));
        if (tagMap.isEmpty()) {
            return;
        }
        System.out.println("correctContent.length() = " + correctContent.length());
        StringBuilder labeledContent = new StringBuilder(correctContent);
        List<TagPosition> tagPositions = tagMap.values().stream().sorted(Comparator.comparing(TagPosition::getStart)).collect(Collectors.toList());
        System.out.println("tagPositions = " + new Gson().toJson(tagPositions));
        fillContentRegion(tagPositions, labeledContent);
        System.out.println("tagPositions = " + new Gson().toJson(tagPositions));
    }

    /**
     * 将人工批注信息转换成app展示标签内容
     *
     * @param essayLabelTotal
     * @param details
     * @param getAndConvertComments
     * @return
     */
    public static String label2App(EssayLabelTotal essayLabelTotal, List<EssayLabelDetail> details,
                                   Function<Long, Map<String, String>> getAndConvertComments) {
        String correctContent = essayLabelTotal.getLabeledContent();
        if (StringUtils.isBlank(correctContent) || //无批改文字内容
                details.stream().anyMatch(i -> StringUtils.isBlank(i.getContent()))) {          //无详细批注文字内容
            int seq = 1;
            StringBuilder sb = new StringBuilder();
            for (EssayLabelDetail detail : details) {
                Map<String, String> apply = getAndConvertComments.apply(detail.getId());
                boolean underLine = Boolean.parseBoolean(MapUtils.getString(apply, "underLine", "false"));
                boolean highLight = Boolean.parseBoolean(MapUtils.getString(apply, "highLight", "false"));
                //                   highLight
                //                       1
                //  underLine  0         2
                int drawType = highLight ? (underLine ? 2 : 1) : 0;
                sb.append(String.format(Intelligence2AppUtil.label_tag,
                        Intelligence2AppUtil.FINAL_TAG_NAME + "_" + seq,
                        seq,
                        MapUtils.getString(apply, "description", Strings.EMPTY),
                        MapUtils.getString(apply, "score", "0.0"),
                        drawType));
                seq++;
            }
            return sb.toString();
        }
        Map<Integer, TagPosition> tagMap = getTagPositions(new StringBuilder(correctContent));        //标签序号和标签位置的对应关系
        StringBuilder labeledContent = new StringBuilder(correctContent);
        if (tagMap.isEmpty()) {
            return labeledContent.toString();
        }
        List<TagPosition> tagPositions = tagMap.values().stream().sorted(Comparator.comparing(TagPosition::getStart)).collect(Collectors.toList());
        System.out.println("tagPositions = " + gson.toJson(tagPositions));
        fillContentRegion(tagPositions, labeledContent);
        fillDetailIdByContent(tagPositions, details);
        for (TagPosition tagPosition : tagPositions) {
            long detailId = tagPosition.getDetailId();
            if (detailId == 0) {
                continue;
            }
            Map<String, String> apply = getAndConvertComments.apply(detailId);
            tagPosition.setDescription(MapUtils.getString(apply, "description", Strings.EMPTY));
            tagPosition.setScore(Double.parseDouble(MapUtils.getString(apply, "score", "0")));
            tagPosition.setUnderLine(Boolean.parseBoolean(MapUtils.getString(apply, "underLine", "false")));
            tagPosition.setHighLight(Boolean.parseBoolean(MapUtils.getString(apply, "highLight", "false")));
            tagPosition.setTagName("label_" + tagPosition.getTagName());
        }
        System.out.println("tagPositions = " + gson.toJson(tagPositions));
        Intelligence2AppUtil.mergeTag(labeledContent, tagPositions);
        return labeledContent.toString();
    }

    private static void fillDetailIdByContent(List<TagPosition> tagPositions, List<EssayLabelDetail> details) {
        List<EssayLabelDetail> noMatchDetails = Lists.newArrayList();
        for (EssayLabelDetail detail : details) {
            String content = detail.getContent();
            Optional<TagPosition> first = tagPositions.stream().filter(i -> i.getDetailId() == 0)
                    .filter(i -> content.equals(i.getContent()))
                    .findFirst();
            if (first.isPresent()) {
                first.get().setDetailId(detail.getId());
            } else {
                noMatchDetails.add(detail);
            }
        }
        if (CollectionUtils.isEmpty(noMatchDetails) ||       //不存在未匹配的详细批注
                tagPositions.stream().filter(i -> i.getDetailId() == 0).count() == 0) {      //标签都已匹配详细批注
            return;
        }
        System.out.println("存在未匹配的详细批注 = " + gson.toJson(noMatchDetails));
        System.out.println("存在未匹配的标签 = " + gson.toJson(tagPositions.stream().filter(i -> i.getDetailId() == 0).collect(Collectors.toList())));
        for (EssayLabelDetail noMatchDetail : noMatchDetails) {
            String content = noMatchDetail.getContent();
            if (tagPositions.parallelStream().filter(i -> i.getDetailId() == 0).count() <= 0) {
                break;
            }
            TagPosition tagPosition = tagPositions.parallelStream().filter(i -> i.getDetailId() == 0).max(Comparator.comparing(i -> getSimilar(i.getContent(), content))).get();
            tagPosition.setDetailId(noMatchDetail.getId());
            System.out.println("匹配》》》》" + tagPosition.getContent() + "\n" + noMatchDetail.getContent());
        }
    }

    /**
     * 补充标签的文字内容区间
     *
     * @param tagPositions
     * @param labeledContent
     */
    private static void fillContentRegion(List<TagPosition> tagPositions, StringBuilder labeledContent) {
        List<String> positions = Lists.newArrayList();
        positions.addAll(tagPositions.stream().map(i -> i.getStart() + Intelligence2AppUtil.SPLIT_CHAR + i.getEnd()).collect(Collectors.toList()));
        positions.addAll(tagPositions.stream().map(TagPosition::getEndTagRegion).collect(Collectors.toList()));
        System.out.println("gson = " + gson.toJson(positions));
        positions.sort(Comparator.comparing(i -> Integer.parseInt(i.split(Intelligence2AppUtil.SPLIT_CHAR)[0])));
        for (TagPosition tagPosition : tagPositions) {
            StringBuilder content = new StringBuilder();
            int start = tagPosition.getEnd();
            int end = Integer.parseInt(tagPosition.getEndTagRegion().split(Intelligence2AppUtil.SPLIT_CHAR)[0]);
            for (; start < end; start++) {
                final int temp = start;
                boolean isTagIndex = positions.stream().filter(i -> {
                    String[] split = i.split(Intelligence2AppUtil.SPLIT_CHAR);
                    return Integer.parseInt(split[0]) <= temp && Integer.parseInt(split[1]) > temp;
                }).findAny().isPresent();
                if (isTagIndex) {     //标签所在下标直接掠过
                    continue;
                }
                content.append(labeledContent.charAt(start));
            }
            tagPosition.setContent(content.toString());
        }
    }

    private static Map<Integer, TagPosition> getTagPositions(StringBuilder labeledContent) {
        Pattern pattern = Pattern.compile("<font>△<font[^>]+>([0-9]+)</font>(<span[^>]+>[^<]+</span>)?△</font>");
        Matcher matcher = pattern.matcher(labeledContent);
        int index = 0;
        Map<Integer, TagPosition> resultMap = Maps.newHashMap();
        BiFunction<Integer, Map<Integer, TagPosition>, Integer> reCountIndex = (i, map) -> {
            if (MapUtils.isEmpty(map)) {
                return i;
            }
            List<int[]> collect = map.values().stream().map(position -> new int[]{position.getStart(), position.getEnd()}).collect(Collectors.toList());
            List<int[]> collect1 = map.values().stream().filter(tagPosition -> StringUtils.isNotBlank(tagPosition.getEndTagRegion()))
                    .map(tagPosition -> new int[]{Integer.parseInt(tagPosition.getEndTagRegion().split(Intelligence2AppUtil.SPLIT_CHAR)[0]), Integer.parseInt(tagPosition.getEndTagRegion().split(Intelligence2AppUtil.SPLIT_CHAR)[1])})
                    .collect(Collectors.toList());
            collect.addAll(collect1);
            collect.sort(Comparator.comparing(array -> array[0]));
            System.out.println("collect = " + new Gson().toJson(collect));
            List<int[]> tempList = IntStream.range(0, collect.size())
                    .boxed().map(j -> {
                        if (j == 0) {
                            return collect.get(0);
                        }
                        int k = j - 1;
                        if (collect.get(k)[1] > collect.get(j)[0]) {
                            collect.set(j, collect.get(k));
                            return collect.get(k);
                        }
                        return collect.get(j);
                    }).distinct().collect(Collectors.toList());


            System.out.println("tempList = " + new Gson().toJson(tempList));
            for (int[] ints : tempList) {
                if (ints[0] <= i) {
                    i += (ints[1] - ints[0]);
                } else {
                    break;
                }
            }
            return i;
        };
        while (matcher.find(index)) {
            String seq = matcher.group(1).trim();
            TagPosition tagPosition = resultMap.get(Integer.parseInt(seq));
            if (null == tagPosition) {
                TagPosition build = TagPosition.builder().tagName("label_"+seq).start(reCountIndex.apply(matcher.start(), resultMap)).end(reCountIndex.apply(matcher.end(), resultMap)).build();
                resultMap.put(Integer.parseInt(seq), build);
            } else {
                int start = reCountIndex.apply(matcher.start(), resultMap);
                int end = reCountIndex.apply(matcher.end(), resultMap);
                if (start > tagPosition.getStart()) {
                    tagPosition.setEndTagRegion(start + Intelligence2AppUtil.SPLIT_CHAR + end);
                } else {
                    tagPosition.setEndTagRegion(tagPosition.getStart() + Intelligence2AppUtil.SPLIT_CHAR + tagPosition.getEnd());
                    tagPosition.setStart(start);
                    tagPosition.setEnd(end);
                }
                System.out.println("tagPosition.getEndTagRegion()=" + tagPosition.getEndTagRegion());
            }
            labeledContent.delete(matcher.start(), matcher.end());
        }
        return resultMap;
    }

    public static double getSimilar(String strA, String strB) {
        String commonPhrase = longestCommonSubstring(strA, strB);
        double percent = 1.0 * commonPhrase.length() / strB.length();
        return percent;
    }
    /**
     * 比较两段文字中的重复文字
     */
    /**
     * 最长的子串
     *
     * @param strA
     * @param strB
     * @return
     */
    public static String longestCommonSubstring(String strA, String strB) {
        if (StringUtils.isNotEmpty(strA)) {
            strA = strA.replaceAll("[^\\u4e00-\\u9fa5]", "");//去除所有非中文字符
        } else {
            strA = "";
        }

        if (StringUtils.isNotEmpty(strB)) {
            strB = strB.replaceAll("[^\\u4e00-\\u9fa5]", "");//去除所有非中文字符
        } else {
            strB = "";
        }
        char[] chars_strA = strA.toCharArray();
        char[] chars_strB = strB.toCharArray();
        int m = chars_strA.length;
        int n = chars_strB.length;
        int[][] matrix = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (chars_strA[i - 1] == chars_strB[j - 1])
                    matrix[i][j] = matrix[i - 1][j - 1] + 1;
                else
                    matrix[i][j] = Math.max(matrix[i][j - 1], matrix[i - 1][j]);
            }
        }

        char[] result = new char[matrix[m][n]];
        int currentIndex = result.length - 1;
        while (matrix[m][n] != 0) {
            if (matrix[m][n] == matrix[m][n - 1])
                n--;
            else if (matrix[m][n] == matrix[m - 1][n])
                m--;
            else {
                result[currentIndex] = chars_strA[m - 1];
                currentIndex--;
                n--;
                m--;
            }
        }
        return new String(result);
    }
}
