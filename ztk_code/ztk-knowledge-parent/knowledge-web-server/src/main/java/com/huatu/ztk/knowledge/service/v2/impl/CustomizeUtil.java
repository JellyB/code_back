package com.huatu.ztk.knowledge.service.v2.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.collections.CollectionUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户抽题策略包装
 */
public class CustomizeUtil {

    /**
     * 获取用户抽题策略
     *
     * @param questionPoints 抽取的知识点（包含题量）
     * @param finishCountMap 每个知识点已做完题量
     * @param size           单位抽题量
     * @return
     */
    public static Map<CustomizeStrategyEnum, List<Integer>> assignPointStrategy(List<QuestionPoint> questionPoints, Map<Integer, Integer> finishCountMap, int size) {
        HashMap<Integer, Integer> unFinishCountMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(questionPoints)) {
            return Maps.newHashMap();
        }
        int unfinishedTotal = 0;
        for (QuestionPoint questionPoint : questionPoints) {
            int id = questionPoint.getId();
            int qnumber = questionPoint.getQnumber();
            System.out.println("qnumber = " + qnumber);
            Integer finishCount = finishCountMap.getOrDefault(id, 0);
            System.out.println("finishCount = " + finishCount);
            if (qnumber > finishCount) {
                unFinishCountMap.put(id, qnumber - finishCount);
                unfinishedTotal += (qnumber - finishCount);
            }
        }
        Map<CustomizeStrategyEnum, List<Integer>> result = Maps.newHashMap();
        System.out.println("unfinishedTotal = " + unfinishedTotal);
        System.out.println("size = " + size);
        if (unfinishedTotal <= 0) {     //都完成了
            result.put(CustomizeStrategyEnum.ERROR_COUNT, finishCountMap.keySet().stream().collect(Collectors.toList()));
        } else if ((unfinishedTotal > 0 && unfinishedTotal <= size)) {       //存在遗留的试题
            result.put(CustomizeStrategyEnum.NO_REPEAT, unFinishCountMap.keySet().stream().collect(Collectors.toList()));
            result.put(CustomizeStrategyEnum.ERROR_COUNT, finishCountMap.keySet().stream().collect(Collectors.toList()));
        } else {        //足够多的未完成试题
            result.put(CustomizeStrategyEnum.NO_REPEAT, filterPointId(unFinishCountMap, size));
        }
        return result;
    }

    /**
     * 根据每个知识点未完成数量筛选特定的知识点数量
     *
     * @param unFinishCountMap
     * @param size
     * @return
     */
    private static List<Integer> filterPointId(HashMap<Integer, Integer> unFinishCountMap, int size) {
        List<Map.Entry<Integer, Integer>> entries = unFinishCountMap.entrySet().stream().sorted(Comparator.comparing(i -> -i.getValue())).collect(Collectors.toList());
        List<Integer> ids = Lists.newArrayList();
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : entries) {
            ids.add(entry.getKey());
            total += entry.getValue();
            if (total >= size) {
                break;
            }
        }
        if (ids.size() == 1 && entries.size() > ids.size()) {   //一个知识点下的未做题量满足抽题数，但是不止一个未做完知识点时，再额外多取至多两个知识点
            List<Integer> collect = entries.stream().map(Map.Entry::getKey).collect(Collectors.toList());
            ids.addAll(collect.subList(1, Math.min(entries.size(), 3)));
        }
        return ids;
    }

    /**
     * 抽题策略枚举
     */
    @AllArgsConstructor
    @Getter
    public enum CustomizeStrategyEnum {
        NO_REPEAT(1, "不重复抽题"),
        ERROR_COUNT(2, "错题量抽题"),
        DEFAULT(-1, "不抽题");
        private int code;
        private String value;
    }
}
