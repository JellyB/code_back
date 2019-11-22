package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperSummary;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import com.huatu.ztk.paper.dao.AnswerCardDao;
import com.huatu.ztk.paper.dao.PaperDao;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by shaojieyue
 * Created time 2016-07-02 15:28
 */
public class PaperServiceTest extends BaseTest {

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperDao paperDao;

    @Autowired
    AnswerCardDao answerCardDao;

    final int uid = 12252065;
    final long startTime = 1524240000000L;
    final long endTime = 1524448800000L;

    final String ANSWER_CARD_SUBMIT_TIME_SET = "answer_card_submit_time_set_%s";
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void testGet(){
        Paper paper = paperService.findById(123123123);
        System.out.println(JsonUtil.toJson(paper));
    }

    @Test
    public void summaryTest(){
        ArrayList<Integer> subjects = Lists.newArrayList(1);
        final List<PaperSummary> summary = paperService.summary(subjects, 586, 1);
        System.out.println(summary);
        Assert.assertEquals(summary.get(1).getArea(), AreaConstants.QUAN_GUO_ID);
        Assert.assertEquals(summary.get(0).getArea(), 586);

        Assert.assertEquals(0, paperService.summary(subjects, 586, 1).size());
        Assert.assertEquals(0,paperService.summary(subjects, 586, 2).size());
    }

    @Test
    public void delTest() {
        Paper p = paperDao.findById(2004464);
        Assert.assertNotNull(p);
    }

    @Test
    public void getEstimateDate(){
        File file  = new File("/app/logs/estimateInfo.log");
        try {
            System.setErr(new PrintStream(new FileOutputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        List<EstimatePaper> estimatePapers = paperDao.findEstimatePaperByType(1, 8);
        List<EstimatePaper> list = estimatePapers.stream().filter(i -> i.getStartTime() >= startTime)
                .filter(i -> i.getStartTime() <= endTime)
                .collect(Collectors.toList());
        int total = 0;
        System.err.println("数据统计日期:"+LocalDateTime.now());
        System.err.println("统计日期范围："+new Date(startTime)+"到"+new Date(endTime));
        for (int i = 0; i < list.size(); i++) {
            EstimatePaper estimatePaper = list.get(i);
            String areaNames = AreaConstants.getArea(estimatePaper.getArea()).getName();
            Integer paperId = estimatePaper.getId();
            Integer count = getParticipants(estimatePaper);
            total += count;
            System.err.println("试卷id:" + paperId + ",地区:"+areaNames+",参加人数:"+count+",试卷名称："+estimatePaper.getName());

        }
        System.err.println("共"+list.size()+"场估分,参加人数:"+total);
        System.err.println("参加人数："+total);
    }

    private Integer getParticipants(EstimatePaper estimatePaper) {
        boolean flag = System.currentTimeMillis() < endTime;
        String paperPracticeIdSoreKey = PaperRedisKeys.getPaperPracticeIdSore(estimatePaper.getId());
        if(flag){
            Long size = redisTemplate.opsForZSet().size(paperPracticeIdSoreKey);
            return size.intValue();
        }
        String temp = getPaperPracticeIdSubmitKey(estimatePaper.getId());
        Long size = redisTemplate.opsForZSet().size(temp);
        if(null!= size&&size>0){
            Set set = redisTemplate.opsForZSet().rangeByScore(temp, startTime, endTime);
            if(CollectionUtils.isEmpty(set)){
                return 0;
            }
            return set.size();
        }
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        try{
            Set<byte[]> bytes = connection.zRange(paperPracticeIdSoreKey.getBytes(), 0, -1);
            List<Long> ids = bytes.stream().map(String::new).map(Long::parseLong).collect(Collectors.toList());
            return answerCardDao.countByIdsAndDate(ids,startTime, endTime,redisTemplate,temp);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.close();
        }

        return 0;

    }

    private String getPaperPracticeIdSubmitKey(int paperId) {
        return String.format(ANSWER_CARD_SUBMIT_TIME_SET,paperId);
    }

    @Test
    public void test(){
        Object paperIdsGroupBySubjectAndType = paperService.getPaperIdsGroupBySubjectAndType();
        System.out.println(JsonUtil.toJson(paperIdsGroupBySubjectAndType));
    }

}
