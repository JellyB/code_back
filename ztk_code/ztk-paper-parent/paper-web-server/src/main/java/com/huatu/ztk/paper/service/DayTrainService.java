package com.huatu.ztk.paper.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.paper.bean.DayTrain;
import com.huatu.ztk.paper.bean.DayTrainSettings;
import com.huatu.ztk.paper.bean.KnowledgeModule;
import com.huatu.ztk.paper.bean.TrainPoint;
import com.huatu.ztk.paper.common.TrainPointStatus;
import com.huatu.ztk.paper.dao.DayTrainDao;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static com.huatu.ztk.paper.common.PracticeErrors.DAY_TRAIN_SETTING_NO_INIT;


/**
 * 每日特训服务层
 * Created by shaojieyue
 * Created time 2016-05-20 22:07
 */

@Service
public class DayTrainService {
    private static final Logger logger = LoggerFactory.getLogger(DayTrainService.class);

    /**
     * 每日特训知识点个数
     */
    public static final int DAY_TRAIN_POINT_COUNT = 5;

    /**
     * 每日特训试题个数
     */
    public static final int DAY_TRAIN_QUESTION_COUNT = 5;

    @Autowired
    private DayTrainDao dayTrainDao;

    @Autowired
    private QuestionPointDubboService questionPointDubboService;

    @Autowired
    private DayTrainSettingsService dayTrainSettingsService;

    /**
     * 查找用户当天的DayTrain
     *
     * @param userId
     * @return
     */
    public DayTrain findCurrent(long userId, int subject) throws BizException {
        //特训记录保存在day_train（mongo中）
        DayTrain dayTrain = dayTrainDao.findById(getId(userId, subject));
        //如果没有查到当天的特训记录，则创建特训
        if (dayTrain == null) {//没有则创建新的
            dayTrain = create(userId, subject);
        }
        return dayTrain;
    }

    /**
     * 创建每日特训对象
     * 抽题策略是从每个顶级知识点下获取相同个数的3级知识点，打乱顺序然后抽出一定数量的3级知识点，进而获取试题
     * 防止抽题时总是只从一个顶级知识点下抽题
     *
     * @param userId 用户id
     * @return
     */
    public DayTrain create(long userId, int subject) throws BizException {
        final String trainId = getId(userId, subject);
        DayTrain dayTrain = dayTrainDao.findById(trainId);
        if (dayTrain != null) {//如果查询出来,则直接返回
            logger.warn("repeat create day train. userId = {}", userId);
            return dayTrain;
        }

        //用户每日特训设置信息
        final DayTrainSettings dayTrainSettings = dayTrainSettingsService.findByUserId(userId, subject);

        //没有则提示用户初始化每日特训设置
        //TODO 由于目前已经关闭每日特训的设置入口，所以如果没有设置信息，则学员将不能做每日特训的信息
        if (dayTrainSettings == null || CollectionUtils.isEmpty(dayTrainSettings.getSelects())) {
            throw new BizException(DAY_TRAIN_SETTING_NO_INIT);
        }

        //获取用户设置的涉及知识点
        final List<Integer> selects = dayTrainSettings.getSelects();

        List<QuestionPoint> questionPoints = new ArrayList<>();
        //获取用户设置的练习次数，同样是出题的个数（出题个数25）
        final int settingNumber = dayTrainSettings.getNumber();
        //计算每个顶级知识点下应该抽取的3级知识点的个数（5个知识点，每个知识点选6个底层知识点参与抽题）
        int avgNumber = (settingNumber / selects.size()) + 1;
        for (Integer pointId : selects) {
            //根据一级知识点,随机获取指定个数的3级知识点（如果有不超过5道题的试题也是存在的）
            questionPoints.addAll(questionPointDubboService.randomPoint(pointId, avgNumber));
        }

        //重新打乱排序
        Collections.shuffle(questionPoints);
        List<QuestionPoint> finalPoints;
        int size = questionPoints.size();
        if(size==0){
            finalPoints = Lists.newArrayListWithCapacity(1);
        }else if (size!=0 && size < settingNumber) {
            finalPoints = questionPoints.subList(0, questionPoints.size());
        } else {
            finalPoints = questionPoints.subList(0, settingNumber);
        }
        //取出用户指定个数的3级知识点
        List<TrainPoint> trainPoints = new ArrayList<>();
        //遍历创建今日特训知识点列表
        for (QuestionPoint questionPoint : finalPoints) {
            final TrainPoint trainPoint = TrainPoint.builder()
                    .name(questionPoint.getName())
                    .status(TrainPointStatus.UNDO)
                    .questionPointId(questionPoint.getId()).build();
            trainPoints.add(trainPoint);
        }


        dayTrain = DayTrain.builder().createTime(new Date())
                .id(trainId)
                .allCount(trainPoints.size())
                .points(trainPoints)
                .questionCount(dayTrainSettings.getQuestionCount()).build();
        dayTrainDao.insert(dayTrain);
        return dayTrain;
    }

    /**
     * 添加用户每日训练练习
     *
     * @param userId          用户id
     * @param questionPointId 知识点id
     * @param practiceId      练习id
     * @return
     */
    public DayTrain addTrainPractice(long userId, int questionPointId, long practiceId, int subject) throws BizException {
        final DayTrain dayTrain = findCurrent(userId, subject);
        if (dayTrain == null) {
            logger.error("can`t found user day train.userId = {}, questionPointId = {}, practiceId = {}", userId, questionPointId, practiceId);
            return null;
        }

        int finishCount = 0;
        //遍历考点,找到对应的考试点,设置对应的练习id
        for (TrainPoint trainPoint : dayTrain.getPoints()) {
            if (trainPoint.getQuestionPointId() == questionPointId) {
                trainPoint.setPracticeId(practiceId);
                trainPoint.setStatus(TrainPointStatus.FINISH);//表示已经做完
            }

            //计算已完成个数
            if (trainPoint.getStatus() == TrainPointStatus.FINISH) {//如果已经完成,则累计1
                finishCount++;
            }
        }

        //设置已完成次数
        dayTrain.setFinishCount(finishCount);
        dayTrainDao.update(dayTrain);
        return dayTrain;
    }

    /**
     * 获取用户当天的特训id
     *
     * @param userId 用户id
     * @return
     */
    private String getId(long userId, int subject) {
        String id = userId + DateFormatUtils.format(System.currentTimeMillis(), "yyMMdd") + "_" + subject;
        return id;
    }

    /**
     * 随机获取知识点
     *
     * @param pointNum
     * @return
     */
    public DayTrain getRandomPoints(int pointNum, int subject) {
        List<QuestionPoint> questionPoints = new ArrayList<>();

        List<KnowledgeModule> points = dayTrainSettingsService.getPointsBySubject(subject);
        int avgNumber = (pointNum / points.size()) + 1;
        for (KnowledgeModule point : points) {
            //根据一级知识点,随机获取指定个数的3级知识点
            questionPoints.addAll(questionPointDubboService.randomPoint(point.getPointId(), avgNumber));
        }
        //重新打乱排序
        Collections.shuffle(questionPoints);
        //取出用户指定个数的3级知识点
        final List<QuestionPoint> finalPoints = questionPoints.subList(0, pointNum);
        List<TrainPoint> trainPoints = new ArrayList<>();
        //遍历创建今日特训知识点列表
        for (QuestionPoint questionPoint : finalPoints) {
            final TrainPoint trainPoint = TrainPoint.builder()
                    .name(questionPoint.getName())
                    .status(TrainPointStatus.UNDO)
                    .questionPointId(questionPoint.getId()).build();
            trainPoints.add(trainPoint);
        }
        DayTrain dayTrain = DayTrain.builder().createTime(new Date())
                .id("-1")
                .allCount(trainPoints.size())
                .points(trainPoints)
                .questionCount(pointNum).build();
        return dayTrain;
    }

    /**
     * @param userId
     * @param subject
     * @return
     */
    public DayTrain findDayTrain(long userId, int subject) {
        DayTrain dayTrain = dayTrainDao.findById(getId(userId, subject));

        if (dayTrain == null) {
            dayTrain = createDayTrainByUserPoints(userId, subject);
        }
        return dayTrain;
    }

    /**
     * 第二版每日特训专用创建逻辑
     * @param userId
     * @param subject
     * @return
     */
    private DayTrain createDayTrainByUserPoints(long userId, int subject) {

        List<QuestionPoint> finalPoints = questionPointDubboService.findDayTrainPoints(userId, subject, DAY_TRAIN_POINT_COUNT);

        List<TrainPoint> trainPoints = new ArrayList<>();
        //遍历创建今日特训知识点列表
        for (QuestionPoint questionPoint : finalPoints) {
            final TrainPoint trainPoint = TrainPoint.builder()
                    .name(questionPoint.getName())
                    .status(TrainPointStatus.UNDO)
                    .questionPointId(questionPoint.getId()).build();
            trainPoints.add(trainPoint);
        }
        DayTrain dayTrain = DayTrain.builder().createTime(new Date())
                .id(getId(userId, subject))
                .allCount(trainPoints.size())
                .points(trainPoints)
                .questionCount(DAY_TRAIN_QUESTION_COUNT).build();

        dayTrainDao.insert(dayTrain);
        return dayTrain;
    }
}
