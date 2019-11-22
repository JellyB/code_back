package com;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.BaseTestW;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/9/10.
 */
public class QuestionCountT extends BaseTestW {
    private static final Logger logger = LoggerFactory.getLogger(QuestionCountT.class);
    @Autowired
    QuestionDao questionDao;


    @Test
    public void test(){
        StringBuilder stringBuilder = new StringBuilder("");

//        List<Integer> subject = Lists.newArrayList(1);//行测
        List<Integer> subject = Lists.newArrayList(2,3,24);//事业单位
//        List<Integer> subject = Lists.newArrayList(100100263);//国家电网
//        List<Integer> subject = Lists.newArrayList(100100173,100100174,100100175);//招警
        stringBuilder.append("处理的科目ID:").append(StringUtils.join(subject.stream().map(String::valueOf).collect(Collectors.toList()), ",")).append("\n");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("ztk_question");
        List<String> questions = questionDao.findAllQuestion(subject, "ztk_question");
        logger.info("questions.size={}",questions.size());
        stringBuilder.append("原有试题数量：").append(questions.size()).append("\n");
        stopWatch.stop();
        stopWatch.start("ztk_question_new");
        List<String> newQuestions = questionDao.findAllQuestion(subject, "ztk_question_new");
        logger.info("newQuestions.size={}",newQuestions.size());
        stringBuilder.append("新表试题数量：").append(newQuestions.size()).append("\n");
        stopWatch.stop();
        stopWatch.start("intersection");
        Collection intersection = CollectionUtils.intersection(questions, newQuestions);
        logger.info("intersection.size={}",intersection.size());
        stringBuilder.append("交集试题数量：").append(intersection.size()).append("\n");
        stopWatch.stop();
        stopWatch.start("ztk_question_diff");
        questions = questions.parallelStream().filter(i -> !intersection.contains(i)).collect(Collectors.toList());
        logger.info("ztk_question_diff.size={}",questions.size());
        stringBuilder.append("原有试题特有的数量：").append(questions.size()).append("\n");
        stopWatch.stop();
        stopWatch.start("ztk_question_new_diff");
//        newQuestions.removeAll(intersection);
        newQuestions = newQuestions.parallelStream().filter(i -> !intersection.contains(i)).collect(Collectors.toList());
//        questionDao.groupBy(newQuestions,"ztk_question_new");
        logger.info("ztk_question_diff.size={}",newQuestions.size());
        stringBuilder.append("现有试题特有的数量：").append(newQuestions.size()).append("\n");
        stopWatch.stop();
        stopWatch.start("分析试题组成");
        questionDao.group(questions,"ztk_question",stringBuilder);
        stopWatch.stop();
        stopWatch.start("分析new试题组成");
        questionDao.group(newQuestions,"ztk_question_new",stringBuilder);
        stopWatch.stop();
        logger.info(stopWatch.prettyPrint());
        System.out.println("结果\n"+stringBuilder.toString());
    }
}
