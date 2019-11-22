package com.huatu.tiku.essay.service.impl.comment;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.entity.EssayLabelTotal;
import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import com.huatu.tiku.essay.entity.correct.CommentTemplateDetail;
import com.huatu.tiku.essay.entity.correct.LabelCommentRelation;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.TemplateEnum;
import com.huatu.tiku.essay.repository.v2.EssayCommentTemplateDetailRepository;
import com.huatu.tiku.essay.repository.v2.EssayCommentTemplateRepository;
import com.huatu.tiku.essay.repository.v2.EssayLabelCommentRelationRepository;
import com.huatu.tiku.essay.service.comment.AdminCommentTemplateService;
import com.huatu.tiku.essay.service.question.EssayQuestionLabelService;
import com.huatu.tiku.essay.service.v2.EssayTemplateService;
import com.huatu.tiku.essay.service.v2.question.QuestionTypeService;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelDetailVO;
import com.huatu.tiku.essay.vo.admin.question.QuestionLabelTotalVO;
import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.correct.report.EssayQuestionCorrectReportVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkResultVo;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class AdminCommentTemplateServiceImpl implements AdminCommentTemplateService {

    @Autowired
    EssayCommentTemplateRepository essayCommentTemplateRepository;

    @Autowired
    EssayCommentTemplateDetailRepository essayCommentTemplateDetailRepository;

    @Autowired
    EssayLabelCommentRelationRepository essayLabelCommentRelationRepository;

    @Autowired
    QuestionTypeService questionTypeService;

    @Autowired
    EssayTemplateService essayTemplateService;

    @Autowired
    AdminCommentTemplateService adminCommentTemplateService;

    @Autowired
    EssayQuestionLabelService essayQuestionLabelService;


    @Override
    public List<LabelCommentRelationVO> findRemarkListById(Long id, TemplateEnum.LabelTypeEnum labelTypeEnum) {
        List<LabelCommentRelation> relations = essayLabelCommentRelationRepository.findByLabelIdAndTypeAndStatus(id, labelTypeEnum.getType(), EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus());
        System.out.println("remarkList = " + new Gson().toJson(relations));
        List<LabelCommentRelationVO> labelCommentRelationVOList = convertRelation2VO(relations);
        return labelCommentRelationVOList;
    }

    public List<LabelCommentRelationVO> convertRelation2VO(List<LabelCommentRelation> relations) {
        List<LabelCommentRelationVO> labelCommentRelationVOList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(relations)) {
            relations.stream().forEach(labelCommentRelation -> {
                LabelCommentRelationVO labelCommentRelationVo = new LabelCommentRelationVO();
                BeanUtils.copyProperties(labelCommentRelation, labelCommentRelationVo);
                labelCommentRelationVOList.add(labelCommentRelationVo);
            });
        }
        return labelCommentRelationVOList;
    }

    @Override
    public BiFunction<Long, QuestionLabelDetailVO, List<LabelCommentRelationVO>> getSaveLabelDetailRemarks() {
        return saveLabelDetailRemarks;
    }

    /**
     * 保存答题卡状态
     */
    private BiFunction<Long, QuestionLabelTotalVO, List<LabelCommentRelationVO>> saveLabelTotalRemarks = ((totalId, labelTotalVO) -> {
        List<LabelCommentRelationVO> remarkList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(labelTotalVO.getRemarkList())) {
            remarkList.addAll(labelTotalVO.getRemarkList());
        }
        if (CollectionUtils.isNotEmpty(labelTotalVO.getDeRemarkList())) {
            remarkList.addAll(labelTotalVO.getDeRemarkList());
        }
        if (CollectionUtils.isEmpty(remarkList)) {
            return Lists.newArrayList();
        }
        List<LabelCommentRelation> collect = remarkList.stream().map(i -> convertLabelCommentRelation(totalId, TemplateEnum.LabelTypeEnum.QUESTION_TOTAL, i))
                .collect(Collectors.toList());
        delLabelCommentRelation(totalId, TemplateEnum.LabelTypeEnum.QUESTION_TOTAL);
        List<LabelCommentRelation> save = essayLabelCommentRelationRepository.save(collect);
        return convertRelation2VO(save);
    });

    @Override
    public BiFunction<Long, QuestionLabelTotalVO, List<LabelCommentRelationVO>> getSaveLabelTotalRemarks() {
        return saveLabelTotalRemarks;
    }

    @Override
    public void delLabelCommentRelation(long totalId, TemplateEnum.LabelTypeEnum labelTypeEnum) {
        essayLabelCommentRelationRepository.updateStatusByLabelId(totalId, labelTypeEnum.getType(), EssayLabelStatusConstant.EssayLabelStatusEnum.DELETED.getStatus());
    }

    @Override
    public List<CommentTemplate> findTemplateByIds(List<Long> templateIds) {
        return essayCommentTemplateRepository.findByIdIn(templateIds);
    }

    @Override
    public List<CommentTemplateDetail> findCommentByIds(List<Long> commentIds) {
        return essayCommentTemplateDetailRepository.findByIdInAndStatus(commentIds, EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
    }

    /**
     * 保存答题卡状态
     */
    private BiFunction<Long, QuestionLabelDetailVO, List<LabelCommentRelationVO>> saveLabelDetailRemarks = ((detailId, labelDetailVO) -> {
        List<LabelCommentRelationVO> remarkList = labelDetailVO.getRemarkList();
        if (CollectionUtils.isEmpty(remarkList)) {
            return Lists.newArrayList();
        }
        List<LabelCommentRelation> collect = remarkList.stream().map(i -> convertLabelCommentRelation(detailId, TemplateEnum.LabelTypeEnum.QUESTION_DETAIL, i))
                .collect(Collectors.toList());
        delLabelCommentRelation(detailId, TemplateEnum.LabelTypeEnum.QUESTION_DETAIL);
        List<LabelCommentRelation> save = essayLabelCommentRelationRepository.save(collect);
        return convertRelation2VO(save);
    });

    /**
     * 转换成可写入实体类对象
     *
     * @param detailId
     * @param questionDetail
     * @param labelCommentRelationVO
     * @return
     */
    private LabelCommentRelation convertLabelCommentRelation(Long detailId, TemplateEnum.LabelTypeEnum questionDetail, LabelCommentRelationVO labelCommentRelationVO) {
        LabelCommentRelation labelCommentRelation = new LabelCommentRelation();
        BeanUtils.copyProperties(labelCommentRelationVO, labelCommentRelation);
        labelCommentRelation.setType(questionDetail.getType());
        labelCommentRelation.setLabelId(detailId);
        labelCommentRelation.setStatus(EssayLabelStatusConstant.EssayLabelStatusEnum.NORMAL.getStatus());
        String bizId = labelCommentRelationVO.getBizId();
        if (StringUtils.isNotBlank(bizId) &&
                NumberUtils.isDigits(bizId) &&
                labelCommentRelationVO.getBizType() == TemplateEnum.BizTypeEnum.REGULAR.getId() &&
                Integer.parseInt(bizId) == labelCommentRelationVO.getCommentId()) {     //如果前端传过来的bizId是一个整数且值跟commentId一样则直接不存储（过滤无用数据）
            labelCommentRelation.setBizId(Strings.EMPTY);
        }
        return labelCommentRelation;
    }



    @Override
    public List<LabelCommentRelationVO> findRemarkListByLabelIds(int labelType, List<Long> labelIds) {
        List<LabelCommentRelation> relations = essayLabelCommentRelationRepository.findByStatusAndTypeAndLabelIdIn(EssayStatusEnum.NORMAL.getCode(),
                labelType, labelIds);

        List<LabelCommentRelationVO> labelCommentRelationVOList = convertRelation2VO(relations);
        return labelCommentRelationVOList;
    }


}

