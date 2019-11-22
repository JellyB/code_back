package com.huatu.ztk.paper.service.v4;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Answer;
import com.huatu.ztk.paper.bean.AnswerCard;
import com.huatu.ztk.paper.bean.StandardCard;
import com.huatu.ztk.paper.bo.SmallEstimateHeaderBo;
import com.huatu.ztk.paper.bo.SmallEstimateSimpleReportBo;
import com.huatu.ztk.question.util.PageUtil;
import com.self.generator.core.WaitException;

import java.util.List;

/**
 * 小模考相关实现
 * Created by huangqingpeng on 2019/2/13.
 */
public interface SmallEstimateService {

    /**
     * 查询当天小模考数据
     * @param subject
     * @param uid
     * @return
     */
    List<SmallEstimateHeaderBo> findTodaySmallEstimateInfo(int subject, long uid) throws BizException;

    /**
     * 创建小模考答题卡
     * @param id
     * @param subject
     * @param userId
     * @param terminal
     * @return
     */
    StandardCard create(int id, int subject, long userId, int terminal) throws WaitException, BizException;

    /**
     * 查询答题卡信息（包括继续答题和查看报告逻辑）
     * @param practiceId
     * @param userId
     * @param terminal
     * @return
     */
    AnswerCard findAnswerCardDetail(Long practiceId, long userId, int terminal,String cv) throws BizException;

    /**
     * 保存用户答题信息
     * @param practiceId
     * @param userId
     * @param answers
     */
    AnswerCard saveAnswers(Long practiceId, long userId, List<Answer> answers) throws BizException;

    /**
     * 提交试卷
     * @param practiceId
     * @param userId
     * @param answers
     * @param area
     * @return
     */
    AnswerCard submitAnswer(Long practiceId, long userId, List<Answer> answers, int area,int terminal,String cv) throws BizException;

    /**
     * 分页查询报告信息
     * @param subject
     * @param uid
     * @param startTime
     * @param endTime
     * @param page
     * @param size
     * @return
     */
    PageUtil<List<SmallEstimateSimpleReportBo>> getEstimateReportPage(int subject, long uid, long startTime, long endTime, int page, int size);

}
