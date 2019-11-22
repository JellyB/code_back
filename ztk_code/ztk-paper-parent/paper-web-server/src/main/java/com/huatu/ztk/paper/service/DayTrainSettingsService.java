package com.huatu.ztk.paper.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.paper.bean.DayTrainSettings;
import com.huatu.ztk.paper.bean.KnowledgeModule;
import com.huatu.ztk.paper.dao.DayTrainSettingsDao;
import com.self.generator.core.WaitException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 每日特训 设置 服务层
 * Created by shaojieyue
 * Created time 2016-05-20 16:41
 */
@Service
public class DayTrainSettingsService {
    private static final Logger logger = LoggerFactory.getLogger(DayTrainSettingsService.class);

    //默认每日训练知识点数
    public static final int DEFAULT_TRAIN_NUMBER = 5;

//    public static final List<KnowledgeModule> points =Lists.newArrayList();
    //最大每天训练知识点数
    public static final int MAX_TRAIN_NUMBER = 10;
    public static final int DEFAULT_QUESTION_COUNT = 5;

    @Autowired
    private DayTrainSettingsDao dayTrainSettingsDao;

    @Autowired
    private ModuleDubboService moduleDubboService;

    //训练点缓存,科目-训练点（其实存储的是每一个顶级知识点的信息）
    private static final Cache<Integer,List<KnowledgeModule>> pointsCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.DAYS)//缓存时间
                    .maximumSize(100)
                    .build();

    /**
     * 根据科目id获得对应的训练点
     * 取科目下的顶级知识点（只有顶级知识点）
     * @param subject
     * @return
     */
    public List<KnowledgeModule> getPointsBySubject(int subject) {
        List<KnowledgeModule> points = pointsCache.getIfPresent(subject);

        if (CollectionUtils.isEmpty(points)) {
            List<Module> modules = moduleDubboService.findSubjectModules(subject);

            points = modules.stream()
                    .map(module -> new KnowledgeModule(module.getId(), module.getName()))
                    .collect(Collectors.toList());
            pointsCache.put(subject, points);
        }

        return points;
    }

    /**
     * 查询每日特训设置,如果不存在,则创建一个
     * @param userId 每日特训所属用户
     * @return
     */
    public DayTrainSettings findByUserId(long userId,int subject) {
        //每日特训用户的设置存储在user_day_train_settings（mongo）中
        DayTrainSettings dayTrainSettings = dayTrainSettingsDao.findByUserId(userId, subject);
        if (dayTrainSettings != null) {
            //获取顶级知识点的信息
            List<KnowledgeModule> points = getPointsBySubject(subject);
            dayTrainSettings.setPoints(points);
        }
        return dayTrainSettings;
    }

    public DayTrainSettings findById(long id){
        DayTrainSettings dayTrainSettings = dayTrainSettingsDao.findById(id);
        return dayTrainSettings;
    }

    /**
     * 更新每日训练配置
     * @param dayTrainSettings
     * @param uid
     */
    public DayTrainSettings update(DayTrainSettings dayTrainSettings, long uid,int subject) throws BizException {
        final DayTrainSettings newTrainSettings = findById(dayTrainSettings.getId());
        if (newTrainSettings == null) {//根据id没有查询到对应的设置
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        //该配置不是用户本人的,无权更改
        if (newTrainSettings.getUserId() != uid) {
            throw new BizException(CommonErrors.PERMISSION_DENIED);
        }

        List<Integer> newselects = new ArrayList<>();

        List<KnowledgeModule> points = getPointsBySubject(subject);

        for (KnowledgeModule point : points) {
            //遍历,防止有不正确的数据写进来
            if (dayTrainSettings.getSelects().contains(point.getPointId())) {
                newselects.add(point.getPointId());
            }
        }

        //设置选择的知识点
        newTrainSettings.setSelects(newselects);
        int number = dayTrainSettings.getNumber();
        if (number > MAX_TRAIN_NUMBER) {
            number = MAX_TRAIN_NUMBER;
        }

        newTrainSettings.setSubject(subject);
        newTrainSettings.setNumber(number);
        dayTrainSettingsDao.update(newTrainSettings);
        newTrainSettings.setPoints(points);
        return newTrainSettings;
    }


    /**
     * 创建每日特训（设置参数）
     * @param userId
     * @return
     */
    public DayTrainSettings create(long userId,int subject) throws WaitException {

        //默认选择所有试题点
        final ArrayList<Integer> selects = new ArrayList<>();

        List<KnowledgeModule> points = getPointsBySubject(subject);
        for (KnowledgeModule point : points) {
            selects.add(point.getPointId());
        }
        //获取唯一性id
        //long id = IdClient.getClient().nextCommonId();

        long id = Long.valueOf(String.valueOf(System.nanoTime()) + String.valueOf(System.currentTimeMillis()).substring(11));

        DayTrainSettings dayTrainSettings = DayTrainSettings.builder()
                .number(DEFAULT_TRAIN_NUMBER)
                .selects(selects)
                .userId(userId)
                .id(id)
                .createTime(new Date())
                .questionCount(DEFAULT_QUESTION_COUNT)
                .subject(subject)
                .build();

        dayTrainSettingsDao.insert(dayTrainSettings);
        //设置知识点
        dayTrainSettings.setPoints(points);
        return dayTrainSettings;
    }
}
