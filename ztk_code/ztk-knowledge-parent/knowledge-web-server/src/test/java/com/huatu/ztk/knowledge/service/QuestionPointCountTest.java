package com.huatu.ztk.knowledge.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.knowledge.constant.CustomizeEnum;
import com.huatu.ztk.knowledge.controller.v4.QuestionErrorControllerV4;

public class QuestionPointCountTest extends BaseTest {

    @Autowired
    private PoxyUtilService poxyUtilService;

    @Autowired
    private QuestionPointService questionPointService;

    @Autowired
    QuestionErrorControllerV4 questionErrorControllerV4;
    @Autowired
    QuestionErrorService questionErrorService;

    @Autowired
    QuestionStrategyDubboService questionStrategyDubboService;
    @Test
    public void test(){
        long userId = 233646703;
        List<QuestionPointTree> questionPointTrees = questionPointService.questionPointTreeNode(userId, 1, 0, CustomizeEnum.ModeEnum.Write);
        questionPointTrees.forEach(i->i.setChildren(Lists.newArrayList()));
        System.out.println("questionPointTrees = " + JsonUtil.toJson(questionPointTrees));
        Map<Integer, Integer> oldQnum = poxyUtilService.getQuestionPointService(1).countAll();
        Map<Integer, Integer> newQnum = poxyUtilService.getQuestionPointService(2).countAll();
        Map<Integer, Integer> oldFinishMap = poxyUtilService.getQuestionFinishService(1).countAll(userId);
        Map<Integer, Integer> newFinishMap = poxyUtilService.getQuestionFinishService(2).countAll(userId);
        Map<Integer, Integer> oldErrorMap = poxyUtilService.getQuestionErrorService(1).countAll(userId);
        Map<Integer, Integer> newErrorMap = poxyUtilService.getQuestionErrorService(2).countAll(userId);
        for (QuestionPointTree questionPointTree : questionPointTrees) {
            System.out.println("questionPointTree.getName() = " + questionPointTree.getName());
            int id = questionPointTree.getId();
            Function<Map<Integer,Integer>,Integer> get = (map->map.getOrDefault(id,0));
            System.out.println("oldQnum | oldFinishMap | oldErrorMap");
            System.out.println(get.apply(oldQnum)+"  | "+ get.apply(oldFinishMap)+" |" + get.apply(oldErrorMap));
            System.out.println("newQnum | newFinishMap | newErrorMap");
            System.out.println(get.apply(newQnum)+"  | "+ get.apply(newFinishMap)+" |" + get.apply(newErrorMap));
            System.out.println("*********************");
        }

        List<QuestionPointTree> combine = combine(questionPointTrees, newFinishMap, newErrorMap, newQnum, Maps.newHashMap(), oldFinishMap);
        System.out.println("combine = " + JsonUtil.toJson(combine));
    }

    private List<QuestionPointTree> combine(List<QuestionPointTree> treeList,
                                            Map<Integer, Integer> finishCountMap,
                                            Map<Integer, Integer> wrongCountMap,
                                            Map<Integer, Integer> pointQuestionMap,
                                            Map<Integer, Long> unfinishedPointIds,
                                            Map<Integer, Integer> allFinishCountMap) {
        for (QuestionPointTree questionPointTree : treeList) {
            final int pointId = questionPointTree.getId();
            final List<QuestionPointTree> children = questionPointTree.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {//含有子节点,则递归调用
                combine(children, finishCountMap, wrongCountMap, pointQuestionMap, unfinishedPointIds, allFinishCountMap);
            }
            /**
             * 总题量
             */
            final Integer qnum = pointQuestionMap.getOrDefault(pointId, 0);
            questionPointTree.setQnum(qnum);
            /**
             * 正确率计算
             */
            Integer allFinishCount = allFinishCountMap.getOrDefault(pointId, 0);
            Integer wrongCount = wrongCountMap.getOrDefault(pointId, 0);
            int accuracy = 0;
            wrongCount = Math.min(allFinishCount, wrongCount);
            if (allFinishCount > 0) {
                accuracy = 100 * (allFinishCount - wrongCount) / allFinishCount;
            }
            questionPointTree.setAccuracy(accuracy);
            //取最小值，防止出现完成次数比总题数多的情况
            final Integer finishCount = Math.min(finishCountMap.getOrDefault(pointId, 0), qnum);
            questionPointTree.setRnum(finishCount * accuracy / 100);
            questionPointTree.setWnum(finishCount * (100 - accuracy) / 100);
            //当前知识点是未完成的
            if (unfinishedPointIds.containsKey(pointId)) {
                questionPointTree.setUnfinishedPracticeId(unfinishedPointIds.get(pointId));
            }
        }
        //update by lijun 防止返回 试题数量 0 的节点信息
        //在缓存未更新的情况下，原始代码可能会出现问题，需要重启服务解决
        return treeList.stream().filter(questionPointTree -> questionPointTree.getQnum() > 0).collect(Collectors.toList());
    }


    @Test
    public void test1(){
        List<QuestionPointTree> questionPointTrees = questionErrorService.queryErrorPointTrees(233408436, 100100173);
        System.out.println("JsonUtil.toJson(questionPointTrees) = " + JsonUtil.toJson(questionPointTrees));

        QuestionStrategy questionStrategy = questionStrategyDubboService.randomErrorStrategy(233408436, -1, 100100173, 10);
        System.out.println("JsonUtil.toJson(questionStrategy) = " + JsonUtil.toJson(questionStrategy));
    }
}
