package com.huatu.tiku.essay.service.comment;

import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import com.huatu.tiku.essay.entity.correct.CommentTemplateDetail;
import com.huatu.tiku.essay.entity.correct.LabelCommentRelation;
import com.huatu.tiku.essay.essayEnum.TemplateEnum;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelDetailVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelTotalVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.correct.report.EssayQuestionCorrectReportVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkResultVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public interface AdminCommentTemplateService {

    /**
     * 通过批注ID和批注类型，返回相关的批注评语信息
     *
     * @param id
     * @param questionDetail
     * @return
     */
    List<LabelCommentRelationVO> findRemarkListById(Long id, TemplateEnum.LabelTypeEnum questionDetail);

    /**
     * 返回单个批注的评语保存实现函数
     *
     * @return
     */
    BiFunction<Long, QuestionLabelDetailVO, List<LabelCommentRelationVO>> getSaveLabelDetailRemarks();

    /**
     * 返回试题阅卷批注的保存实现函数
     *
     * @return
     */
    BiFunction<Long, QuestionLabelTotalVO, List<LabelCommentRelationVO>> getSaveLabelTotalRemarks();

    /**
     * 删除详细批注对应的评语数据
     *
     * @param totalId
     * @param labelTypeEnum
     */
    void delLabelCommentRelation(long totalId, TemplateEnum.LabelTypeEnum labelTypeEnum);

    /**
     * 批量查询评语模版信息
     *
     * @param templateIds
     * @return
     */
    List<CommentTemplate> findTemplateByIds(List<Long> templateIds);


    /**
     * 批量查询评语信息
     *
     * @param commentIds
     * @return
     */
    List<CommentTemplateDetail> findCommentByIds(List<Long> commentIds);

    /**
     * 将实体信息转化为vo信息
     *
     * @param relations
     * @return
     */
    List<LabelCommentRelationVO> convertRelation2VO(List<LabelCommentRelation> relations);

    /**
     * 根据批注ID批量查询批注信息
     *
     * @param labelType
     * @param labelIds
     * @return
     */
    List<LabelCommentRelationVO> findRemarkListByLabelIds(int labelType, List<Long> labelIds);


}