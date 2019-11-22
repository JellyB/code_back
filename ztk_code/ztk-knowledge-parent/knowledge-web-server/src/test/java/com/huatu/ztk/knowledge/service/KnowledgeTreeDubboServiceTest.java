package com.huatu.ztk.knowledge.service;

import com.huatu.ztk.knowledge.BaseTest;

/**
 * Created by lijianying on 6/3/16.
 */
public class KnowledgeTreeDubboServiceTest extends BaseTest {

//    @Autowired
//    private KnowledgeTreeDubboService knowledgeTreeDubboService;
//
//    @Test
//    public void incrementTest(){
//        Map<String,Integer> pointMap = Maps.newHashMap();
//        pointMap.put("1234",-1);
//        pointMap.put("2345",-1);
//        pointMap.put("3456",-1);
//        pointMap.put("4567",-1);
//        pointMap.put("56789",-1);
//        knowledgeTreeDubboService.increment(1, StatisticsType.COLLECT_COUNT,28282828L,pointMap);
//    }

    /*@Test
    public void putAllTest(){
        Map<String,Object> paramMap = Maps.newHashMap();
        List<Integer> questionIdList = Lists.newArrayList();
        for (int i = 90; i < 190; i++) {
            questionIdList.add(i);
            paramMap.put("10000"+i, JacksonUtil.toJSon(questionIdList));
        }
        long start = System.currentTimeMillis();
        knowledgeTreeDubboService.putAll(1, StatisticsType.COLLECT_LIST,28282827,paramMap);
        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }*/
//    @Test
//    public void putSetTest(){
//        List<Integer> ids = Lists.newArrayList();
//        ids.add(30725);
//        ids.add(30726);
//        ids.add(30727);
//        ids.add(30728);
//        ids.add(30729);
//        long start = System.currentTimeMillis();
//        knowledgeTreeDubboService.putQuestionIdSet(1, StatisticsType.COLLECT_LIST,282828,ids);
//        long end = System.currentTimeMillis();
//        System.out.println(end-start);
//    }
//
//    @Test
//    public void removeTest(){
//        List<Integer> ids = Lists.newArrayList();
//        ids.add(30725);
//        ids.add(30726);
//        long start = System.currentTimeMillis();
//        knowledgeTreeDubboService.delQuestionId(1, StatisticsType.COLLECT_LIST,282828,ids);
//        long end = System.currentTimeMillis();
//        System.out.println(end-start);
//    }
//
//    @Test
//    public void getStatisticsTreeTest(){
//        knowledgeTreeDubboService.getStatisticsTree(1, StatisticsType.COLLECT_LIST,282828);
//    }
}
