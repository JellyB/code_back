package com.huatu.tiku.match.service.v1.meta;


import com.huatu.tiku.match.bean.entity.MatchUserMeta;
import com.huatu.tiku.match.enums.MatchInfoEnum;
import com.huatu.ztk.chart.Line;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.AnswerCard;
import org.springframework.scheduling.annotation.Async;
import service.BaseServiceHelper;

import java.util.List;

/**
 * 模考大赛用户数据统计处理
 * Created by huangqingpeng on 2018/10/16.
 */
public interface MatchUserMetaService extends BaseServiceHelper<MatchUserMeta> {

    /**
     * 获取用户模考大赛报名参考数据
     *
     * @param userId
     * @param paperId
     * @return
     */
    MatchUserMeta findMatchUserEnrollInfo(int userId, int paperId);

    /**
     * 实时存储用户模考大赛报名信息(报名或者修改报名数据时使用)
     *
     * @param userId
     * @param paperId
     * @param positionId
     * @param schoolId
     * @param schoolName
     * @param enrollTime
     * @param essayPaperId
     * @return
     */
    int saveMatchEnrollInfo(int userId, int paperId, int positionId, int schoolId, String schoolName, long enrollTime, long essayPaperId);


    /**
     * 查询报名总人数
     *
     * @param paperId
     * @return
     */
    int getEnrollTotal(int paperId);

    /**
     * 查询职位报名人数
     *
     * @param paperId
     * @param positionId
     * @return
     */
    int getPositionTotal(int paperId, int positionId);

    /**
     * 同步答题卡创建到新系统
     *
     * @param paperId
     * @param userId
     * @param practiceId
     * @param createTime
     * @return
     */
    int savePracticeId(int paperId, int userId, long practiceId, long createTime) throws BizException;

    /**
     * 查询模考大赛答题卡ID
     *
     * @param paperId
     * @param userId
     * @return
     */
    long getMatchPracticeId(int paperId, int userId);

    /**
     * 用户成绩是否存在
     *
     * @param paperId
     * @param userId
     * @return
     */
    boolean isExistedScore(int paperId, int userId);


    /**
     * 交卷操作存储
     *
     * @param paperId
     * @param userId
     * @param submitTypeEnum
     * @param score
     * @param submitTime
     * @return
     */
    int saveMatchScore(int paperId, int userId, MatchInfoEnum.SubmitTypeEnum submitTypeEnum, double score, long submitTime) throws BizException;

    /**
     * 答题卡是否提交
     *
     * @param paperId
     * @param userId
     * @return
     */
    boolean isSubmitted(int paperId, int userId);

    /**
     * 模考大赛是否已结束
     *
     * @param paperId
     * @return
     */
    boolean isFinished(int paperId);

    /**
     * 获取用户模考大赛报告及历史（只有历史数据，未结束的考试排除掉）
     *
     * @param userId
     * @param tagId
     * @param subjectId
     * @return
     */
    public List<MatchUserMeta> getAvailableMatchMeta(int userId, int tagId, int subjectId);

    /**
     * 用户模考大赛成绩生成曲线图
     *
     * @param matchUserMetas 用户统计数据
     * @return
     */
    Line getMatchLine(List<MatchUserMeta> matchUserMetas);

    /**
     * 获取最大考试分数
     *
     * @param paperId
     * @return
     */
    Double getMaxScore(int paperId);

    /**
     * 获取用户报告
     *
     * @param paperId
     * @param userId
     * @return
     */
    MatchUserMeta getReport(int paperId, int userId) throws BizException;

    /**
     * 同步信息到数据统计表中
     *
     * @param userMeta
     * @param answerCard
     * @param essayPaperId
     */
    @Async
    void sync2DB(com.huatu.ztk.paper.bean.MatchUserMeta userMeta, AnswerCard answerCard, long essayPaperId);

    /**
     * 遍历所有的答题卡ID，并重新计算试题统计信息
     */
    void restAnswerCard();

}
