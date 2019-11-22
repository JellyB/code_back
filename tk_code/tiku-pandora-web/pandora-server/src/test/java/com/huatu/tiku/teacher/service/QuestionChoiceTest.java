package com.huatu.tiku.teacher.service;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.banckend.dao.manual.QuestionAdviceMapper;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.duplicate.ObjectiveDuplicatePartService;
import com.huatu.tiku.teacher.service.duplicate.QuestionDuplicateService;
import com.huatu.tiku.teacher.service.question.QuestionSearchService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.commons.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/11/1.
 */
@Slf4j
public class QuestionChoiceTest extends TikuBaseTest {

    @Autowired
    private CommonQuestionServiceV1 questionServiceV1;

    @Autowired
    private QuestionDuplicateService questionDuplicateService;

    @Autowired
    private ObjectiveDuplicatePartService objectiveDuplicatePartService;

    @Autowired
    private ImportService importService;

    @Autowired
    QuestionSearchService questionSearchService;

    @Autowired
    QuestionAdviceMapper questionAdviceMapper;
    @Test
    public void test(){
        List<Integer> list = Lists.newArrayList(21917048,21924690,21924707,21924730,40011634,40012109,40015470,40019731,40022014,40022015,40022017,40022020,40022021,40022022,40022023,40022026,40022027,40022028,40022029,40022030,40022031,40022033,40022036,40022040,40022043,40022139,40022141,40022143,40022157,40022163,40022165,40022166,40022168,40022169,40022170,40022171,40022174,40022175,40022176,40022180,40022181,40022183,40022184,40022186,40022187,40022193,40022233,40022303,40022308,40022314,40022315,40022438,40022441,40022448,40022449,40022450,40022454,40022457,40022458,40022459,40022460,40022461,40022462,40022465,40022467,40022469,40022471,40022472,40022474,40022475,40022480,40022482,40022483,40022485,40022492,40022598,40022600,40022602,40022622,40022624,40022625,40022626,40022627,40022628,40022629,40022633,40022634,40022635,40022636,40022637,40022638,40022639,40022640,40022644,40022654,40022655,40022658,40022664,40022743);
        List<Long> ids = list.stream().map(Long::new).collect(Collectors.toList());

        StopWatch stopWatch = new StopWatch("批量修改答案乱序问题");
//        Example example = new Example(BaseQuestion.class);
//        example.and().andIn("id",ids);
//        stopWatch.start("查询试题");
//        List<BaseQuestion> baseQuestions = questionServiceV1.selectByExample(example);
//        if(CollectionUtils.isEmpty(baseQuestions)){
//            return;
//        }
//        stopWatch.stop();
//        stopWatch.start("查询试题的复用ID");
//        Function<List<Long>,List<QuestionDuplicate>> supplier = (questions ->{
//            Example example = new Example(QuestionDuplicate.class);
//            example.and().andIn("questionId",questions);
//            List<QuestionDuplicate> questionDuplicates = questionDuplicateService.selectByExample(example);
//            return questionDuplicates;
//        });
//        List<QuestionDuplicate> questionDuplicates = supplier.apply(ids);
//        stopWatch.stop();
//        if(CollectionUtils.isEmpty(questionDuplicates)){
//            System.out.println(stopWatch.prettyPrint());
//            return;
//        }
//        stopWatch.start("查询复用数据并修改");
//        List<Long> duplicateIds = questionDuplicates.stream().map(QuestionDuplicate::getDuplicateId).collect(Collectors.toList());
//        Function<List<Long>,List<ObjectiveDuplicatePart>> function = (dups->{
//            Example example = new Example(ObjectiveDuplicatePart.class);
//            example.and().andIn("id",dups);
//            List<ObjectiveDuplicatePart> objectiveDuplicateParts = objectiveDuplicatePartService.selectByExample(example);
//            return objectiveDuplicateParts;
//        });
//        List<ObjectiveDuplicatePart> objectiveDuplicatePartList = function.apply(duplicateIds);
//        stopWatch.stop();
//        if(CollectionUtils.isEmpty(objectiveDuplicatePartList)){
//            System.out.println(stopWatch.prettyPrint());
//            return;
//        }
//        stopWatch.start("批量修改数据");
//        objectiveDuplicatePartList.parallelStream().forEach(i->{
//            i.setAnswer(answerSort(i.getAnswer()));
//            objectiveDuplicatePartService.save(i);
//        });
//        stopWatch.stop();
        stopWatch.start("同步到mongo");
        ids.stream().forEach(i->importService.sendQuestion2Mongo(i.intValue()));
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());

    }
    @Test
    public void test1(){
        System.out.println(answerSort("ABC"));
        System.out.println(answerSort("BAC"));
        System.out.println(answerSort("BDC"));
        System.out.println(answerSort("ECDA"));
    }
    public static String answerSort(String answer){
        char[] chars = answer.toCharArray();
        Arrays.sort(chars);
        return String.valueOf(chars);
    }
    @Test
    public void testSource(){
        ArrayList<Integer> ids = Lists.newArrayList(31216,42931,42933,43217,43220,43225,44987);
        List<Long> id = ids.stream().map(Long::new).collect(Collectors.toList());
        List<HashMap<String, Object>> questionSource = questionSearchService.findQuestionSource(id);
        if(CollectionUtils.isEmpty(questionSource)){
            System.out.println("null = " + "kong");
            return;
        }
        Map<Object, Object> collect = questionSource.stream().filter(i->i.containsKey("source")).collect(Collectors.toMap(k -> k.get("question_id"), v -> v.get("source")));
        List<String> sourceList = questionSource.stream().filter(i -> i.get("source") != null).map(i -> i.get("source").toString()).collect(Collectors.toList());
        String source = StringUtils.join(sourceList, "、");
        System.out.println("source = " + source);
        System.out.println("JsonUtil.toJson(collect)0 = " + JsonUtil.toJson(collect));
        Map<Object, Object> questionSourceMap = questionAdviceMapper.getQuestionTagInfo(id.stream().map(String::valueOf).collect(Collectors.joining(","))).stream().collect(Collectors.toMap(k -> k.get("questionId"), v -> v.get("source")));
        System.out.println("JsonUtil.toJson(collect)1 = " + JsonUtil.toJson(questionSourceMap));
    }
}


