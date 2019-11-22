package com.huatu.ztk.service;

import com.google.common.collect.Interner;
import com.google.common.collect.Lists;
import com.huatu.ztk.BaseTest;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.UserAnswers;
import com.huatu.ztk.question.bean.QuestionRecord;
import com.huatu.ztk.question.service.QuestionRecordService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by shaojieyue
 * Created time 2016-09-09 14:24
 */
public class QuestionRecordServiceTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(QuestionRecordServiceTest.class);

    @Autowired
    private QuestionRecordService questionRecordService;

    @Test
    public void testFind(){
        List<Answer> answerList = new ArrayList();
        Random random = new Random();
        List<Integer> qids = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            final Answer answer = new Answer();
            answer.setQuestionId(i+40000);
            answer.setAnswer("1");
            answer.setCorrect(1);
            answer.setTime(random.nextInt(50)+10);
            answerList.add(answer);
            qids.add(answer.getQuestionId());
        }
        System.out.println(StringUtils.join(qids,","));

        final int uid = 13061689;
        final UserAnswers userAnswers = UserAnswers.builder()
                .uid(uid)
                .area(9)
                .subject(1)
                .answers(answerList)
                .submitTime(System.currentTimeMillis())
                .build();

        final int[] as = qids.stream().mapToInt(qid -> qid.intValue()).toArray();
        List<QuestionRecord> batch = questionRecordService.findBatch(uid, as);
        System.out.println(JsonUtil.toJson(batch));
        Assert.assertEquals(batch.stream().filter(dd->dd!=null).count(),100);
        questionRecordService.updateQuestionRecord(userAnswers);
        batch = questionRecordService.findBatch(uid, as);
        Assert.assertEquals(batch.stream().filter(dd->dd!=null).count(),100);

        questionRecordService.updateQuestionRecord(userAnswers);
        batch = questionRecordService.findBatch(uid, as);
        Assert.assertEquals(batch.stream().filter(dd->dd!=null).count(),100);
    }
}
