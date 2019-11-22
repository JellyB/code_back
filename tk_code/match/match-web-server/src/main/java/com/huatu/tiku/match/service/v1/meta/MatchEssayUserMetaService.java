//package com.huatu.tiku.match.service.v1.meta;
//
//
//import com.huatu.common.exception.BizException;
//import com.huatu.tiku.match.bean.entity.MatchEssayUserMeta;
//import com.huatu.tiku.match.enums.EssayMatchStatusEnum;
//import com.huatu.tiku.match.enums.MatchInfoEnum;
//import service.BaseServiceHelper;
//
///**
// * Created by huangqingpeng on 2018/10/17.
// */
//public interface MatchEssayUserMetaService extends BaseServiceHelper<MatchEssayUserMeta> {
//
//
//    /**
//     * 返回用户申论考试状态(用于联合考试申论阶段判断逻辑)
//     *
//     * @param essayPaperId
//     * @param userId
//     * @return 用户答题卡状态(1未完成2已交卷3已批改)
//     */
//    @Deprecated
//    EssayMatchStatusEnum getEssayUserAnswerStatus(long essayPaperId, int userId);
//
//    /**
//     * 模考大赛是否已结束
//     *
//     * @param essayPaperId
//     * @return
//     */
//    @Deprecated
//    boolean isFinished(long essayPaperId);
//
//
//    /**
//     * 实时存储用户模考大赛报名信息
//     *
//     * @param userId
//     * @param essayPaperId
//     * @param positionId
//     * @return
//     */
//    @Deprecated
//    int saveMatchEnrollInfo(int userId, long essayPaperId, int positionId);
//
//
//    /**
//     * 同步报名信息到新系统
//     *
//     * @param userId
//     * @param essayPaperId
//     * @param positionId
//     * @param enrollTime
//     * @return
//     */
//    @Deprecated
//    int saveMatchEnrollInfo(int userId, long essayPaperId, int positionId, long enrollTime);
//
//    /**
//     * 保存答题卡
//     *
//     * @param essayPaperId
//     * @param userId
//     * @param practiceId
//     * @param createTime
//     */
//    @Deprecated
//    void savePracticeId(long essayPaperId, int userId, Long practiceId, Long createTime) throws BizException;
//
//
//    /**
//     * 交卷操作存储
//     *
//     * @param essayPaperId
//     * @param userId
//     * @param submitTypeEnum
//     * @param score
//     * @param submitTime
//     * @return
//     */
//    @Deprecated
//    int saveMatchScore(long essayPaperId, int userId, MatchInfoEnum.SubmitTypeEnum submitTypeEnum, double score, long submitTime) throws BizException;
//
//}
