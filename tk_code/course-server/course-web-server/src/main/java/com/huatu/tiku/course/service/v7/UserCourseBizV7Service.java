package com.huatu.tiku.course.service.v7;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.course.bean.vo.EssayAnswerCardInfo;
import com.huatu.tiku.course.bean.vo.EssayCourseWorkSyllabusInfo;
import com.huatu.tiku.course.bean.vo.LiveRecordInfo;
import java.util.Map;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-22 9:50 PM
 **/
public interface UserCourseBizV7Service {

    /**
     * 直播数据上报处理
     * @param userId
     * @param liveRecordInfo
     * @param terminal
     * @param subject
     * @throws BizException
     */
    void dealLiveReport(int userId, String userName, int subject, int terminal, String cv, LiveRecordInfo liveRecordInfo) throws BizException;


    /**
     * 课后作业全部已读 行测、申论
     * @param userId
     * @param type
     * @param uName
     * @throws BizException
     */
    long allReadByType(int userId, String type, String uName) throws BizException;

    /**
     * 单条已读
     * @param userId
     * @param type
     * @param syllabusId
     * @return
     * @throws BizException
     */
     Object readyOneCourseWork(int userId, int type, long syllabusId) throws BizException;


    /**
     * 获取行测、申论课后作业列表
     * @param userId
     * @param type
     * @param page
     * @param size
     * @return
     * @throws BizException
     */
    Object courseWorkList(int userId, int type, int page, int size) throws BizException;


    /**
     * 课后作业未读数
     * @param userId
     * @param userName
     * @return
     * @throws BizException
     */
    Map<String, Long> getCountByType(int userId, String userName) throws BizException;


    /**
     * 获取申论课后作业大纲信息 & 答题卡状态
     * @param userId
     * @param videoType
     * @param courseWareId
     * @param syllabusId
     * @param cardId
     * @return
     * @throws BizException
     */
    EssayCourseWorkSyllabusInfo essayCourseWorkSyllabusInfo(int userId, Integer videoType, Long courseWareId, Long syllabusId, Long cardId) throws BizException;


    /**
     * 使用 syllabusId 构建申论课后作业答题卡信息
     * @param userId
     * @param syllabusId
     * @return
     * @throws BizException
     */
    EssayAnswerCardInfo buildEssayAnswerCardInfo(int userId, long syllabusId) throws BizException;


    void exportData() throws BizException;
}
