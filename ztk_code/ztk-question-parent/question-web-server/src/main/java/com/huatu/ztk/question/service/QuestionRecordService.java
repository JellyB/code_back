package com.huatu.ztk.question.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.UserAnswers;
import com.huatu.ztk.question.bean.QuestionRecord;
import com.huatu.ztk.question.dao.QuestionRecordDao;
import com.mongodb.DuplicateKeyException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户，试题答题记录service
 * 用户每道题，作答次数，耗时等
 * Created by shaojieyue
 * Created time 2016-09-08 11:39
 */

@Service
public class QuestionRecordService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionRecordService.class);

    @Autowired
    private QuestionRecordDao questionRecordDao;

    /**
     * 查询用户答题记录
     * @param uid
     * @param questionIds
     * @return
     */
    public List<QuestionRecord> findBatch(long uid, int[] questionIds) {
        List<QuestionRecord> list = questionRecordDao.findBath(uid,questionIds);


        //只取最后一个答题记录
        for (QuestionRecord record : list) {
            List<Integer> answers = record.getAnswers();
            List<Integer> times = record.getTimes();

            if (CollectionUtils.isNotEmpty(answers) && CollectionUtils.isNotEmpty(times)) {
                record.setAnswers(Lists.newArrayList(answers.get(answers.size() - 1)));
                record.setTimes(Lists.newArrayList(times.get(times.size() - 1)));
            }
        }

        //转为map key:qid
        final Map<Integer, QuestionRecord> data = list.stream().collect(Collectors.toMap(p -> p.getQid(), p -> p));
        //对列表进行排序,保证返回的结果和questionIds里面的id一致
        final List<QuestionRecord> results = Arrays.stream(questionIds).mapToObj(qid -> data.get(qid)).collect(Collectors.toList());
        return results;
    }

    public void updateQuestionRecord(UserAnswers userAnswers){
        long uid = userAnswers.getUid();
        List<QuestionRecord> questionRecords = new ArrayList<>(userAnswers.getAnswers().size());

        for (Answer answer : userAnswers.getAnswers()) {
            //主观题的答案是string,暂时不处理
            if (!StringUtils.isNumeric(answer.getAnswer())
                    || Integer.valueOf(answer.getAnswer()) < 1) {//非法的答案
                continue;
            }

            final boolean update = questionRecordDao.update(uid, answer.getQuestionId(), Integer.valueOf(answer.getAnswer()), answer.getTime());
            if (!update) {//没有说明记录不存在,需要进行插入操作
                final QuestionRecord questionRecord = QuestionRecord.builder()
                        .answers(Lists.newArrayList(Integer.valueOf(answer.getAnswer())))
                        .qid(answer.getQuestionId())
                        .uid(uid)
                        .times(Lists.newArrayList(answer.getTime()))
                        .build();
                questionRecords.add(questionRecord);
            }
        }

        try {
            //批量插入数据
            questionRecordDao.insert(questionRecords);
        }catch (DuplicateKeyException e){//该异常不用处理
        }
    }
}
