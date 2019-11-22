//package com.huatu.tiku.essay.test;
//
//import com.google.common.collect.Maps;
//import com.huatu.tiku.essay.constant.status.EssayAnswerKeyWordConstant;
//import com.huatu.tiku.essay.entity.EssayQuestionBase;
//import com.huatu.tiku.essay.entity.EssayQuestionDetail;
//import com.huatu.tiku.essay.vo.*;
//import com.huatu.tiku.essay.vo.admin.*;
//import org.assertj.core.util.Lists;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
////import com.huatu.ztk.commons.JsonUtil;
//
///**
// * Created by huangqp on 2017\12\1 0001.
// */
//public class JsonTest {
//    private final static Logger logger = LoggerFactory.getLogger(JsonTest.class);
//    @Test
//    public void formatTest(){
//        AdminQuestionKeyWordVO keyWordVO = AdminQuestionKeyWordVO.builder().item("关键词1内容") .splitWords(new ArrayList<String>(){{add("关键词");add("1内容");}}).correspondingId(1).score(2).build();
//        AdminQuestionKeyWordVO keyWordVO_1 =AdminQuestionKeyWordVO.builder().item("近义词1内容").splitWords(new ArrayList<String>(){{add("近义词");add("1内容");}}).correspondingId(1).score(2).build();
//        keyWordVO.setSimilarWordVOList(new ArrayList(){{add(keyWordVO_1);}});
//        AdminQuestionKeyWordVO keyWordVO1 = AdminQuestionKeyWordVO.builder().item("关键词2内容") .splitWords(new ArrayList<String>(){{add("关键词");add("2内容");}}).correspondingId(1).score(2).build();
//        AdminQuestionKeyWordVO keyWordVO_2 =AdminQuestionKeyWordVO.builder().item("近义词2内容").splitWords(new ArrayList<String>(){{add("近义词");add("2内容");}}).correspondingId(1).score(2).build();
//        keyWordVO1.setSimilarWordVOList(new ArrayList(){{add(keyWordVO_2);}});
//        AdminQuestionKeyWordVO keyWordVO2 = AdminQuestionKeyWordVO.builder().item("关键词3内容") .splitWords(new ArrayList<String>(){{add("关键词");add("3内容");}}).correspondingId(1).score(2).build();
//        AdminQuestionKeyWordVO keyWordVO_3 =AdminQuestionKeyWordVO.builder().item("近义词3内容").splitWords(new ArrayList<String>(){{add("近义词");add("3内容");}}).correspondingId(1).score(2).build();
//        keyWordVO2.setSimilarWordVOList(new ArrayList(){{add(keyWordVO_3);}});
//        AdminQuestionFormatVO format  = AdminQuestionFormatVO.builder().type(1).titleInfo(AnswerSubFormatVO.builder().score(2).childKeyWords(new ArrayList(){{add(keyWordVO);}}).build())
//                .appellationInfo(AnswerSubFormatVO.builder().score(2).childKeyWords(new ArrayList(){{add(keyWordVO1);}}).build())
//                .inscribeInfo(AnswerSubFormatVO.builder().score(2).childKeyWords(new ArrayList(){{add(keyWordVO2);}}).build()).build();
//
//     //   System.out.println("cone= ： "+JsonUtil.toJson(format));
//    }
//    @Test
//    public void TestDeductRule(){
//        List<EssayStandardAnswerRuleSpecialStripVO> specialStripList = Lists.newArrayList();
//        List<EssayStandardAnswerRuleStripSegmentalVO> stripSegmentalList = Lists.newArrayList();
//        List<EssayStandardAnswerRuleStripSegmentalVO> normalStripList = Lists.newArrayList();
//        List<EssayStandardAnswerRuleWordNumVO> wordNumLimitList = Lists.newArrayList();
//        List<EssayStandardAnswerRuleWordNumVO> redundantSentenceList = Lists.newArrayList();
//        //特殊分条
//        EssayStandardAnswerRuleSpecialStripVO essayStandardAnswerRuleSpecialStripVO = EssayStandardAnswerRuleSpecialStripVO
//                .builder().deductType(1).deductScore(2).firstWord("以").secondWord("方面").wordNum(2).build();
//        specialStripList.add(essayStandardAnswerRuleSpecialStripVO);
//        //分段规则
//        EssayStandardAnswerRuleStripSegmentalVO essayStandardAnswerRuleStripSegmental0 = EssayStandardAnswerRuleStripSegmentalVO.builder()
//                .type(1).nextType(1).deductType(1).deductScore(2).build();
//        EssayStandardAnswerRuleStripSegmentalVO essayStandardAnswerRuleStripSegmental1 = EssayStandardAnswerRuleStripSegmentalVO.builder()
//                .type(1).nextType(2).deductType(2).deductScore(5).maxParagraphNum(1000).build();
//        stripSegmentalList.add(essayStandardAnswerRuleStripSegmental0);
//        stripSegmentalList.add(essayStandardAnswerRuleStripSegmental1);
//        //分条规则
//        EssayStandardAnswerRuleStripSegmentalVO essayStandardAnswerRuleStripSegmental2 = EssayStandardAnswerRuleStripSegmentalVO.builder()
//                .type(2).deductType(1).deductScore(2).build();
//        normalStripList.add(essayStandardAnswerRuleStripSegmental2);
//        //字数限制
//        EssayStandardAnswerRuleWordNumVO wordLimit = EssayStandardAnswerRuleWordNumVO.builder()
//                .type(1).nextType(3).minWordNum(100).maxWordNum(500).firstDeductType(2)
//                .firstDeductTypeWordNum(20).firstDeductTypeScore(1).build();
//        wordNumLimitList.add(wordLimit);
//        //句子冗余
//        EssayStandardAnswerRuleWordNumVO redundantSentence =  EssayStandardAnswerRuleWordNumVO.builder()
//                .type(2).nextType(4).maxWordNum(20).firstDeductType(3).firstDeductTypeScore(1).build();
//        redundantSentenceList.add(redundantSentence);
//        EssayQuestionDeductRuleVO rootRule = EssayQuestionDeductRuleVO.builder()
//                .questionDetailId(1)
//                .specialStripList(specialStripList)
//                .build();
//      //  System.out.print(JsonUtil.toJson(rootRule));
//    }
//    public Map getResult(Object obj){
//        Map mapData = Maps.newHashMap();
//        mapData.put("code",1000000);
//        mapData.put("data",obj);
//        return mapData;
//    }
//    @Test
//    public void test(){
//        getAdminQuestionKeyWordVo();
//        AdminQuestionKeyPhraseVO adminQuestionKeyPhraseVO = AdminQuestionKeyPhraseVO.builder().item("这是一个关键句").score(4).position(1).questionDetailId(2).keyWordVOList(new ArrayList<AdminQuestionKeyWordVO>(){{add(getAdminQuestionKeyWordVo());}})
//                .build();
//      //  System.out.print(JsonUtil.toJson(adminQuestionKeyPhraseVO));
//    }
//    public AdminQuestionKeyWordVO getAdminQuestionKeyWordVo(){
//        AdminQuestionKeyWordVO adminQuestionKeyWordVO = AdminQuestionKeyWordVO.builder()
//                .questionDetailId(1).correspondingId(1).type(EssayAnswerKeyWordConstant.QUESTION_PARENT_KEYWORD)
//                .score(2).item("关键词1").splitWords(new ArrayList<String>(){{add("关键词");add("1");}})
//                .build();
//        AdminQuestionKeyWordVO child = AdminQuestionKeyWordVO.builder().item("近义词1").splitWords(new ArrayList<String>(){{add("近义词");add("1");}})
//                .score(2).build();
//        adminQuestionKeyWordVO.setSimilarWordVOList(new ArrayList<AdminQuestionKeyWordVO>(){{add(child);}});
//        return adminQuestionKeyWordVO;
//    }
//    @Test
//    public void testQuestionDetail(){
//        EssayQuestionVO question = EssayQuestionVO.builder()
//                .questionBaseId(1L)
//                .questionDetailId(1L)
//                .sort(1)
//                .stem("试题题干")
//                .build();
//    }
//    @Test
//    public void testAnswerRule(){
//        AdminQuestionDeductRuleVO rule = new AdminQuestionDeductRuleVO();
//        LinkedList<AdminCommonDeductVO> rules = com.google.common.collect.Lists.newLinkedList();
//        rule.setDeductRuleList(rules);
//        rule.setQuestionDetailId(1);
//        //特殊分条
//        AdminCommonDeductVO specilStrip = AdminCommonDeductVO.builder()
//                .type("2_1")
//                .specialWords(new ArrayList<String>(){{add("特殊部分1");add("特殊部分2");add("特殊部分3");add("特殊部分4");}})
//                .deductType(2).deductTypeScorePercent(20).build();
//
//        rules.add(specilStrip);
//        //字数限制
//        AdminCommonDeductVO wordNum = AdminCommonDeductVO.builder()
//                .type("1_1")
//                .minNum(100).maxNum(200)
//                .deductTypeNum(20).deductTypeScorePercent(2).build();
//        rules.add(wordNum);
//        //句子冗余
//        AdminCommonDeductVO sentence = AdminCommonDeductVO.builder()
//                .type("1_2")
//                .maxNum(200)
//                .deductType(3).deductTypeScorePercent(2).build();
//        rules.add(sentence);
//        //分段规则
//        AdminCommonDeductVO segment = AdminCommonDeductVO.builder()
//                .type("1_3")
//                .minNum(100).maxNum(200)
//                .deductType(3).deductTypeScorePercent(2).build();
//        rules.add(segment);
//        //严重分条
//        AdminCommonDeductVO strip = AdminCommonDeductVO.builder()
//                .type("1_4")
//                .deductType(2).deductTypeScorePercent(20).build();
//        rules.add(strip);
//
//    //    System.out.print(JsonUtil.toJson(rule));
//
//    }
//    @Test
//    public void testBeanUtils(){
//        EssayQuestionBase base = new EssayQuestionBase();
//        EssayQuestionDetail detail = new EssayQuestionDetail();
//        AdminQuestionVO adminQuestionVO = new AdminQuestionVO();
////        base.setDetailId(1);
////        detail.setId(1);
////        detail.setDifficultGrade(1.23);
////        base.setId(2);
////        BeanUtils.copyProperties(detail,adminQuestionVO);
////        BeanUtils.copyProperties(base,adminQuestionVO);
//        adminQuestionVO.setScore(2);
//        adminQuestionVO.setSort(1);
//        adminQuestionVO.setStem("这是第一道试题");
//      //  System.out.print(JsonUtil.toJson(adminQuestionVO));
//    }
//}
