package com.huatu.tiku.essay.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.CommonErrors;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.CorrectRedisKeyConstant;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.admin.EssayAnalyzeUtil;
import com.huatu.tiku.essay.util.file.HtmlFileUtil;
import com.huatu.tiku.essay.util.video.BjyHandler;
import com.huatu.tiku.essay.util.video.YunUtil;
import com.huatu.tiku.essay.vo.admin.AdminMaterialListVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionRelationVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionTypeVO;
import com.huatu.tiku.essay.vo.admin.AdminQuestionVO;
import com.huatu.tiku.essay.vo.resp.EssayCenterThesisVO;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.EssayStandardAnswerVO;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;
import com.huatu.tiku.essay.vo.resp.correct.ResponseExtendVO;
import com.huatu.tiku.essay.vo.video.YunVideoInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

import static com.huatu.tiku.essay.constant.status.EssayCenterThesisConstant.EssayCenterThesisBizStatusEnum.UNADOPTED;

/**
 * Created by huangqp on 2017\12\5 0005.
 */
@Service
@Slf4j
public class EssayQuestionServiceImpl implements EssayQuestionService {
    @Autowired
    EssayPaperServiceImpl essayPaperService;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    EssayQuestionMaterialRepository essayQuestionMaterialRepository;
    @Autowired
    EssayMaterialRepository essayMaterialRepository;
    @Autowired
    EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    EssayCenterThesisRepository essayCenterThesisRepository;
    @Autowired
    EssayAreaRepository essayAreaRepository;
    @Autowired
    HtmlFileUtil htmlFileUtil;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    EssayStandardAnswerKeyPhraseRepository essayStandardAnswerKeyPhraseRepository;
    @Autowired
    EssayStandardAnswerRepository essayStandardAnswerRepository;
    @Autowired
    private EssayQuestionTypeRepository essayQuestionTypeRepository;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private BjyHandler bjyHandler;
    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Override
    public List<EssayQuestionBase> findQuestionsByPaperId(long paperId) {
        List<EssayQuestionBase> questionBases = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId,
                EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(questionBases)) {
            return Lists.newArrayList();
        }
        return questionBases;
    }

    @Override
    public EssayQuestionDetail findQuestionDetailById(long detailId) {
        return essayQuestionDetailRepository.findById(detailId);
    }

    /**
     * 添加
     *
     * @param question
     * @param paperId
     * @param uid
     * @return
     */
    @Override
    public AdminQuestionVO saveQuestionDetail(AdminQuestionVO question, long paperId, int uid) throws BizException {
        EssayQuestionBase essayQuestionBase = new EssayQuestionBase();
        String userId = uid + "";
        EssayQuestionDetail essayQuestionDetail = new EssayQuestionDetail();
        essayQuestionDetail.setIsAssigned(question.getIsAssigned());
        if (question.getSort() > 0) {
            long questionId = IsExistedSortInPaper(paperId, question.getSort());
            if (questionId != -1 && questionId != question.getQuestionBaseId()) {
                log.warn("题序已被占用：{}", question.getSort());
                throw new BizException(EssayErrors.QUESTION_SORT_EXISTED_IN_PAPER);
            }
        }
        //查询试卷信息
        EssayPaperBase paper = essayPaperBaseRepository.findOne(paperId);
        //试题id不存在，则直接创建新的试题
        if (question.getQuestionBaseId() <= 0) {
            if (0 >= question.getType()) {
                log.warn("试题无类型,试题id：{}", question.getQuestionBaseId());
                throw new BizException(EssayErrors.QUESTION_CREATE_NO_TYPE);
            }
            if (question.getSort() <= 0) {
                log.warn("首次保存试题属性时，请添加题序以便试题创建");
                throw new BizException(EssayErrors.QUESTION_CREATE_NO_SORT);
            }
            if (question.getScore() <= 0) {
                log.warn("首次保存试题属性时，请添加分数以便试题创建");
                throw new BizException(EssayErrors.QUESTION_CREATE_NO_SCORE);
            }
            double limitTime = getQuestionLimitTime(question.getScore(), question.getType());
            essayQuestionBase.setLimitTime(new Double(limitTime).intValue());
            essayQuestionBase.setPaperId(paper.getId());
            essayQuestionBase.setAreaId(paper.getAreaId());
            essayQuestionBase.setAreaName(paper.getAreaName());
            essayQuestionBase.setSubAreaId(paper.getSubAreaId());
            essayQuestionBase.setSubAreaName(paper.getSubAreaName());
            long areaSortId = paper.getAreaId();
            if (StringUtils.isNotEmpty(paper.getSubAreaName())) {
                areaSortId = paper.getSubAreaId();
            }
            EssayQuestionBelongPaperArea area = essayAreaRepository.findOne(areaSortId);
            if (null != area) {
                essayQuestionBase.setAreaSort(area.getSort());
            }

            essayQuestionBase.setQuestionDate(paper.getPaperDate());
            essayQuestionBase.setQuestionYear(paper.getPaperYear());
            essayQuestionBase.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.OFFLINE.getBizStatus());
            essayQuestionBase.setStatus(EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            essayQuestionBase.setSort(question.getSort());
            essayQuestionDetail.setScore(question.getScore());
            essayQuestionBase.setCreator(userId);
            essayQuestionBase.setGmtCreate(new Date());
            essayQuestionDetail.setCreator(userId);
            essayQuestionDetail.setGmtCreate(new Date());
            essayQuestionDetail.setComprehensiveCorrectType(question.getComprehensiveCorrectType());
            if (question.getCorrectType() > 0) {
                essayQuestionDetail.setCorrectType(question.getCorrectType());
            }
        } else {
            essayQuestionBase = essayQuestionBaseRepository.findOne(question.getQuestionBaseId());
            essayQuestionDetail = essayQuestionDetailRepository.findOne(essayQuestionBase.getDetailId());

            essayQuestionDetail.setIsAssigned(question.getIsAssigned());
            if (question.getSort() > 0) {
                essayQuestionBase.setSort(question.getSort());
            }
            if (question.getScore() > 0) {
                double limitTime = getQuestionLimitTime(question.getScore(), question.getType());
                essayQuestionBase.setLimitTime(new Double(limitTime).intValue());
                essayQuestionDetail.setScore(question.getScore());
            }
            essayQuestionBase.setModifier(userId);
            essayQuestionBase.setGmtModify(new Date());
            essayQuestionDetail.setModifier(userId);
            essayQuestionDetail.setGmtModify(new Date());
        }
        String stem = question.getStem();
        if (stem != null) {
            essayQuestionDetail.setStem(stem);
        }
        String answerRequire = convertImgAndHtml(question.getAnswerRequire(), userId);
        if (answerRequire != null) {
            essayQuestionDetail.setAnswerRequire(answerRequire);
        }
        int inputWordNumMin = question.getInputWordNumMin();
        if (inputWordNumMin > 0) {
            essayQuestionDetail.setInputWordNumMin(inputWordNumMin);
        }
        int inputWordNumMax = question.getInputWordNumMax();
        if (inputWordNumMin > 0) {
            essayQuestionDetail.setInputWordNumMax(inputWordNumMax);
        }

//        //标准答案
//        String answerComment = convertImgAndHtml(question.getAnswerComment(), userId);
//        if (answerComment != null) {
//            essayQuestionDetail.setAnswerComment(answerComment);
//        }

        String correctRule = convertImgAndHtml(question.getCorrectRule(), userId);
        if (correctRule != null) {
            essayQuestionDetail.setCorrectRule(correctRule);
        }
        String authorityReviews = convertImgAndHtml(question.getAuthorityReviews(), userId);
        if (authorityReviews != null) {
            essayQuestionDetail.setAuthorityReviews(StringUtils.EMPTY.equals(authorityReviews) ? null : authorityReviews);
        }
        /**
         * @update: huangqp 20180420
         * @from :"试卷分析维度之前是"难度"+"分析内容"，改为"难度"+"答题任务"+"答题范围"+"答题要求"
         * @idea :添加三个维度字段，代替分析内容字段;
         * 如果以后者方式录入试题，存储三个字段外，"分析内容"字段由三个字段拼接，保证前端展示的正确性；
         * 如果用最后前者方式录入，也允许；如果都没有录入，报错
         */
        String analyzeQuestion = convertImgAndHtml(question.getAnalyzeQuestion(), userId);
        String answerTask = convertImgAndHtml(question.getAnswerTask(), userId);
        String answerDetail = convertImgAndHtml(question.getAnswerDetails(), userId);
        String answerRange = convertImgAndHtml(question.getAnswerRange(), userId);
        if (StringUtils.isNotBlank(analyzeQuestion)) {
            double difficultGrade = question.getDifficultGrade();
            if (difficultGrade > 0) {
                essayQuestionDetail.setDifficultGrade(difficultGrade);
            } else {
                throw new BizException(EssayErrors.NO_CHOOSE_DIFFICULTGRADE);
            }
            if (StringUtils.isNotBlank(answerDetail) && StringUtils.isNotBlank(answerTask)
                    && StringUtils.isNotBlank(answerRange)) {
                essayQuestionDetail.setAnswerRange(answerRange);
                essayQuestionDetail.setAnswerTask(answerTask);
                essayQuestionDetail.setAnswerDetails(answerDetail);
                essayQuestionDetail
                        .setAnalyzeQuestion(EssayAnalyzeUtil.assertAnalyze(answerTask, answerRange, answerDetail));
            } else {
                essayQuestionDetail.setAnalyzeQuestion(analyzeQuestion);
            }
        } else if (StringUtils.EMPTY.equals(analyzeQuestion)) {
            //避免前端删除该字段值不更新的bug
            essayQuestionDetail.setAnalyzeQuestion(analyzeQuestion);
            essayQuestionDetail.setDifficultGrade(0D);
        }
        String answerThink = convertImgAndHtml(question.getAnswerThink(), userId);
        if (answerThink != null) {
            essayQuestionDetail.setAnswerThink(answerThink);
        }
        String bestowPointExplain = convertImgAndHtml(question.getBestowPointExplain(), userId);
        if (bestowPointExplain != null) {
            essayQuestionDetail.setBestowPointExplain(bestowPointExplain);
        }
        double score = question.getScore();
        if (score > 0) {
            essayQuestionDetail.setScore(score);
        }
        int type = question.getType();
        if (type > 0) {
            essayQuestionDetail.setType(type);
        }

        int realQuestion = question.getRealQuestion();
        if (realQuestion > 0) {
            essayQuestionDetail.setRealQuestion(realQuestion);
        } else if (0 >= essayQuestionDetail.getRealQuestion()) {
            essayQuestionDetail.setRealQuestion(1);
        }
        String commentAuthor = convertImgAndHtml(question.getCommentAuthor(), userId);
        if (commentAuthor != null) {
            essayQuestionDetail.setCommentAuthor(commentAuthor);
        }
        if (essayQuestionBase.getId() < 0) {
            essayQuestionBase.setId(question.getQuestionBaseId());
        }
        if (essayQuestionDetail.getId() < 0) {
            essayQuestionDetail.setId(question.getQuestionDetailId());
        }
        essayQuestionDetail.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus());


        AdminQuestionVO result = new AdminQuestionVO();

//        // 只有议论文和应用文会有标题
//        if(question.getType() == 4 || question.getType() == 5){
//            if(StringUtils.isNotEmpty(question.getTopic())){
//                essayQuestionDetail.setTopic(question.getTopic());
//                essayQuestionDetail.setSubTopic(question.getSubTopic());
//            }
//        }
//
//        // 只有应用文会有称呼&&落款
//        if(question.getType() == 4 ){
//            if(StringUtils.isNotEmpty(question.getCallName())){
//                essayQuestionDetail.setCallName(question.getCallName());
//            }
//            if(StringUtils.isNotEmpty(question.getInscribedDate())){
//                essayQuestionDetail.setInscribedDate(question.getInscribedDate());
//            }
//            if(StringUtils.isNotEmpty(question.getInscribedName())){
//                essayQuestionDetail.setInscribedName(question.getInscribedName());
//            }
//        }
        //人工批改,需要选择是要点制 || 划档制度
        if (question.getComprehensiveCorrectType() > 0) {
            essayQuestionDetail.setComprehensiveCorrectType(question.getComprehensiveCorrectType());
        }
        EssayQuestionDetail resultDetail = essayQuestionDetailRepository.save(essayQuestionDetail);
        //清除缓存中的标准答案
//        String standardAnswerKey = RedisKeyConstant.getStandardAnswerKey(essayQuestionDetail.getId());
//        redisTemplate.delete(standardAnswerKey);
//        log.info("清除标准答案缓存成功，key值:"+standardAnswerKey);

        essayQuestionBase.setDetailId(resultDetail.getId());
        BeanUtils.copyProperties(resultDetail, result);
        EssayQuestionBase resultBase = essayQuestionBaseRepository.save(essayQuestionBase);

        //编辑材料。试卷状态置为（下线，未审核）
        essayPaperBaseRepository.modifyPaperToOffline(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(), EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus(), paperId);

        BeanUtils.copyProperties(resultBase, result);
        result.setPaperId(resultBase.getPaperId());
        result.setQuestionBaseId(resultBase.getId());
        result.setQuestionDetailId(resultBase.getDetailId());
        return result;
    }

    /**
     * 归纳概括题作答时间=试题分值+15分钟；
     * 综合分析题作答时间=试题分值+15分钟；
     * 提出对策题作答时间=试题分值+15分钟；
     * 应用文作答时间=试题分值+20分钟；
     * 议论文作答时间=试题分值+35分钟；
     *
     * @param score
     * @param type
     * @return
     */
    private double getQuestionLimitTime(double score, int type) {
        int limitTime = 0;
        switch (type) {
            case 1:
            case 2:
            case 3: {
                limitTime = new Double(score).intValue() + 15;
                break;
            }
            case 4: {
                limitTime = new Double(score).intValue() + 20;
                break;
            }
            case 5: {
                limitTime = new Double(score).intValue() + 35;
                break;
            }
        }
        limitTime = limitTime * 60;
        return limitTime;
    }

    /**
     * null返回值不做处理，空字符串删除内容，其他修改内容
     *
     * @param content
     * @param userId
     * @return
     */
    public String convertImgAndHtml(String content, String userId) {
        if (StringUtils.isNotBlank(content)) {
            String temp = content.replaceAll("<[^>|(img)]*>", "").trim();
            if (StringUtils.isBlank(temp)) {
                return StringUtils.EMPTY;
            }
            try {
                content = htmlFileUtil.imgManage(content, userId, 0);
            } catch (Exception e) {
                log.error("富文本内容处理失败,{}", content.length() > 10 ? content.substring(10) : content);
                e.printStackTrace();
            }
            content = htmlFileUtil.htmlManage(content);
        }
        return content;
    }

    private long IsExistedSortInPaper(long paperId, int sort) {
        //查询试卷下的现有试题
        List<EssayQuestionBase> questions = essayQuestionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        for (EssayQuestionBase essayQuestionBase : questions) {
            if (sort == essayQuestionBase.getSort()) {
                return essayQuestionBase.getId();
            }
        }
        return -1;
    }

    @Override
    public EssayQuestionBase findQuestionBaseById(long questionBaseId) {
        return essayQuestionBaseRepository.findOne(questionBaseId);
    }


    @Override
    public List<EssayMaterialVO> materialList(long questionBaseId, long paperId) {

        //如果paperId为空，从试卷上取paperId
        if (paperId == 0 && questionBaseId == 0) {
            log.warn("参数异常，题目和试卷id不能同时为空");
        } else {
            if (paperId == 0) {
                EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(questionBaseId);
                if (essayQuestionBase == null) {
                    log.debug("传入查询questionBaseId为空");
                    throw new BizException(EssayErrors.ESSAY_QUESTION_BASE_ID_NULL);
                }
                paperId = essayQuestionBase.getPaperId();
            }
        }

        //根据试卷id查询题目
        List<EssayMaterial> paperMaterialList = essayMaterialRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc
                (paperId, EssayMaterialConstant.EssayMaterialBizStatusEnum.CONNECTED.getBizStatus(), EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());
        List<Long> questionMaterialList = essayQuestionMaterialRepository.findMaterialIdByQuestionBaseIdAndStatusAndBizStatusOrderBySortAsc
                (questionBaseId, EssayMaterialConstant.EssayMaterialBizStatusEnum.CONNECTED.getBizStatus(), EssayMaterialConstant.EssayMaterialStatusEnum.NORMAL.getStatus());
        LinkedList<EssayMaterialVO> essayMaterialVOList = new LinkedList<>();
        //试卷材料不为空
        boolean flag = CollectionUtils.isNotEmpty(questionMaterialList) ? true : false;
        if (CollectionUtils.isNotEmpty(paperMaterialList)) {
            for (EssayMaterial material : paperMaterialList) {
                EssayMaterialVO essayMaterialVO = new EssayMaterialVO();
                essayMaterialVO.setFlag(false);//关联状态（默认false）
                essayMaterialVO.setId(material.getId());
                essayMaterialVO.setSort(material.getSort());
                essayMaterialVO.setContent(material.getContent());
                if (flag && questionMaterialList.contains(material.getId())) {
                    essayMaterialVO.setFlag(true);//关联
                }
                essayMaterialVOList.add(essayMaterialVO);
            }
        }

        return essayMaterialVOList;
    }

    @Override
    public AdminMaterialListVO saveMaterial(AdminMaterialListVO adminMaterialListVO) {
        long questionBaseId = adminMaterialListVO.getQuestionBaseId();
        long paperId = adminMaterialListVO.getPaperId();
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        List<Long> oldIdList = essayQuestionMaterialRepository.findMaterialIdByQuestionBaseIdAndStatusAndBizStatusOrderBySortAsc
                (questionBaseId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus(), EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus());

        if (null == questionBase) {
            log.info("参数异常，查询试题异常");
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        if (CollectionUtils.isNotEmpty(adminMaterialListVO.getMaterialList())) {
            LinkedList<Long> newIdList = new LinkedList<>();
            for (EssayMaterialVO material : adminMaterialListVO.getMaterialList()) {
                if (material.isFlag() && null != material.getId() && material.getId() != 0) {
                    newIdList.add(material.getId());
                }
            }

            if (CollectionUtils.isNotEmpty(oldIdList)) {
                //需要删除的idList(差集)
                List<Long> deleteIdList = (List<Long>) CollectionUtils.disjunction(oldIdList, newIdList);
                if (CollectionUtils.isNotEmpty(deleteIdList)) {
                    essayQuestionMaterialRepository.deleteByQuestionIdAndMaterialIdList(questionBaseId, deleteIdList);
                    //  essayQuestionMaterialRepository.deleteByMaterialIdListAndQuestionId(deleteIdList,questionBaseId);
                }

            }

            for (EssayMaterialVO material : adminMaterialListVO.getMaterialList()) {
                if (material.isFlag()) {//处理用户勾选的材料
                    EssayQuestionMaterial essayQuestionMaterial = new EssayQuestionMaterial();
                    List<EssayQuestionMaterial> questionMaterialList = essayQuestionMaterialRepository.findByQuestionBaseIdAndMaterialId(questionBaseId, material.getId());
                    if (CollectionUtils.isNotEmpty(questionMaterialList)) {
                        essayQuestionMaterial = questionMaterialList.get(0);
                    }

                    EssayMaterial essayMaterial = essayMaterialRepository.findOne(material.getId());
                    essayQuestionMaterial.setMaterialId(material.getId());
                    essayQuestionMaterial.setSort(essayMaterial.getSort());
                    essayQuestionMaterial.setQuestionBaseId(questionBaseId);

                    essayQuestionMaterial.setStatus(EssayQuestionMaterialConstant.EssayQuestionMaterialStatusEnum.NORMAL.getStatus());
                    essayQuestionMaterial.setBizStatus(EssayMaterialConstant.EssayMaterialBizStatusEnum.CONNECTED.getBizStatus());
                    essayQuestionMaterialRepository.save(essayQuestionMaterial);
                }
            }
        }

        //编辑材料。试卷状态置为（下线，未审核）
        essayPaperBaseRepository.modifyPaperToOffline(EssayPaperBaseConstant.EssayPaperBizStatusEnum.OFFLINE.getBizStatus(), EssayPaperBaseConstant.EssayPaperStatusEnum.UN_CHECK.getStatus(), questionBase.getPaperId());
        /**
         * 清空智能批改材料相关缓存
         */
//        String materialKey = RedisKeyConstant.getMaterialKey(questionBaseId);
//        redisTemplate.delete(materialKey);
//        log.info("清除材料缓存成功，key值:"+materialKey);
        String correctMaterialKey = CorrectRedisKeyConstant.getMaterialQuestionKey(questionBaseId);
        redisTemplate.delete(correctMaterialKey);
        log.info("清除试题材料缓存成功（批改用），key值:" + correctMaterialKey);
        String correctMaterialPaperKey = CorrectRedisKeyConstant.getMaterialPaperKey(paperId);
        redisTemplate.delete(correctMaterialPaperKey);
        log.info("清除试卷材料缓存成功（批改用），key值:" + correctMaterialPaperKey);

        return adminMaterialListVO;
    }


    @Override
    public PageUtil<EssayCenterThesisVO> findThesisByCondition(int page, int pageSize, String questionName, long areaId, String year) {

        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtCreate");
        List<Long> detailIds = Lists.newArrayList();
        if (StringUtils.isNotEmpty(questionName)) {
            questionName = questionName.trim();
            detailIds = essayQuestionDetailRepository.findIdByTypeAndStem(5, "%" + questionName + "%");
        }

        Specification specification = querySpecificCenterThesis(areaId, year, detailIds);

        Page all = essayCenterThesisRepository.findAll(specification, pageable);

        List<EssayCenterThesis> list = all.getContent();
        LinkedList<EssayCenterThesisVO> result = new LinkedList<>();
        for (EssayCenterThesis thesis : list) {
            EssayCenterThesisVO vo = new EssayCenterThesisVO();
            BeanUtils.copyProperties(thesis, vo);

            EssayQuestionDetail detail = essayQuestionDetailRepository.findOne(vo.getQuestionDetailId());
            vo.setQuestionName(detail.getStem());
            result.add(vo);
        }
        long totalElements = all.getTotalElements();

        PageUtil p = PageUtil.builder()
                .result(result)
                .next(totalElements > page * pageSize ? 1 : 0)
                .total(totalElements)
                .totalPage((0 == totalElements % pageSize) ? (totalElements / pageSize) : (totalElements / pageSize + 1))
                .build();
        return p;
    }


    private Specification querySpecificCenterThesis(long areaId, String year, List<Long> detailIds) {
        Specification querySpecific = new Specification<EssayCenterThesis>() {
            @Override
            public Predicate toPredicate(Root<EssayCenterThesis> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("status"), EssayCenterThesisConstant.EssayCenterThesisStatusEnum.NORMAL.getStatus()));
                predicates.add(criteriaBuilder.notEqual(root.get("bizStatus"), UNADOPTED.getBizStatus()));
                if (StringUtils.isNotEmpty(year)) {
                    predicates.add(criteriaBuilder.equal(root.get("year"), year));
                }
                if (areaId != -1) {
                    predicates.add(criteriaBuilder.equal(root.get("areaId"), areaId));
                }

                if (CollectionUtils.isNotEmpty(detailIds)) {
                    predicates.add((root.get("questionDetailId").in(detailIds)));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }

    @Override
    public AdminQuestionRelationVO findQuestionRelationInfo(long questionBaseId) {
        AdminQuestionRelationVO result = new AdminQuestionRelationVO();
        result.setQuestionBaseId(questionBaseId);
        if (questionBaseId <= 0) {
            log.error("questionBaseId is illegal ,questionBaseId={}", questionBaseId);
            throw new BizException(EssayErrors.ESSAY_PARAM_ILLEGAL);
        }
        EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        if (essayQuestionBase == null) {
            log.error("no existed questionBase for questionId = {}", questionBaseId);
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_BASE);
        }
        long paperId = essayQuestionBase.getPaperId();
        if (paperId <= 0) {
            log.error("paperId is illegal for question, then questionId = {}", questionBaseId);
            throw new BizException(EssayErrors.ESSAY_QUESTION_BASE_ILLEGAL);
        }
        result.setPaperId(paperId);
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperId);
        if (paperBase == null) {
            log.error("no existed paperBase for paperId = {}", paperId);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }
        result.setPaperName(paperBase.getName());
        List<EssayMaterialVO> materials = materialList(questionBaseId, paperId);
        result.setMaterials(materials);
        return result;
    }

    @Override
    public PageUtil<AdminQuestionVO> findQuestionListByCondition(String stem, String year, long areaId, int type, PageRequest pageable) {
        PageUtil<AdminQuestionVO> questions = findQuestionList(stem, type, year, areaId, pageable);
        return questions;
    }

    @Override
    public String findPaperName(long paperId) {
        EssayPaperBase paperBase = essayPaperBaseRepository.findOne(paperId);
        if (null == paperBase) {
            log.warn("参数异常。试卷id ：" + paperId);
            throw new BizException(EssayErrors.NO_EXISTED_PAPER);
        }
        return paperBase.getName();
    }


    private PageUtil<AdminQuestionVO> findQuestionList(String stem, int type, String year, long areaId, PageRequest pageable) {
        List<EssayQuestionDetail> essayQuestionDetails = findQuestionDetailByStemAndType(stem, type);
        List<AdminQuestionVO> adminQuestions = Lists.newLinkedList();
        if (CollectionUtils.isEmpty(essayQuestionDetails)) {
            PageUtil p = PageUtil.builder()
                    .result(adminQuestions)
                    .build();
            return p;
        }
        final List<Long> detailIds = Lists.newLinkedList();
        final Map<Long, EssayQuestionDetail> detailMap = Maps.newHashMap();
        essayQuestionDetails.forEach(i -> {
            detailIds.add(i.getId());
            detailMap.put(i.getId(), i);
        });
        Page<EssayQuestionBase> pages = findQuestionBaseByYearAndAreaId(year, areaId, detailIds, pageable);
        List<EssayQuestionBase> essayQuestionBases = pages.getContent();

        for (EssayQuestionBase essayQuestionBase : essayQuestionBases) {
            EssayQuestionDetail detail = detailMap.get(essayQuestionBase.getDetailId());
            if (detail != null) {
                adminQuestions.add(AdminQuestionVO.builder().stem(detail.getStem())
                        .questionBaseId(essayQuestionBase.getId())
                        .questionDetailId(essayQuestionBase.getDetailId())
                        .areaId(essayQuestionBase.getAreaId())
                        .areaName(essayQuestionBase.getAreaName())
                        .type(detail.getType())
                        .questionDate(essayQuestionBase.getQuestionDate())
                        .questionYear(essayQuestionBase.getQuestionYear())
                        .build());
            }
        }
        long totalElements = pages.getTotalElements();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        PageUtil p = PageUtil.builder()
                .result(adminQuestions)
                .next(totalElements > (pageNumber + 1) * pageSize ? 1 : 0)
                .total(totalElements)
                .totalPage((0 == totalElements % pageSize) ? (totalElements / pageSize) : (totalElements / pageSize + 1))
                .build();
        return p;
    }

    private List<EssayQuestionDetail> findQuestionDetailByStemAndType(String stem, int type) {
        Specification specification = querySpecific(stem, type);
        return essayQuestionDetailRepository.findAll(specification);
    }

    private Specification querySpecific(String stem, int type) {
        Specification querySpecific = new Specification<EssayQuestionDetail>() {
            @Override
            public Predicate toPredicate(Root<EssayQuestionDetail> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                if (StringUtils.isNotEmpty(stem)) {
                    predicates.add(criteriaBuilder.like(root.get("stem"), "%" + stem + "%"));
                }
                if (-1 != type) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), type));
                }
                predicates.add(criteriaBuilder.notEqual(root.get("status"), EssayPaperBaseConstant.EssayPaperStatusEnum.DELETED.getStatus()));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }

    private Page<EssayQuestionBase> findQuestionBaseByYearAndAreaId(String year, long areaId, List<Long> detailIds, PageRequest pageable) {
        List<Long> areaIds = essayPaperService.findAreaIds(areaId);
        Specification specification = querySpecific(areaIds, year, detailIds, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus(),
                EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus());
        return essayQuestionBaseRepository.findAll(specification, pageable);
    }

    private Specification querySpecific(List<Long> areaIds, String year, List<Long> detailIds, int status, int bizStatus) {
        Specification querySpecific = new Specification<EssayQuestionBase>() {
            @Override
            public Predicate toPredicate(Root<EssayQuestionBase> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                predicates.add((root.get("areaId").isNotNull()));

                predicates.add(criteriaBuilder.notEqual(root.get("areaId"), 0));
                if (StringUtils.isNotEmpty(year)) {
                    predicates.add(criteriaBuilder.equal(root.get("questionYear"), year));
                }
                if (CollectionUtils.isNotEmpty(areaIds)) {
                    predicates.add((root.get("areaId").in(areaIds)));
                }
                if (CollectionUtils.isNotEmpty(detailIds)) {
                    predicates.add((root.get("detailId").in(detailIds)));
                }
                if (0 != status) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                }
//                if (-1 != bizStatus) {
//                    predicates.add(criteriaBuilder.equal(root.get("bizStatus"), bizStatus));
//                }
                predicates.add(criteriaBuilder.notEqual(root.get("status"), EssayPaperBaseConstant.EssayPaperStatusEnum.DELETED.getStatus()));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }


    /**
     * 编辑是否采纳中心论点
     *
     * @param id
     * @param type
     * @return
     */
    @Override
    public EssayUpdateVO adopt(long id, int type) {
        EssayCenterThesis centerThesis = essayCenterThesisRepository.findOne(id);
        if (null == centerThesis) {
            log.warn("中心论点不存在，中心论点ID：{}", id);
            throw new BizException(EssayErrors.CENTER_THESIS_NOT_EXIST);
        }

        //判断是否采纳
        if (EssayCenterThesisConstant.EssayCenterThesisBizStatusEnum.ADOPTED.getBizStatus() == type) {
            //采纳中心论点
            centerThesis.setBizStatus(type);
            //将学员信息保存到标准答案
            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(centerThesis.getQuestionBaseId());
            EssayStandardAnswerKeyPhrase answerKeyPhrase = EssayStandardAnswerKeyPhrase.builder()
                    .item(centerThesis.getContent())
                    .position(0)
                    .questionDetailId(questionBase.getDetailId())
                    .score(0)
                    .type(2)
                    .build();

            answerKeyPhrase.setStatus(1);
            essayStandardAnswerKeyPhraseRepository.save(answerKeyPhrase);

        } else if (UNADOPTED.getBizStatus() == type) {
            //不采纳
            centerThesis.setBizStatus(type);
        } else {
            //类型错误
            log.warn("中心论点编辑类型错误。中心论点ID：{}，编辑类型：{}", id, type);
            throw new BizException(EssayErrors.CENTER_THESIS_NOT_EXIST);
        }
        essayCenterThesisRepository.save(centerThesis);
        return EssayUpdateVO.builder()
                .flag(true)
                .build();
    }


    @Override
    public EssayStandardAnswer saveStandardAnswer(EssayStandardAnswerVO vo) {

        String answerComment = vo.getAnswerComment();
        try {
            answerComment = htmlFileUtil.imgManage(answerComment, "123", 0);
            answerComment = htmlFileUtil.htmlManage(answerComment);
            vo.setAnswerComment(answerComment);
        } catch (Exception e) {
            log.error("富文本内容处理失败");
            e.printStackTrace();
        }
        vo.setStatus(EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        vo.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus());

        EssayStandardAnswer standardAnswer = new EssayStandardAnswer();
        List<String> inscribedNameList = vo.getInscribedNameList();
        StringBuilder inscribedName = new StringBuilder();
        if (CollectionUtils.isNotEmpty(inscribedNameList)) {
            for (String name : inscribedNameList) {
                if (inscribedName.length() > 0) {
                    inscribedName.append("<br/>");
                }
                inscribedName.append(name);
            }
        }

        BeanUtils.copyProperties(vo, standardAnswer);
        if (StringUtils.isNotEmpty(vo.getInscribedName()) && inscribedName.length() == 0) {
            inscribedName.append(vo.getInscribedName());
        }
        standardAnswer.setInscribedName(inscribedName.toString());
        return essayStandardAnswerRepository.save(standardAnswer);
    }

    @Override
    public int delStandardAnswer(long id) {
        EssayStandardAnswer standardAnswer = essayStandardAnswerRepository.findOne(id);
        //清除缓存中的标准答案
//        String standardAnswerKey = RedisKeyConstant.getRefrenceAnswerKey(standardAnswer.getQuestionId());
//        redisTemplate.delete(standardAnswerKey);
//        log.info("清除标准答案缓存成功，key值:"+standardAnswerKey);
        return essayStandardAnswerRepository.updateToDel(id);
    }

    @Override
    public List<EssayStandardAnswer> findStandardAnswer(long questionDetailId) {
        return essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc(questionDetailId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

    }

    @Override
    public AdminQuestionTypeVO getQuestionType(int type) {
        String questionTypeKey = RedisKeyConstant.getQuestionTypeKey(type);
        AdminQuestionTypeVO typeVO = (AdminQuestionTypeVO) redisTemplate.opsForValue().get(questionTypeKey);
        if (typeVO == null) {
            EssayQuestionType essayQuestionType = essayQuestionTypeRepository.findOne((long) type);
            if (null == essayQuestionType) {
                return typeVO;
            }

            List<Long> questionType = new LinkedList<>();
            StringBuilder questionTypeName = new StringBuilder();

            if (essayQuestionType.getPid() != 0) {
                questionType.add(essayQuestionType.getPid());
                EssayQuestionType pQuestionType = essayQuestionTypeRepository.findOne(essayQuestionType.getPid());
                questionTypeName
                        .append(null != pQuestionType ? pQuestionType.getName() : "")
                        .append("-");
            }
            questionTypeName.append(essayQuestionType.getName());
            questionType.add(essayQuestionType.getId());
            typeVO = AdminQuestionTypeVO.builder()
                    .questionType(questionType)
                    .questionTypeName(questionTypeName.toString())
                    .build();
            if (typeVO != null) {
                redisTemplate.opsForValue().set(questionTypeKey, typeVO);
            }
        }

        return typeVO;
    }

    /**
     * 试题绑定视频
     *
     * @param questionId
     * @param videoId
     * @return
     */
    @Override
    public Object boundVideo(Long questionId, Integer videoId) {
        //先校验试题，视频是否存在
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionId);
        if (questionBase == null) {
            log.info("试题ID有误，请检查试题ID后重试,questionId:{}", questionId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }
        YunVideoInfo yunVideoInfo = bjyHandler.getYunVideoInfo(videoId);
        if (yunVideoInfo == null) {
            log.info("视频ID有误，百家云视频信息查询失败。请检查视频ID后重试");
            throw new BizException(EssayErrors.ERROR_VIDEO_ID);

        }

        int upVideoIdById = essayQuestionBaseRepository.upVideoIdById(videoId, questionId);
        if (upVideoIdById != 1) {
            log.info("更新试题解析视频失败。questionId:{},videoId:{}", questionId, videoId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }
        //更新视频的视频标志位
        essayPaperBaseRepository.updateVideoFlag(questionBase.getPaperId(), true);
        String token = bjyHandler.getToken(videoId);

        return YunUtil.getVideoUrl(videoId, token);
    }

    /**
     * 试题取消绑定视频
     *
     * @param questionId
     * @return
     */
    @Override
    public Object cancelBoundVideo(Long questionId) {
        //先校验试题是否存在
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionId);
        if (questionBase == null) {
            log.info("试题ID有误，请检查试题ID后重试,questionId:{}", questionId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }
        questionBase.setVideoId(0);
        //视频ID置为0
        int upVideoIdById = essayQuestionBaseRepository.upVideoIdById(0, questionId);
        if (upVideoIdById != 1) {
            log.info("更新试题解析视频失败。questionId:{},videoId:{}", questionId, 0L);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        }
        List<EssayQuestionBase> questionBaseList = essayQuestionBaseRepository.findByPaperIdAndStatus
                (questionBase.getPaperId(), new Sort(Sort.Direction.ASC, "sort"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isNotEmpty(questionBaseList)) {
            boolean videoAnalyzeFlag = questionBaseList.stream().anyMatch(question -> null != question.getVideoId() && question.getVideoId() > 0);

            //更新视频的视频标志位
            if (!videoAnalyzeFlag) {
                essayPaperBaseRepository.updateVideoFlag(questionBase.getPaperId(), false);
            }
        }
        return null;
    }

    @Override
    public Object setCorrectType(Long questionDetailId, int correctType) {
        EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findById(questionDetailId);
        if (null == questionDetail) {
            log.info("题目id错误，questionDetail：{}", questionDetailId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        } else {
            if (correctType != questionDetail.getCorrectType()) {
                essayPaperService.delQuestionRuleByDetailId(questionDetailId, false);
            }
            questionDetail.setCorrectType(correctType);
        }
        return essayQuestionDetailRepository.save(questionDetail);
    }

    /**
     * @param id
     * @param examScoreMax
     * @param examScoreMin
     * @param inputWordMax
     * @param inputWordMin
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Object getAnswerByConditions(long id, double examScoreMax, double examScoreMin, int inputWordMax, int inputWordMin, int page, int pageSize) {

        Specification specification = querySpecificAnswer(id, examScoreMax, examScoreMin, inputWordMax, inputWordMin);


        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "correctDate");

        Page all = essayQuestionAnswerRepository.findAll(specification, pageable);
        return all;
    }


    private Specification querySpecificAnswer(long questionId, double examScoreMax, double examScoreMin, int inputWordMax, int inputWordMin) {
        Specification querySpecific = new Specification<EssayQuestionAnswer>() {
            @Override
            public Predicate toPredicate(Root<EssayQuestionAnswer> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                predicates.add(criteriaBuilder.equal(root.get("questionBaseId"), questionId));
                predicates.add(criteriaBuilder.equal(root.get("status"), EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus()));
                predicates.add(criteriaBuilder.equal(root.get("bizStatus"), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()));

                if (examScoreMax > 0) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("examScore"), examScoreMax));
                }
                if (examScoreMin > 0) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("examScore"), examScoreMin));
                }

                if (inputWordMax > 0) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("inputWordNum"), inputWordMax));
                }
                if (inputWordMin > 0) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("inputWordNum"), inputWordMin));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }

    @Override
    public void dealQuestionResponseExtendInfo(List<EssayQuestionAnswer> answers, ResponseExtendVO responseExtendVO) {
        EssayQuestionAnswer lastIntelligence = null;
        EssayQuestionAnswer lastManual = null;
        if (CollectionUtils.isEmpty(answers)) {
            responseExtendVO.setCorrectMode(null);
            responseExtendVO.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
            responseExtendVO.setManualRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
            responseExtendVO.setAnswerCardId(null);
            responseExtendVO.setOtherAnswerCardId(null);
            responseExtendVO.setLastType(null);
            return;
        }
        for (EssayQuestionAnswer answer : answers) {
            // 筛选最后答题卡状态
            if (CorrectModeEnum.INTELLIGENCE.getMode() == answer.getCorrectMode()) {
                // 智能批改
                if (lastIntelligence == null || lastIntelligence.getGmtModify().compareTo(answer.getGmtModify()) < 0) {
                    lastIntelligence = answer;
                }
            } else {
                // 人工批改
                if (lastManual == null || lastManual.getGmtModify().compareTo(answer.getGmtModify()) < 0) {
                    lastManual = answer;
                }
            }
        }
        if (lastIntelligence != null) {
            responseExtendVO.setRecentStatus(lastIntelligence.getBizStatus());
        } else {
            responseExtendVO.setRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }
        if (lastManual != null) {
            responseExtendVO.setManualRecentStatus(lastManual.getBizStatus());
        } else {
            responseExtendVO.setManualRecentStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }
        if (null != lastIntelligence && null != lastManual) {
            responseExtendVO.setCorrectMode(lastIntelligence.getGmtModify().compareTo(lastManual.getGmtCreate()) > 0 ? lastIntelligence.getCorrectMode() : lastManual.getCorrectMode());
        } else if (null != lastIntelligence) {
            responseExtendVO.setCorrectMode(lastIntelligence.getCorrectMode());
        } else if (null != lastManual) {
            responseExtendVO.setCorrectMode(lastManual.getCorrectMode());
        }
        Integer lastType = getLastQuestionCardType(lastIntelligence, lastManual);
        if (null != lastType && lastType.equals(CorrectModeEnum.INTELLIGENCE.getMode())) {
            responseExtendVO.setAnswerCardId(null == lastIntelligence ? 0L : lastIntelligence.getId());
            responseExtendVO.setOtherAnswerCardId(null == lastManual ? 0L : lastManual.getId());
        }
        if (null != lastType && lastType.equals(CorrectModeEnum.MANUAL.getMode())) {
            responseExtendVO.setAnswerCardId(null == lastManual ? 0L : lastManual.getId());
            responseExtendVO.setOtherAnswerCardId(null == lastIntelligence ? 0L : lastIntelligence.getId());
        }
        responseExtendVO.setLastType(lastType);
    }

    /**
     * 获取最后一次修改的答题卡类型
     *
     * @param lastIntelligence
     * @param lastManual
     * @return
     */
    private Integer getLastQuestionCardType(EssayQuestionAnswer lastIntelligence, EssayQuestionAnswer lastManual) {
        if (lastIntelligence != null && lastIntelligence
                .getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus()) {
            lastIntelligence = null;
        }
        if (lastManual != null && lastManual.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED
                .getBizStatus()) {
            lastManual = null;
        }
        if (lastIntelligence != null && lastManual != null) {
            if (lastIntelligence.getGmtModify().compareTo(lastManual.getGmtModify()) > 0) {
                return lastIntelligence.getCorrectMode();
            } else {
                return lastManual.getCorrectMode();
            }
        } else if (lastIntelligence == null && lastManual != null) {
            return lastManual.getCorrectMode();
        } else if (lastIntelligence != null && lastManual == null) {
            return lastIntelligence.getCorrectMode();
        }
        return null;
    }
}
