package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.BaseTest;
import com.huatu.ztk.knowledge.api.QuestionStrategyDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.bean.QuestionStrategy;
import com.huatu.ztk.knowledge.constant.CustomizeEnum;
import com.huatu.ztk.knowledge.dao.QuestionUserMetaDao;
import com.huatu.ztk.knowledge.service.v1.QuestionErrorServiceV1;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.Question;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.*;
import java.util.stream.Collectors;

public class QuestionUserMetaServiceTest extends BaseTest {

    @Autowired
    private QuestionUserMetaDao questionUserMetaDao;

    @Autowired
    QuestionStrategyDubboService questionStrategyDubboService;
    @Autowired
    @Qualifier("questionErrorServiceImplV1")
    private QuestionErrorServiceV1 questionErrorServiceV1;

    @Autowired
    @Qualifier("questionErrorServiceImplV2")
    private QuestionErrorServiceV1 questionErrorServiceV2;


    @Autowired
    private QuestionErrorService questionErrorService;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private QuestionPointService questionPointService;
    @Test
    public void test() {
        Map<Integer, Integer> integerIntegerMap = questionUserMetaDao.countErrorQuestion(233358929);
        List<Integer> mongoIds = integerIntegerMap.entrySet().stream().filter(i -> 0 != i.getValue()).map(Map.Entry::getKey).collect(Collectors.toList());
        System.out.println("integerIntegerMap = " + integerIntegerMap.entrySet().stream().filter(i -> 0 != i.getValue()).count());
        Map<Integer, Integer> resultMap = questionErrorServiceV1.countAll(233358929);
        List<Integer> oldIds = resultMap.entrySet().stream().filter(i -> 0 != i.getValue()).map(Map.Entry::getKey).collect(Collectors.toList());
        System.out.println("resultMap = " + resultMap.entrySet().stream().filter(i -> 0 != i.getValue()).count());
        Map<Integer, Integer> map = questionErrorServiceV2.countAll(233358929);
        List<Integer> newIds = map.entrySet().stream().filter(i -> 0 != i.getValue()).map(Map.Entry::getKey).collect(Collectors.toList());
        System.out.println("resultMap1 = " + map.entrySet().stream().filter(i -> 0 != i.getValue()).count());
        compare(mongoIds,newIds);

    }


    @Test
    public void test1(){
        List<QuestionPointTree> questionPointTrees = questionErrorService.queryErrorPointTrees(233358929, 1);
        System.out.println("questionPointTrees = " + JsonUtil.toJson(questionPointTrees));
    }
    private void compare(List<Integer> mongoIds, List<Integer> oldIds) {
        Collection<Integer> intersection = CollectionUtils.intersection(mongoIds, oldIds);
        System.out.println("CollectionUtils.intersection(mongoIds,oldIds) = " + JsonUtil.toJson(intersection));
        System.out.println("oldIds = " + ListUtils.removeAll(oldIds,intersection));
        System.out.println("mongoIds = " + ListUtils.removeAll(mongoIds,intersection));
    }

    @Test
    public void test2(){
        String ids = "40064852,40064853,40064860,40064861,40064862,40064863,40064904,40064912,40064914,40064917,40064918,40064920,40064921,40064922,40064923,40064924,40064925,40064926,40064927,40064928,40064929,40064930,40064931,40064932,40064933,40064934,40064935,40064936,40064937,40064938,40064939,40064940,40064941,40064942,40064943,40064944,40064945,40064946,40064947,40064948,40064949,40064950,40064951,40064952,40064953,40064954,40064955,40064957,40064959,40064961,40064962,40064963,40064964,40064965,40064966,40064967,40064969,40064970,40064972,40064973,40064974,40064976,40064978,40064979,40064980,40064982,40064984,40064988,40064989,40064991,40064995,40064999,40065002,40065004,40065006,40065007,40065011,40065014,40065017,40065026,40065027,40065028,40065029,40065030,40065031,40065032,40065033,40065034,40065035,40065036";
        List<Integer> collect = Arrays.stream(ids.split(",")).map(Integer::parseInt).collect(Collectors.toList());
        List<Question> bath = questionDubboService.findBath(collect);
        Set<Integer> collect1 = bath.stream().map(Question::getId).collect(Collectors.toSet());
        System.out.println("ListUtils.removeAll(collect,collect1) = " + ListUtils.removeAll(collect, collect1));
    }


    @Test
    public void test3(){
        QuestionStrategy questionStrategy = questionStrategyDubboService.randomCustomizeStrategy(234694130, 2, 3340, 15);
        System.out.println("questionStrategy = " + JsonUtil.toJson(questionStrategy));
    }

    @Test
    public void test4(){
        List<QuestionPointTree> questionPointTrees = questionPointService.questionPointTreeNode(10186685, 1, 0, CustomizeEnum.ModeEnum.Write);
        System.out.println("questionPointTrees = " + JsonUtil.toJson(questionPointTrees));
    }
}
