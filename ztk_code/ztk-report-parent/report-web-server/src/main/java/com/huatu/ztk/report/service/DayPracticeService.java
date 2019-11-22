package com.huatu.ztk.report.service;

import com.google.common.collect.TreeBasedTable;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.chart.util.ChartUtils;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.report.bean.DayPractice;
import com.huatu.ztk.report.common.ForecastSocreLineConfig;
import com.huatu.ztk.report.common.RedisReportKeys;
import com.huatu.ztk.report.dao.DayPracticeDao;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-05-30 18:31
 */

@Service
public class DayPracticeService {
    private static final Logger logger = LoggerFactory.getLogger(DayPracticeService.class);

    /**
     * 平均分用户id
     */
    public static final int AVERAGE_USER_ID=-1;

    @Autowired
    private DayPracticeDao dayPracticeDao;

    @Autowired
    private RedisTemplate redisTemplate;

    public List<DayPractice> findAll(long uid, int subject){
        final List<DayPractice> dayPractices = dayPracticeDao.findAll(uid, subject);
        return dayPractices;
    }

    public DayPractice findById(String id){
        final DayPractice dayPractice = dayPracticeDao.findById(id);
        return dayPractice;
    }


    /**
     * 初始化当天的试卷练习情况
     * @param uid
     * @param subject
     * @param createTime
     */
    private void initCurrentDayPractice(long uid, int subject, long createTime) {
        final String date = getDate(createTime);
        final String id = getId(uid, subject, date);
        final DayPractice dayPractice = DayPractice.builder().count(0)
                .id(id)
                .practices(new HashSet())
                .date(date)
                .subject(subject)
                .uid(uid).build();
        dayPracticeDao.insert(dayPractice);

        final ListOperations listOperations = redisTemplate.opsForList();
        String listKey = RedisReportKeys.getUserDayPracticesIdListKey(uid, subject);

        //插入到队首
        listOperations.leftPush(listKey, id);
        //count默认为5，表示只保留列表 list 的前5个元素，其余元素删除
        listOperations.trim(listKey, 0, ForecastSocreLineConfig.SCORE_COUNT - 1);
    }

    /**
     * 获取日练习统计时间格式
     * @param createTime
     * @return
     */
    private String getDate(long createTime) {
        return DateFormatUtils.format(createTime, "yyyyMMdd");
    }

    /**
     * 添加练习记录
     * @param answerCard
     */
    public void addPractice(AnswerCard answerCard) {
        final long uid = answerCard.getUserId();
        final int subject = answerCard.getSubject();
        final long practiceId = answerCard.getId();
        final String id = getId(uid, subject, getDate(answerCard.getCreateTime()));
        DayPractice dayPractice = dayPracticeDao.findById(id);
        if (dayPractice == null) {//没有则先初始化当天的记录
            initCurrentDayPractice(uid, subject, answerCard.getCreateTime());
            dayPractice = dayPracticeDao.findById(id);
        }

        if (dayPractice.getPractices().contains(practiceId)) {
            logger.warn("repeat receive command proccess practiceId={}",practiceId);
            //如果已经包含id,说明已经处理过,则不进行处理
            return;
        }

        commonUpdate(answerCard, dayPractice);

        //添加练习的id
        dayPractice.getPractices().add(practiceId);
        //设置数量
        dayPractice.setCount(dayPractice.getPractices().size());
        dayPracticeDao.update(dayPractice);
        addAverageDayPractice(answerCard);
    }

    private void commonUpdate(AnswerCard answerCard, DayPractice dayPractice) {
        final int rcount = answerCard.getRcount();
        final int wcount = answerCard.getWcount();
        final int acount = rcount + wcount;
        final double difficulty = answerCard.getDifficulty();
        final double allDiff = dayPractice.getQuestionAllCount() * dayPractice.getDifficulty() + acount * difficulty;
        //重新计算难度
        final int newAllCount = dayPractice.getQuestionAllCount() + acount;
        if (newAllCount == 0) {//总数量为0，则进行处理
            return;
        }
        final double newDifficulty = new BigDecimal(allDiff).divide(new BigDecimal(newAllCount),1,BigDecimal.ROUND_HALF_UP).doubleValue();
        //难度
        dayPractice.setDifficulty(newDifficulty);
        //正确题数
        dayPractice.setQuestionRightCount(rcount+dayPractice.getQuestionRightCount());
        //错题数
        dayPractice.setQuestionWrongCount(wcount+dayPractice.getQuestionWrongCount());
        //所有题数
        dayPractice.setQuestionAllCount(newAllCount);
        //评估分数
        final int score = dayPractice.getQuestionRightCount() * 100 / dayPractice.getQuestionAllCount();
        dayPractice.setScore(score);
    }


    /**
     * 更新每日统计平均值
     * @param answerCard
     */
    private void addAverageDayPractice(AnswerCard answerCard){

        long uid = AVERAGE_USER_ID;
        int subject = answerCard.getSubject();
        final String id = getId(uid, subject, getDate(answerCard.getCreateTime()));
        DayPractice dayPractice = dayPracticeDao.findById(id);
        if (dayPractice == null) {//没有则先初始化当天的记录
            initCurrentDayPractice(uid, subject,answerCard.getCreateTime());
            dayPractice = dayPracticeDao.findById(id);
        }
        commonUpdate(answerCard,dayPractice);
        //更新计算结果
        dayPracticeDao.update(dayPractice);
    }

    public List<DayPractice> find(long userId, int subject) {
        final ListOperations listOperations = redisTemplate.opsForList();
        String listKey = RedisReportKeys.getUserDayPracticesIdListKey(userId,subject);
        List<String> pids = listOperations.range(listKey, 0, -1);
        return dayPracticeDao.findBath(pids);
    }

    /**
     * 查询用户预测分曲线
     * @param userId 用户id
     * @param subject 科目
     * @return
     */
    public Line queryForecastChart(long userId, int subject) {
        final TreeBasedTable<String, String, Number> basedTable = TreeBasedTable.create();
        final List<DayPractice> dayPractices = find(userId,subject);

        //个人每天预测
        for (DayPractice dayPractice : dayPractices) {
            final int score = dayPractice.getScore();
            final String date = dayPractice.getDate();
            final String time = date.substring(4, date.length());
            //设置个人曲线图
            basedTable.put(time,"我", score);

            //设置平均值
            final String id = getId(AVERAGE_USER_ID, subject, date);
            final DayPractice averageDayPractice = findById(id);
            basedTable.put(time,"平均",averageDayPractice.getScore());
        }

        final Line line = ChartUtils.table2LineSeries(basedTable);
        return line;
    }

    /**
     * 根据uid和科目类型来生成DayPractice id
     * @param uid
     * @param subject
     * @return
     */
    public static final String getId(long uid,int subject,String date){
        final String id = uid+"_" + subject+"_"+date;
        return id;
    }
}
