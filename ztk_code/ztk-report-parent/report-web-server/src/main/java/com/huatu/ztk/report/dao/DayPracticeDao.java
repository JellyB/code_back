package com.huatu.ztk.report.dao;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.report.bean.DayPractice;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by shaojieyue
 * Created time 2016-05-30 18:19
 */

@Repository
public class DayPracticeDao {
    private static final Logger logger = LoggerFactory.getLogger(DayPracticeDao.class);


    @Autowired
    private MongoTemplate mongoTemplate;

    public List<DayPractice> findAll(long uid,int subject){
        final Criteria criteria = Criteria.where("uid").is(uid).and("subject").is(subject);
        final Query query = Query.query(criteria);
        List<DayPractice> dayPractices = mongoTemplate.find(query, DayPractice.class);
        if (dayPractices == null) {
            dayPractices = new ArrayList<DayPractice>();
        }

        return dayPractices;
    }

    /**
     * 查询用户指定数量的天数统计
     * @param uid
     * @param subject
     * @param count 去记录数
     * @return
     */
    public List<DayPractice> find(long uid,int subject,int count){
        final Criteria criteria = Criteria.where("uid").is(uid).and("subject").is(subject);
        //id做降序排序
        final Query query = Query.query(criteria)
                .with(new Sort(Sort.Direction.DESC,"id"))//id做降序
                .limit(count);
        List<DayPractice> dayPractices = mongoTemplate.find(query, DayPractice.class);
        if (dayPractices == null) {
            dayPractices = new ArrayList<DayPractice>();
        }
        dayPractices = Lists.reverse(dayPractices);
        return dayPractices;
    }

    /**
     * 根据id查询每日训练统计
     * @param id
     * @return
     */
    public DayPractice findById(String id){
        return mongoTemplate.findById(id,DayPractice.class);
    }

    /**
     * 插入数据
     * @param dayPractice
     */
    public void insert(DayPractice dayPractice){
        mongoTemplate.insert(dayPractice);
    }

    /**
     * 更新数据
     * @param dayPractice
     */
    public void update(DayPractice dayPractice){
        logger.info("update DayPractice,data={}", JsonUtil.toJson(dayPractice));
        mongoTemplate.save(dayPractice);
    }


    /**
     *批量获得每日练习数据
     * @param pids 每日特训id
     * @return
     */
    public List<DayPractice> findBath(List<String> pids) {
        List<DayPractice> ret = new ArrayList<>();

        for (String pid : pids) {
            DayPractice dayPractice = mongoTemplate.findById(pid, DayPractice.class);
            if (dayPractice != null) {
                ret.add(dayPractice);
            }
        }

        return ret;
    }


    /**
     * 查询全部的练习天数
     * @param uid
     * @param subject
     * @return
     */
    public int queryTotalPracticeDayCount(long uid,int subject) {
        //直接计算每日练习统计的数量
        Criteria criteria = Criteria.where("uid").is(uid).and("subject").is(subject);
        Long totalCount = mongoTemplate.count(new Query(criteria), DayPractice.class);
        return totalCount.intValue();
    }

    /**
     * 查询当月的练习天数
     * @param uid
     * @param subject
     * @return
     */
    public int queryMonthPracticeDayCount(long uid,int subject) {
        //年月
        String yearMonthString = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMM");

        //当月第一天
        String firstDay = yearMonthString + "01";
        //当月最后一天
        String lastDay = yearMonthString + "31";

        //第一天的练习统计id
        String firstDayPracticeId = getDayPracticeId(uid, subject, firstDay);
        //最后一天的练习统计id
        String lastDayPracticeId = getDayPracticeId(uid, subject, lastDay);

        //通过比较每天练习id，统计当月每日练习的数量，也可以通过uid比较date字段，性能接近
        Criteria criteria = new Criteria();
        criteria = criteria.andOperator(Criteria.where("_id").gte(firstDayPracticeId), Criteria.where("_id").lte(lastDayPracticeId));
        Long monthCount = mongoTemplate.count(new Query(criteria), DayPractice.class);

        //查询当天练习统计是否存在
        String currentDay = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd");
        final DayPractice todayPractice = findById(getDayPracticeId(uid, subject, currentDay));

        //不存在，说明当天练习统计未插入，计数+１
        int result = todayPractice == null ? monthCount.intValue() + 1 :monthCount.intValue();
        return result;
    }

    /**
     * 获得每日练习统计的id
     * @param uid
     * @param subject
     * @param date ×月x日
     * @return
     */
    private String getDayPracticeId(long uid, int subject,String date) {
        return uid + "_" + subject + "_" + date;
    }
}
