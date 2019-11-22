package com.huatu.ztk.report.service;

import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.PointSummaryDubboService;
import com.huatu.ztk.knowledge.bean.PointSummary;
import com.huatu.ztk.report.bean.QuestionSummary;
import com.huatu.ztk.report.common.RedisReportKeys;
import com.huatu.ztk.report.dao.QuestionSummaryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 试题总结服务层
 * Created by shaojieyue
 * Created time 2016-05-28 21:23
 */

@Service
public class QuestionSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionSummaryService.class);

    @Autowired
    private QuestionSummaryDao questionSummaryDao;

    @Autowired
    private PointSummaryDubboService pointSummaryDubboService;

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private ModuleDubboService moduleDubboService;

    /**
     * 通过用户和考试科目查询统计信息
     * @param uid 用户id
     * @param subject 科目id
     * @return
     */
    public QuestionSummary findByUserId(long uid, int subject){
        long t1 = System.currentTimeMillis();
        QuestionSummary questionSummary = questionSummaryDao.findByUserId(uid, subject);
        logger.info("find findByUserId expendTime={}", System.currentTimeMillis() - t1);
        if (questionSummary == null) {//不存在,则初始化
            questionSummary = initQuestionSummary(uid, subject);
        }
        return questionSummary;
    }

    /**
     * 更新统计,加锁保证导入数据时,出现更新数据被覆盖的情况
     * @param uid 用户id
     * @param subject 科目id
     * @param area
     */
    public synchronized QuestionSummary update(long uid, int subject, int wsum, int rsum, int times, int area){
        QuestionSummary questionSummary = findByUserId(uid, subject);
        if (questionSummary == null) {//不存在,则初始化
            questionSummary = initQuestionSummary(uid, subject);
        }

        int asum = wsum+rsum;
        if (asum == 0) {//总题数为0,不做处理
            logger.warn("asum=0,do not proccess update");
            return questionSummary;
        }

        questionSummary.setRsum(questionSummary.getRsum()+rsum);
        questionSummary.setWsum(questionSummary.getWsum()+wsum);
        questionSummary.setAsum(questionSummary.getRsum()+questionSummary.getWsum());
        questionSummary.setTimes(questionSummary.getTimes() + times);

        int rcount = 0;
        int wcount = 0;
        //遍历科目下所有模块
        for (Module module : moduleDubboService.findSubjectModules(subject)) {
            //查询用户知识点汇总信息（知识点的一级节点就是模块）
            final PointSummary pointSummary = pointSummaryDubboService.find(uid, subject, module.getId());
            rcount = rcount + pointSummary.getRcount();//错误数累加
            wcount = wcount + pointSummary.getWcount();//正确数累加
        }

        questionSummary.setWcount(wcount);
        questionSummary.setRcount(rcount);
        questionSummary.setAcount(questionSummary.getRcount()+questionSummary.getWcount());

        //重新计算正确率,保留一位小数
        double accuracy = questionSummary.getRsum() * 100/questionSummary.getAsum();
        questionSummary.setAccuracy(accuracy);
        questionSummary.setSpeed(questionSummary.getTimes()/questionSummary.getAsum());
        //更新数据
        questionSummaryDao.update(questionSummary);

        if (questionSummary.getAcount() >0) {//设置用户平均分,用作用户区域排名
            //计算用户平均分
            int avgScore = 100*questionSummary.getRcount()/questionSummary.getAcount();
            final String userScoreZsetKey = RedisReportKeys.getUserScoreZsetKey(area, subject);
            //设置平均分
            redisTemplate.opsForZSet().add(userScoreZsetKey,uid+"",avgScore);
        }

        return questionSummary;
    }

    /**
     * 初始化试题整体统计记录
     * @param uid
     * @param subject
     * @return
     */
    private QuestionSummary initQuestionSummary(long uid, int subject) {
        QuestionSummary questionSummary;
        questionSummary = QuestionSummary.builder().uid(uid).subject(subject).build();
        try {
            //插入数据
            long t1 = System.currentTimeMillis();
            questionSummaryDao.insert(questionSummary);
            logger.info("questionSummaryDao insert expendTime={}", System.currentTimeMillis() - t1);
        }catch (Exception e){
            logger.warn("init question summary fail.",e);
            questionSummary = findByUserId(uid, subject);
        }
        return questionSummary;
    }


}
