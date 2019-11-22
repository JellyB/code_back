package com.huatu.ztk.paper.service;

import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.paper.bean.PracticePointsSummary;
import com.huatu.ztk.paper.dao.PracticePointsSummaryDao;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-07-26 10:35
 */
public class PracticePointsSummaryDaoTest extends BaseTest{
    private static final Logger logger = LoggerFactory.getLogger(PracticePointsSummaryDaoTest.class);

    @Autowired
    private PracticePointsSummaryDao practicePointsSummaryDao;


    @Test
    public void testInsert(){
        String dd = "[ \n" +
                "            {\n" +
                "                \"_id\" : 707,\n" +
                "                \"name\" : \"定义判断\",\n" +
                "                \"parent\" : 642,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"wnum\" : 0,\n" +
                "                \"unum\" : 0,\n" +
                "                \"times\" : 2,\n" +
                "                \"speed\" : 2,\n" +
                "                \"level\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"children\" : [ \n" +
                "                    {\n" +
                "                        \"_id\" : 711,\n" +
                "                        \"name\" : \"管理学类\",\n" +
                "                        \"parent\" : 707,\n" +
                "                        \"qnum\" : 1,\n" +
                "                        \"rnum\" : 1,\n" +
                "                        \"wnum\" : 0,\n" +
                "                        \"unum\" : 0,\n" +
                "                        \"times\" : 2,\n" +
                "                        \"speed\" : 2,\n" +
                "                        \"level\" : 2,\n" +
                "                        \"accuracy\" : 100,\n" +
                "                        \"children\" : []\n" +
                "                    }\n" +
                "                ]\n" +
                "            }, \n" +
                "            {\n" +
                "                \"_id\" : 715,\n" +
                "                \"name\" : \"逻辑判断\",\n" +
                "                \"parent\" : 642,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"rnum\" : 2,\n" +
                "                \"wnum\" : 0,\n" +
                "                \"unum\" : 0,\n" +
                "                \"times\" : 6,\n" +
                "                \"speed\" : 3,\n" +
                "                \"level\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"children\" : [ \n" +
                "                    {\n" +
                "                        \"_id\" : 738,\n" +
                "                        \"name\" : \"评价类\",\n" +
                "                        \"parent\" : 715,\n" +
                "                        \"qnum\" : 2,\n" +
                "                        \"rnum\" : 2,\n" +
                "                        \"wnum\" : 0,\n" +
                "                        \"unum\" : 0,\n" +
                "                        \"times\" : 6,\n" +
                "                        \"speed\" : 3,\n" +
                "                        \"level\" : 2,\n" +
                "                        \"accuracy\" : 100,\n" +
                "                        \"children\" : []\n" +
                "                    }\n" +
                "                ]\n" +
                "            },\n" +
                "            {\n" +
                "                \"_id\" : 707,\n" +
                "                \"name\" : \"定义判断\",\n" +
                "                \"parent\" : 642,\n" +
                "                \"qnum\" : 1,\n" +
                "                \"rnum\" : 1,\n" +
                "                \"wnum\" : 0,\n" +
                "                \"unum\" : 0,\n" +
                "                \"times\" : 2,\n" +
                "                \"speed\" : 2,\n" +
                "                \"level\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"children\" : [ \n" +
                "                    {\n" +
                "                        \"_id\" : 711,\n" +
                "                        \"name\" : \"管理学类\",\n" +
                "                        \"parent\" : 707,\n" +
                "                        \"qnum\" : 1,\n" +
                "                        \"rnum\" : 1,\n" +
                "                        \"wnum\" : 0,\n" +
                "                        \"unum\" : 0,\n" +
                "                        \"times\" : 2,\n" +
                "                        \"speed\" : 2,\n" +
                "                        \"level\" : 2,\n" +
                "                        \"accuracy\" : 100,\n" +
                "                        \"children\" : []\n" +
                "                    }\n" +
                "                ]\n" +
                "            }, \n" +
                "            {\n" +
                "                \"_id\" : 715,\n" +
                "                \"name\" : \"逻辑判断\",\n" +
                "                \"parent\" : 642,\n" +
                "                \"qnum\" : 2,\n" +
                "                \"rnum\" : 2,\n" +
                "                \"wnum\" : 0,\n" +
                "                \"unum\" : 0,\n" +
                "                \"times\" : 6,\n" +
                "                \"speed\" : 3,\n" +
                "                \"level\" : 1,\n" +
                "                \"accuracy\" : 100,\n" +
                "                \"children\" : [ \n" +
                "                    {\n" +
                "                        \"_id\" : 738,\n" +
                "                        \"name\" : \"评价类\",\n" +
                "                        \"parent\" : 715,\n" +
                "                        \"qnum\" : 2,\n" +
                "                        \"rnum\" : 2,\n" +
                "                        \"wnum\" : 0,\n" +
                "                        \"unum\" : 0,\n" +
                "                        \"times\" : 6,\n" +
                "                        \"speed\" : 3,\n" +
                "                        \"level\" : 2,\n" +
                "                        \"accuracy\" : 100,\n" +
                "                        \"children\" : []\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ]";
        final List<QuestionPointTree> questionPointTrees = JsonUtil.toList(dd, QuestionPointTree.class);
        long id = RandomUtils.nextInt(1000000,2000000);
        PracticePointsSummary practicePointsSummary = new PracticePointsSummary();
        practicePointsSummary.setPoints(questionPointTrees);
        practicePointsSummary.setPracticeId(id);
        practicePointsSummaryDao.insert(practicePointsSummary);
        final PracticePointsSummary practice = practicePointsSummaryDao.findByPracticeId(id);
        Assert.assertEquals(practice.getPracticeId(),id);
        Assert.assertEquals(questionPointTrees.size(),practice.getPoints().size());
    }
}
