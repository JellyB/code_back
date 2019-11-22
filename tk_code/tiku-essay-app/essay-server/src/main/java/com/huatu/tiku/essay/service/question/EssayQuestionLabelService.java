package com.huatu.tiku.essay.service.question;

import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import com.huatu.tiku.essay.essayEnum.CommonOperateEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelDetailVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelTotalVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelVO;

import java.util.List;
import java.util.function.BiFunction;

public interface EssayQuestionLabelService {

    /**
     * 批量查询用户试题答案的总批注表数据
     *
     * @param answerIds
     * @param studentLook
     * @return
     */
    List<EssayLabelTotal> findTotalByAnswerIds(List<Long> answerIds, LabelFlagEnum studentLook);

    /**
     * 查询单题详细批注
     *
     * @param labelDetailId
     * @return
     */
    QuestionLabelDetailVO findDetailById(long labelDetailId);


    /**
     * 保存详细批注
     *
     * @param labelDetailVO
     * @return
     */
    Object saveLabelDetail(QuestionLabelDetailVO labelDetailVO);

    /**
     * 删除详细批注
     *
     * @param labelDetailId
     * @param labelContent
     */
    Object delQuestionDetail(long labelDetailId, String labelContent);

    /**
     * 查询单题阅卷批注
     *
     * @param totalId
     * @return
     */
    QuestionLabelTotalVO findTotalInfoById(long totalId);

    /**
     * 保存单题阅卷批注
     *
     * @param questionLabelTotalVO
     * @return
     */
    Object saveLabelTotal(QuestionLabelTotalVO questionLabelTotalVO);

    /**
     * 删除单题阅卷批注
     *
     * @param totalId
     */
    void delQuestionTotal(long totalId);

    /**
     * 查询单题批改基础信息
     *
     * @param questionAnswerCardId
     * @param commonOperateEnum
     * @return
     */
    QuestionLabelVO getMainLabelInfo(long questionAnswerCardId, CommonOperateEnum commonOperateEnum);

    /**
     * 根据答题卡信息初始化批改信息
     *
     * @return
     */
    BiFunction<EssayQuestionAnswer, LabelFlagEnum, EssayLabelTotal> getInitLabelTotalFunction();

    /**
     * 本题阅卷（扣分项和得分项）
     *
     * @param labelTotalVO
     * @param remarks
     * @param commentTemplates
     */
    void handlerLabelTotalRemarks(QuestionLabelTotalVO labelTotalVO, List<LabelCommentRelationVO> remarks, List<CommentTemplate> commentTemplates);

    /**
     * 表示批注已完成
     *
     * @param totalId
     */
    void labelFinish(long totalId);

    /**
     * 关键句相关批注查询
     * @param totalId
     * @return
     */
    List<EssayLabelDetail> getThesisList(long totalId);
}
