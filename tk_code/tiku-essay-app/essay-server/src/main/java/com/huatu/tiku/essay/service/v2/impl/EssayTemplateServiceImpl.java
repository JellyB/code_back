package com.huatu.tiku.essay.service.v2.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.dto.CommentTemplateDto;
import com.huatu.tiku.essay.dto.CommentTemplateNodeDto;
import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import com.huatu.tiku.essay.entity.correct.CommentTemplateDetail;
import com.huatu.tiku.essay.essayEnum.EssayIsDeleteEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.TemplateEnum;
import com.huatu.tiku.essay.repository.v2.CommentTemplateDetailRepository;
import com.huatu.tiku.essay.repository.v2.CommentTemplateRepository;
import com.huatu.tiku.essay.service.v2.EssayTemplateService;
import com.huatu.tiku.essay.vo.admin.CommentTemplateDetailVo;
import com.huatu.tiku.essay.vo.admin.CommentTemplateNodeVo;
import com.huatu.tiku.essay.vo.admin.CommentTemplateVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 3:52 PM
 **/

@Slf4j
@Service
public class EssayTemplateServiceImpl implements EssayTemplateService {

    @Autowired
    private CommentTemplateRepository commentTemplateRepository;

    @Autowired
    private CommentTemplateDetailRepository commentTemplateDetailRepository;

    @Autowired
    RedisTemplate redisTemplate;


    /**
     * 根据 type 查询所有的评语模板
     *
     * @param type
     * @return
     * @throws BizException
     */
    @Override
    public Object findAllTemplateByType(int type) throws BizException {
        List<CommentTemplateVo> voList = Lists.newArrayList();
        constructTemplateBase(voList, type);
        for (CommentTemplateVo nodeVo : voList) {
            List<CommentTemplate> templateList_ = commentTemplateRepository.findAllByTypeAndStatusAndLabelType(type, EssayStatusEnum.NORMAL.getCode(), nodeVo.getLabelType());
            if (CollectionUtils.isEmpty(templateList_)) {
                continue;
            }
            dealTemplateList(nodeVo, templateList_);
        }
        return voList;
    }


    /**
     * 通过枚举返回试题 label 集合
     *
     * @param voList
     * @param type
     * @return
     */
    private void constructTemplateBase(List<CommentTemplateVo> voList, int type) {

        List<TemplateEnum.CommentTemplateEnum> base = TemplateEnum.QuestionLabelEnum.allLalel(type);
        for (int i = 0; i < base.size(); i++) {
            TemplateEnum.CommentTemplateEnum commentTemplateEnum = base.get(i);
            CommentTemplateVo commentTemplateVo = new CommentTemplateVo();
            commentTemplateVo.setLabelType(commentTemplateEnum.getType());
            commentTemplateVo.setLabelTypeName(commentTemplateEnum.getValue());
            commentTemplateVo.setSort(i + 1);
            commentTemplateVo.setTemplateList(Lists.newArrayList());
            voList.add(commentTemplateVo);
        }
    }

    /**
     * 处理模板集合信息
     *
     * @param nodeVo
     * @param templateList_
     */
    private void dealTemplateList(CommentTemplateVo nodeVo, List<CommentTemplate> templateList_) {
        List<CommentTemplateDetailVo> templateList = Lists.newArrayList();
        for (CommentTemplate commentTemplate : templateList_) {
            CommentTemplateDetailVo commentTemplateDetailVo = new CommentTemplateDetailVo();
            commentTemplateDetailVo.setId(commentTemplate.getId());
            commentTemplateDetailVo.setContent(commentTemplate.getName());
            commentTemplateDetailVo.setSort(commentTemplate.getSort());
            commentTemplateDetailVo.setBizType(commentTemplate.getBizType());
            dealFirstCommentInfo(commentTemplateDetailVo);
            templateList.add(commentTemplateDetailVo);
        }
        nodeVo.setTemplateList(templateList);
    }


    private void dealFirstCommentInfo(CommentTemplateDetailVo commentTemplateDetailVo) {
        List<CommentTemplateNodeVo> comments = Lists.newArrayList();
        long templateId = commentTemplateDetailVo.getId();
        if (templateId <= 0) {
            commentTemplateDetailVo.setComments(comments);
            return;
        }

        List<CommentTemplateDetail> commentTemplateDetails = commentTemplateDetailRepository.findAllByTemplateIdAndStatusAndPid(templateId, EssayStatusEnum.NORMAL.getCode());

        if (CollectionUtils.isEmpty(commentTemplateDetails)) {
            commentTemplateDetailVo.setComments(comments);
            return;
        }
        for (CommentTemplateDetail commentTemplateDetail : commentTemplateDetails) {
            CommentTemplateNodeVo commentTemplateNodeVo = new CommentTemplateNodeVo();
            commentTemplateNodeVo.setId(commentTemplateDetail.getId());
            commentTemplateNodeVo.setContent(commentTemplateDetail.getContent());
            commentTemplateNodeVo.setSort(commentTemplateDetail.getSort());
            commentTemplateNodeVo.setIsExtended(commentTemplateDetail.getIsExtended());
            dealSecondCommentInfo(commentTemplateNodeVo);
            comments.add(commentTemplateNodeVo);
        }
        commentTemplateDetailVo.setComments(comments);
    }


    private void dealSecondCommentInfo(CommentTemplateNodeVo commentTemplateNodeVo) {
        List<CommentTemplateNodeVo> children = Lists.newArrayList();
        long pid = commentTemplateNodeVo.getId();
        if (pid == 0) {
            commentTemplateNodeVo.setChildren(children);
            return;
        }
        List<CommentTemplateDetail> commentTemplateDetails = commentTemplateDetailRepository.findAllByPidAndStatus(pid, EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(commentTemplateDetails)) {
            commentTemplateNodeVo.setChildren(children);
            return;
        }

        for (CommentTemplateDetail commentTemplateDetail : commentTemplateDetails) {
            CommentTemplateNodeVo child = new CommentTemplateNodeVo();
            child.setId(commentTemplateDetail.getId());
            child.setContent(commentTemplateDetail.getContent());
            child.setChildren(null);
            child.setIsExtended(null);
            child.setSort(commentTemplateDetail.getSort());
            children.add(child);
        }
        commentTemplateNodeVo.setChildren(children);
    }


    /**
     * 模板评语保存
     *
     * @param dto
     * @return
     * @throws BizException
     */
    @Override
    public Object save(CommentTemplateDto dto) throws BizException {
        log.info("CommentTemplateDto info:{}", JSONObject.toJSONString(dto));
        CommentTemplate commentTemplate;
        if (dto.getId() > 0) {
            commentTemplate = commentTemplateRepository.findOne(dto.getId());
            if (commentTemplate == null) {
                throw new BizException(ErrorResult.create(100010, "数据不存在！"));
            }
            commentTemplate.setId(dto.getId());
            commentTemplate.setGmtCreate(new Date());
        } else {
            commentTemplate = new CommentTemplate();
            commentTemplate.setGmtCreate(new Date());
        }
        TemplateEnum.BizTypeEnum bizTypeEnum = TemplateEnum.BizTypeEnum.create(dto.getBizType());
        commentTemplate.setBizType(bizTypeEnum.getId());
        commentTemplate.setType(dto.getType());
        commentTemplate.setName(dto.getContent());
        commentTemplate.setSort(dto.getSort());
        commentTemplate.setLabelType(dto.getLabelType());
        commentTemplate.setStatus(EssayStatusEnum.NORMAL.getCode());
        commentTemplate.setGmtModify(new Date());

        // 新增或者更新模板数据
        CommentTemplate result = commentTemplateRepository.save(commentTemplate);
        if (CollectionUtils.isNotEmpty(dto.getComments())) {
            long templateId = result.getId();
            dealFirstComments(dto.getComments(), templateId);
        }
        return SuccessMessage.create();
    }

    /**
     * 首级评语处理
     *
     * @param firstComments
     * @param templateId
     * @throws BizException
     */
    private void dealFirstComments(List<CommentTemplateNodeDto> firstComments, long templateId) throws BizException {
        AtomicInteger index = new AtomicInteger(1);
        for (int i = 0; i < firstComments.size(); i++) {
            CommentTemplateNodeDto firstComment = firstComments.get(i);
            if (firstComment.getIsDelete() == EssayIsDeleteEnum.DELETED.getCode()) {
                dealDeleteFirstComments(firstComment);
            } else {
                if (StringUtils.isEmpty(firstComment.getContent())) {
                    continue;
                }
                CommentTemplateDetail firstDetail;
                if (firstComment.getId() > 0) {
                    firstDetail = commentTemplateDetailRepository.findOne(firstComment.getId());
                    if (null == firstDetail) {
                        throw new BizException(ErrorResult.create(100010, "数据不存在！"));
                    }
                    firstDetail.setId(firstComment.getId());
                } else {
                    firstDetail = new CommentTemplateDetail();
                    firstDetail.setGmtCreate(new Date());
                }
                firstDetail.setTemplateId(templateId);
                firstDetail.setContent(firstComment.getContent());
                firstDetail.setSort(index.getAndIncrement());
                firstDetail.setPid(-1);
                firstDetail.setStatus(EssayStatusEnum.NORMAL.getCode());
                firstDetail.setGmtModify(new Date());
                commentTemplateDetailRepository.save(firstDetail);
                long pid = firstDetail.getId();
                List<CommentTemplateNodeDto> secondComments = firstComment.getChildren();
                if (CollectionUtils.isNotEmpty(secondComments)) {
                    dealSecondComments(secondComments, pid);
                }
            }
        }
    }

    /**
     * 处理二级评论列表
     *
     * @param secondComments
     * @param pid
     * @throws BizException
     */
    private void dealSecondComments(List<CommentTemplateNodeDto> secondComments, long pid) throws BizException {
        AtomicInteger index = new AtomicInteger(1);
        for (int i = 0; i < secondComments.size(); i++) {
            CommentTemplateNodeDto secondNoteDto = secondComments.get(i);
            if (secondNoteDto.getIsDelete() == EssayIsDeleteEnum.DELETED.getCode()) {
                dealDeleteSecondCommentsVo(secondNoteDto);
            } else {
                if (StringUtils.isEmpty(secondNoteDto.getContent())) {
                    continue;
                }
                CommentTemplateDetail secondDetail;
                if (secondNoteDto.getId() > 0) {
                    secondDetail = commentTemplateDetailRepository.findOne(secondNoteDto.getId());
                    secondDetail.setId(secondNoteDto.getId());
                } else {
                    secondDetail = new CommentTemplateDetail();
                    secondDetail.setGmtCreate(new Date());
                }
                secondDetail.setTemplateId(-1);
                secondDetail.setContent(secondNoteDto.getContent());
                secondDetail.setSort(index.getAndIncrement());
                secondDetail.setPid(pid);
                secondDetail.setGmtModify(new Date());
                secondDetail.setStatus(EssayStatusEnum.NORMAL.getCode());
                commentTemplateDetailRepository.save(secondDetail);
            }
        }
    }

    /**
     * 删除一级评论
     *
     * @param firstComment
     */
    private void dealDeleteFirstComments(CommentTemplateNodeDto firstComment) {
        if (firstComment.getId() <= 0) {
            return;
        }
        CommentTemplateDetail detail = commentTemplateDetailRepository.findOne(firstComment.getId());
        if (null == detail) {
            log.error("first CommentTemplateDetail not found:{}", firstComment.getId());
            throw new BizException(ErrorResult.create(100010, "数据不存在！"));
        }
        detail.setGmtModify(new Date());
        detail.setStatus(EssayStatusEnum.DELETED.getCode());
        long pid = firstComment.getId();
        List<CommentTemplateDetail> children = commentTemplateDetailRepository.findAllByPidAndStatus(pid, EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isNotEmpty(children)) {
            for (CommentTemplateDetail child : children) {
                dealDeleteSecondCommentsEntity(child);
            }
        }
        commentTemplateDetailRepository.save(detail);
        firstComment.setChildren(Lists.newArrayList());
    }

    /**
     * 根据实体类更新删除二级评论
     *
     * @param secondDetail
     */
    private void dealDeleteSecondCommentsEntity(CommentTemplateDetail secondDetail) {
        secondDetail.setStatus(EssayStatusEnum.DELETED.getCode());
        secondDetail.setGmtModify(new Date());
        commentTemplateDetailRepository.save(secondDetail);
    }

    /**
     * 更具vo对象更新删除二级评论
     *
     * @param secondNoteDto
     */
    private void dealDeleteSecondCommentsVo(CommentTemplateNodeDto secondNoteDto) {
        if (secondNoteDto.getId() <= 0) {
            return;
        }
        CommentTemplateDetail secondDetail = commentTemplateDetailRepository.findOne(secondNoteDto.getId());
        if (null == secondDetail) {
            log.error("second CommentTemplateDetail not found:{}", secondNoteDto.getId());
            throw new BizException(ErrorResult.create(100010, "数据不存在！"));
        }
        secondDetail.setStatus(EssayStatusEnum.DELETED.getCode());
        secondDetail.setGmtModify(new Date());
        commentTemplateDetailRepository.save(secondDetail);
    }

    @Override
    public Object removeLogic(long templateId) throws BizException {
        CommentTemplate commentTemplate = commentTemplateRepository.findOne(templateId);
        if (null == commentTemplate) {
            throw new BizException(ErrorResult.create(1000010, "模板数据不存在"));
        } else {
            commentTemplate.setStatus(EssayStatusEnum.DELETED.getCode());
            commentTemplate.setGmtModify(new Date());
            commentTemplateRepository.save(commentTemplate);
        }
        return SuccessMessage.create();
    }

    @Override
    public List<CommentTemplateDetailVo> findTemplateByLabelTypeAndType(int type, int labelType) {
        List<CommentTemplate> templateList = commentTemplateRepository.findAllByTypeAndStatusAndLabelType(type,
                EssayStatusEnum.NORMAL.getCode(), labelType);

        if (CollectionUtils.isEmpty(templateList)) {
            return null;
        }
        List<CommentTemplateDetailVo> CommentTemplateDetailVo = Lists.newArrayList();

        templateList.stream().forEach(template -> {
            CommentTemplateDetailVo commentTemplateDetailVo = new CommentTemplateDetailVo();
            commentTemplateDetailVo.setId(template.getId());
            commentTemplateDetailVo.setBizType(template.getBizType());
            commentTemplateDetailVo.setContent(template.getName());
            commentTemplateDetailVo.setSort(template.getSort());
            //设置comment
            dealFirstCommentInfo(commentTemplateDetailVo);
            CommentTemplateDetailVo.add(commentTemplateDetailVo);
        });
        return CommentTemplateDetailVo;
    }
}
