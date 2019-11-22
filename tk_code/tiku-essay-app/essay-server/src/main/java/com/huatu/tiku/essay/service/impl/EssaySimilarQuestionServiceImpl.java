package com.huatu.tiku.essay.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.GuavaKeyConstant;
import com.huatu.tiku.essay.constant.cache.RedisKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.*;
import com.huatu.tiku.essay.entity.*;
import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.essayEnum.CorrectModeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.manager.QuestionManager;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.repository.v2.EssayCorrectImageRepository;
import com.huatu.tiku.essay.service.EssayQuestionMaterialService;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminQuestionTypeVO;
import com.huatu.tiku.essay.vo.admin.AdminSingleQuestionGroupVO;
import com.huatu.tiku.essay.vo.admin.AdminSingleQuestionVO;
import com.huatu.tiku.essay.vo.admin.correct.CorrectImageVO;
import com.huatu.tiku.essay.vo.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 单题组管理
 *
 * @author zhaoxi
 */
@Service
@Slf4j
public class EssaySimilarQuestionServiceImpl implements EssaySimilarQuestionService {
    @Autowired
    EssayQuestionService essayQuestionService;
    @Autowired
    private EssayQuestionDetailRepository essayQuestionDetailRepository;
    @Autowired
    private EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    private EssayPaperBaseRepository essayPaperBaseRepository;
    @Autowired
    private EssayQuestionAnswerRepository essayQuestionAnswerRepository;
    @Autowired
    private EssayMaterialRepository essayMaterialRepository;
    @Autowired
    private EssayQuestionTypeRepository essayQuestionTypeRepository;

    @Autowired
    EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository;

    @Autowired
    EssaySimilarQuestionRepository essaySimilarQuestionRepository;

    @Autowired
    EssayQuestionMaterialService essayQuestionMaterialService;

    @Autowired
    EssayStandardAnswerRepository essayStandardAnswerRepository;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private EssayCorrectImageRepository essayCorrectImageRepository;

    /**
     * 查询单题组信息&&用户作答信息
     */
    private EssayQuestionVO findAreaListBySimilarQuestion(EssaySimilarQuestionGroupInfo similarQuestion, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {

        EssayQuestionVO vo = EssayQuestionVO.builder()
                .similarId(similarQuestion.getId())
                .showMsg(similarQuestion.getShowMsg())
                .build();
        //查询该试题  （所属地区年份试卷列表，存在base表中）（走缓存）
        List<EssayQuestionAreaVO> areaList = findAreaList(similarQuestion);

        List<Long> questionIdList = new LinkedList<Long>();
        areaList.forEach(i -> questionIdList.add(i.getQuestionBaseId()));

        //当前用户该题组批改总交卷次数
        long myCorrectSum = 0;
        if (CollectionUtils.isNotEmpty(questionIdList)) {
            myCorrectSum = essayQuestionAnswerRepository.countByUserIdAndPaperIdAndQuestionBaseIdListAndStatusAndBizStatus(userId, 0, questionIdList, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        }
        if (myCorrectSum == 0) {
            vo.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
        }
        int allCorrectSum = 0;
        for (EssayQuestionAreaVO areaVO : areaList) {
            areaVO.setQuestionDate(null);
            areaVO.setQuestionYear(null);
            areaVO.setPaperId(null);
            areaVO.setLimitTime(null);
            //该题 总交卷次数
            String questionCorrectNumKey = RedisKeyConstant.getQuestionCorrectNumKey(areaVO.getQuestionBaseId());
            Integer correctNum = (Integer) redisTemplate.opsForValue().get(questionCorrectNumKey);
            if (correctNum == null) {
                correctNum = essayQuestionAnswerRepository.countByQuestionBaseIdAndStatusAndBizStatusAndAnswerCardType(areaVO.getQuestionBaseId(),
                        EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                        modeTypeEnum.getType());
                redisTemplate.opsForValue().set(questionCorrectNumKey, correctNum);
                redisTemplate.expire(questionCorrectNumKey, 30, TimeUnit.SECONDS);

            }
            //当前用户该题交卷总次数
            String userQuestionCorrectNumKey = RedisKeyConstant.getUserQuestionCorrectNumKey(areaVO.getQuestionBaseId(), userId);
            Integer myCorrectNum = (Integer) redisTemplate.opsForValue().get(userQuestionCorrectNumKey);
            if (myCorrectNum == null) {
                myCorrectNum = essayQuestionAnswerRepository.countByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType(userId, areaVO.getQuestionBaseId(),
                        0,
                        EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                        modeTypeEnum.getType());
                redisTemplate.opsForValue().set(userQuestionCorrectNumKey, myCorrectNum);
                redisTemplate.expire(userQuestionCorrectNumKey, 30, TimeUnit.SECONDS);

            }

            if (0 != myCorrectNum) {
                areaVO.setCorrectTimes(myCorrectNum);
            } else {
                List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndAnswerCardTypeOrderByGmtModifyDesc(
                        userId,
                        areaVO.getQuestionBaseId(),
                        0,
                        EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        modeTypeEnum.getType());
                if (CollectionUtils.isNotEmpty(questionAnswerList) && null != questionAnswerList.get(0)) {
                    areaVO.setBizStatus(questionAnswerList.get(0).getBizStatus());
                    if (myCorrectSum == 0 && vo.getBizStatus() < areaVO.getBizStatus()) {
                        vo.setBizStatus(areaVO.getBizStatus());
                    }
                } else {
                    areaVO.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
                }
            }
            areaVO.setCorrectSum(correctNum);
            allCorrectSum = allCorrectSum + correctNum;
        }

        vo.setCorrectTimes(myCorrectSum);
        vo.setEssayQuestionBelongPaperVOList(areaList);
        vo.setCorrectSum(allCorrectSum);
        return vo;

    }

    /**
     * 获取题组下试题信息
     *
     * @param similarQuestion
     * @return
     */
    @Override
    public List<EssayQuestionAreaVO> findAreaList(EssaySimilarQuestionGroupInfo similarQuestion) {

        //根据题组id，获取题组下试题信息（走缓存，5分钟失效）
        String questionOfGroupKey = RedisKeyConstant.getQuestionOfGroupKey(similarQuestion.getId());
        List<EssayQuestionAreaVO> essayQuestionAreaVOList = (List<EssayQuestionAreaVO>) redisTemplate.opsForValue().get(questionOfGroupKey);
        if (CollectionUtils.isEmpty(essayQuestionAreaVOList)) {
            //根据similarId查询题组所有试题  走索引
            List<Long> questionIds = essaySimilarQuestionRepository.findQuestionBaseIdBySimilarIdAndStatus(similarQuestion.getId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            essayQuestionAreaVOList = new LinkedList<>();
            //根据相似题目的id查询题目base信息
            LinkedList<EssayQuestionBase> baseList;
            if (CollectionUtils.isNotEmpty(questionIds)) {
                baseList = essayQuestionBaseRepository.findList(questionIds);
                essayQuestionAreaVOList = QuestionManager.changeEssayQuestionBaseToEssayQuestionAreaVO(baseList);
            }
            redisTemplate.opsForValue().set(questionOfGroupKey, essayQuestionAreaVOList);
            redisTemplate.expire(questionOfGroupKey, 5, TimeUnit.MINUTES);
        }

        return essayQuestionAreaVOList;
    }


    /*  guava 机器内存 缓存 数据 */
    public List<EssayMaterialVO> materialListCopies(long questionBaseId) {
        List<EssayMaterialVO> materialList = GuavaKeyConstant.materialListCache.getIfPresent(questionBaseId);
        if (CollectionUtils.isEmpty(materialList)) {
            materialList = new LinkedList<>();
            //根据base试题的ID查询材料ID列表，按照序号升序排列  优先取redis 缓存
            List<EssayQuestionMaterial> essayQuestionMaterialList = essayQuestionMaterialService.getEssayQuestionMaterialList(questionBaseId);
            for (EssayQuestionMaterial material : essayQuestionMaterialList) {
                EssayMaterial essayMaterial = essayMaterialRepository.findOne(material.getMaterialId());
                if (null != essayMaterial) {
                    EssayMaterialVO vo = EssayMaterialVO.builder()
                            .content(essayMaterial.getContent())//材料内容
                            .id(essayMaterial.getId())//材料id
                            .sort(material.getSort())//材料序号
                            .build();

                    materialList.add(vo);
                }
            }
            GuavaKeyConstant.materialListCache.put(questionBaseId, materialList);
        }

        return materialList;
    }

    @Override
    public List<EssayMaterialVO> findMaterialList(long questionBaseId) {
        return materialListCopies(questionBaseId);

    }


    @Override
    public List<EssayQuestionAreaVO> findAreaList(long similarId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        EssaySimilarQuestionGroupInfo similarQuestion = essaySimilarQuestionGroupInfoRepository.findOne(similarId);

        EssayQuestionVO vo = findAreaListBySimilarQuestion(similarQuestion, userId, modeTypeEnum);
        List<EssayQuestionAreaVO> areaList = vo.getEssayQuestionBelongPaperVOList();
        for (int i = 0; i < areaList.size(); i++) {
            EssayQuestionAreaVO area = areaList.get(i);
            //查询批改次数
            int count = essayQuestionAnswerRepository.countByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardType(userId,
                    area.getQuestionBaseId(),
                    0,
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                    modeTypeEnum.getType());
            if (0 != count) {
                area.setCorrectTimes(count);
            } else {
                List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerRepository.findByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndAnswerCardTypeOrderByGmtModifyDesc(
                        userId,
                        area.getQuestionBaseId(),
                        0,
                        EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                        modeTypeEnum.getType());
                if (CollectionUtils.isNotEmpty(questionAnswerList) && null != questionAnswerList.get(0)) {
                    area.setBizStatus(questionAnswerList.get(0).getBizStatus());

                } else {
                    area.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.INIT.getBizStatus());
                }
            }

            area.setCorrectSum(essayQuestionAnswerRepository.countByQuestionBaseIdAndStatusAndBizStatusAndAnswerCardType(area.getQuestionBaseId(),
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus(),
                    modeTypeEnum.getType()));
            areaList.set(i, area);
        }
        return areaList;
    }

    @Override
    public List<EssayQuestionTypeVO> findQuestionType() {
        List<EssayQuestionTypeVO> essayQuestionTypeVOList = (List<EssayQuestionTypeVO>) redisTemplate.opsForValue().get(RedisKeyConstant.SINGLE_QUESTION_TYPE_PREFIX);
        if (CollectionUtils.isNotEmpty(essayQuestionTypeVOList)) {
            return essayQuestionTypeVOList;
        }
        essayQuestionTypeVOList = Lists.newLinkedList();
        List<EssayQuestionType> essayQuestionTypeList = essayQuestionTypeRepository.findByStatusAndBizStatus(EssayQuestionTypeConstant.EssayQuestionTypeStatusEnum.NORMAL.getStatus(), EssayQuestionTypeConstant.EssayQuestionTypeBizStatusEnum.USEFUL.getBizStatus());
        for (EssayQuestionType essayQuestionType : essayQuestionTypeList) {
            EssayQuestionTypeVO essayQuestionTypeVO = new EssayQuestionTypeVO();
            BeanUtils.copyProperties(essayQuestionType, essayQuestionTypeVO);
            essayQuestionTypeVOList.add(essayQuestionTypeVO);
        }
        redisTemplate.opsForValue().set(RedisKeyConstant.SINGLE_QUESTION_TYPE_PREFIX, essayQuestionTypeVOList);
        return essayQuestionTypeVOList;
    }


    @Override
    public AdminSingleQuestionGroupVO updateSingleQuestion(AdminSingleQuestionGroupVO singleQuestionGroupVO) {
        if (singleQuestionGroupVO.getSaveType() == 1) {
            EssaySimilarQuestion essaySimilarQuestion = essaySimilarQuestionRepository.findOne(singleQuestionGroupVO.getRelationId());
            EssaySimilarQuestionGroupInfo essaySimilarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(essaySimilarQuestion.getSimilarId());
            if (essaySimilarQuestionGroupInfo.getBizStatus() == EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()) {
                throw new BizException(EssayErrors.NO_DOWN_SERVER);
            }
        } else if (singleQuestionGroupVO.getSaveType() == 0 || singleQuestionGroupVO.getSaveType() == 3) {
            EssaySimilarQuestionGroupInfo essaySimilarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(singleQuestionGroupVO.getId());
            if (essaySimilarQuestionGroupInfo.getBizStatus() == EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()) {
                throw new BizException(EssayErrors.NO_DOWN_SERVER);
            }
        }

        List<EssaySimilarQuestion> questionBaseList = essaySimilarQuestionRepository.findBySimilarIdAndStatus(singleQuestionGroupVO.getId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
//        List<EssaySimilarQuestion> questionBaseList = null;
        //修改
        int saveType = singleQuestionGroupVO.getSaveType();//操作类型
        long id = singleQuestionGroupVO.getId();//题组id
        long relationId = singleQuestionGroupVO.getRelationId();//待删除的题目id
        if (id <= 0 && relationId <= 0) {
            log.info("参数异常，题组id：{}；题组单题关联id={}", id, relationId);
            throw new BizException(EssayErrors.SIMILAR_QUESTION_GROUP_ID_ERROR);
        }
        //删除单题
        if (AdminQuestionSaveTypeConstant.DELETE_QUESTION == saveType) {
            if (relationId <= 0) {
                log.info("删除单题，relationId参数不能为空");
                throw new BizException(EssayErrors.SIMILAR_QUESTION_RELATION_ID_ERROR);
            }
            EssaySimilarQuestion similarQuestion = essaySimilarQuestionRepository.findOne(relationId);
            if (similarQuestion == null) {
                log.info("单题删除失败，不存在该单题");
                throw new BizException(EssayErrors.NO_EXISTED_SIMILAR_QUESTION);
            }
            similarQuestion.setStatus(EssayQuestionConstant.EssayQuestionStatusEnum.DELETED.getStatus());
            essaySimilarQuestionRepository.save(similarQuestion);

            //如果没有试卷关联这个单题，将单题下线
            EssayQuestionBase essayQuestionBase = essayQuestionBaseRepository.findOne(similarQuestion.getQuestionBaseId());
            if (null != essayQuestionBase) {
                EssayPaperBase essayPaperBase = essayPaperBaseRepository.findOne(essayQuestionBase.getPaperId());
                if (essayPaperBase.getBizStatus() != EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()) {
                    essayQuestionBase.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.OFFLINE.getBizStatus());
                    essayQuestionBaseRepository.save(essayQuestionBase);
                }
            }


//            long count = essaySimilarQuestionRepository.countBySimilarIdAndStatus(similarQuestion.getSimilarId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
//            //题组没有题目，下线题组
//            if (count == 0) {
//                EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(similarQuestion.getSimilarId());
//                similarQuestionGroupInfo.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.OFFLINE.getBizStatus());
//                essaySimilarQuestionGroupInfoRepository.save(similarQuestionGroupInfo);
//            }
//            //删除小题
//            long countUpdate=essaySimilarQuestionRepository.upToDeleteBySimilarId(singleQuestionGroupVO.getRelationId());
            return singleQuestionGroupVO;
        }
        EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(id);
        if (similarQuestionGroupInfo == null) {
            log.error("不存在单题组，similarId={}", id);
            throw new BizException(EssayErrors.NO_EXSITED_ESSAY_SIMILAR_QUESTION);
        }
        //删除题组
        if (AdminQuestionSaveTypeConstant.DELETE_GROUP == saveType) {
            similarQuestionGroupInfo.setStatus(EssayQuestionConstant.EssayQuestionStatusEnum.DELETED.getStatus());
        } else if (AdminQuestionSaveTypeConstant.OFFLINE_GROUP == saveType) {
            //题组下线
            similarQuestionGroupInfo.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.OFFLINE.getBizStatus());
        } else if (AdminQuestionSaveTypeConstant.ONLINE_GROUP == saveType) {
            //题组上线
            similarQuestionGroupInfo.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus());

            //判断单题组下是否存在题目。存在则进行上线操作
//            long count = essaySimilarQuestionRepository.countBySimilarIdAndStatus(id, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(questionBaseList)) {
                log.error("单题组无试题，不能上线");
                throw new BizException(EssayErrors.NO_EXISTED_SIMILAR_QUESTION);
            }

        } else {
            log.info("请求参数错误，操作类型异常");
            throw new BizException(EssayErrors.SIMILAR_QUESTION_SAVE_TYPE_ERROR);
        }
        essaySimilarQuestionGroupInfoRepository.save(similarQuestionGroupInfo);

        //如果单题组上线，相应的单题也做上线处理
        if (CollectionUtils.isNotEmpty(questionBaseList)) {
            for (EssaySimilarQuestion similarQuestion : questionBaseList) {
                EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(similarQuestion.getQuestionBaseId());
                questionBase.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus());
                essayQuestionBaseRepository.save(questionBase);
                //查询试卷下所有试题，清空算法相关缓存
                String standardAnswerKey = RedisKeyConstant.getStandardAnswerKey(questionBase.getDetailId());
                redisTemplate.delete(standardAnswerKey);
                log.info("清除标准答案缓存成功，key值:" + standardAnswerKey);
            }

            /**
             * 0806 生成题目的pdf
             */
            EssayCreatePdfVO pdfVO = EssayCreatePdfVO.builder()
                    .id(id)
                    .type(EssayPdfTypeConstant.SINGLE_QUESTION)
                    .build();
            log.info("发送MQ消息。生成PDF文件id:{},type{}", pdfVO.getId(), pdfVO.getType());
            rabbitTemplate.convertAndSend(SystemConstant.CREATE_PDF_ROUTING_KEY, pdfVO);
        }
        return singleQuestionGroupVO;
    }

    @Override
    public PageUtil findSingleQuestionList(Pageable pageRequest, String title, int type, int bizStatus, long questionId, long groupId) {
        long tempGroupId = -1;
        long totalElements = 0;
        List<AdminSingleQuestionGroupVO> questionList = new LinkedList<>();

        List<Long> groupIdList = new LinkedList<>();
        if (-1 != questionId) {
            groupIdList = essaySimilarQuestionRepository.findGroupIdByQuestionBaseIdAndStatus(questionId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isEmpty(groupIdList)) {
                return PageUtil.builder()
                        .result(questionList)
                        .build();
            }
        }

        Specification specification = querySpecific(title, type, bizStatus, groupId, groupIdList);
        Page<EssaySimilarQuestionGroupInfo> all = essaySimilarQuestionGroupInfoRepository.findAll(specification, pageRequest);
        totalElements = all.getTotalElements();

        if (null != all && CollectionUtils.isNotEmpty(all.getContent())) {
            for (EssaySimilarQuestionGroupInfo similarQuestionGroupInfo : all.getContent()) {
                AdminSingleQuestionGroupVO vo = new AdminSingleQuestionGroupVO();
                BeanUtils.copyProperties(similarQuestionGroupInfo, vo);
                List<EssaySimilarQuestion> similarQuestions = essaySimilarQuestionRepository.findBySimilarIdAndStatus(similarQuestionGroupInfo.getId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
                List<Long> questionBaseIdList = Lists.newLinkedList();
                Map<Long, Long> similarMap = Maps.newHashMap();
                for (EssaySimilarQuestion similarQuestion : similarQuestions) {
                    questionBaseIdList.add(similarQuestion.getQuestionBaseId());
                    similarMap.put(similarQuestion.getQuestionBaseId(), similarQuestion.getId());
                }
                LinkedList<EssayQuestionBase> baseList = new LinkedList<>();
                if (CollectionUtils.isNotEmpty(similarQuestions)) {
                    baseList = essayQuestionBaseRepository.findList(questionBaseIdList);
                }

                LinkedList<AdminSingleQuestionVO> adminSingleQuestionVOList = new LinkedList<>();
                for (EssayQuestionBase questionBase : baseList) {
                    AdminSingleQuestionVO questionVO = new AdminSingleQuestionVO();

                    BeanUtils.copyProperties(questionBase, questionVO);
                    questionVO.setBizStatus(questionBase.getBizStatus());
                    EssayQuestionDetail detail = essayQuestionDetailRepository.findOne(questionBase.getDetailId());
                    questionVO.setTitle(detail.getStem());
                    questionVO.setQuestionBaseId(questionBase.getId());
                    questionVO.setQuestionDetailId(questionBase.getDetailId());
                    if (similarMap.get(questionBase.getId()) != null) {
                        questionVO.setRelationId(similarMap.get(questionBase.getId()));
                    }

                    adminSingleQuestionVOList.add(questionVO);
                }
                vo.setQuestionList(adminSingleQuestionVOList);
                /* ---------------处理题目类型 start ---------------*/
                AdminQuestionTypeVO questionTypeVO = essayQuestionService.getQuestionType(vo.getType());
                //题目类型名称(形式为【上级名称/下级名称】)
                vo.setQuestionTypeName(questionTypeVO.getQuestionTypeName());
                //题目类型(形式为【上级id，下级id】)
                vo.setQuestionType(questionTypeVO.getQuestionType());
                /* ---------------处理题目类型 end ---------------*/
                questionList.add(vo);
            }
        }


        int pageNumber = pageRequest.getPageNumber();
        int pageSize = pageRequest.getPageSize();

        PageUtil p = PageUtil.builder()
                .result(questionList)
                .next(totalElements > (pageNumber + 1) * pageSize ? 1 : 0)
                .total(totalElements)
                .totalPage((0 == totalElements % pageSize) ? (totalElements / pageSize) : (totalElements / pageSize + 1))
                .build();
        return p;

    }

    private Specification querySpecific(String title, int type, int bizStatus, Long groupId, List<Long> groupIdList) {
        Specification querySpecific = new Specification<EssaySimilarQuestionGroupInfo>() {
            @Override
            public Predicate toPredicate(Root<EssaySimilarQuestionGroupInfo> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> predicates = new ArrayList<>();
                if (0 != type) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), type));
                } else {
                    //查询全部时过滤掉议论文
                    predicates.add(criteriaBuilder.notEqual(root.get("type"), 5));
                }
                if (-1 != groupId) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), groupId));
                }
                if (CollectionUtils.isNotEmpty(groupIdList)) {
                    predicates.add((root.get("id").in(groupIdList)));
                }
                if (-1 != bizStatus) {
                    predicates.add(criteriaBuilder.equal(root.get("bizStatus"), bizStatus));
                }
                if (StringUtils.isNotEmpty(title)) {
                    predicates.add(criteriaBuilder.like(root.get("showMsg"), "%" + title + "%"));
                }
                predicates.add(criteriaBuilder.equal(root.get("status"), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()));
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
        return querySpecific;
    }


    /**
     * 单题组保存 修改
     *
     * @param singleQuestionGroupVO
     * @return
     */
    @Override
    public AdminSingleQuestionGroupVO saveSingleQuestion(AdminSingleQuestionGroupVO singleQuestionGroupVO) {


        if (null == singleQuestionGroupVO) {
            log.info("参数错误，请求参数不能为空");
            throw new BizException(EssayErrors.SIMILAR_QUESTION_SAVE_ARGUMENT_ERROR);
        }
        int type = singleQuestionGroupVO.getType();
        if (CollectionUtils.isNotEmpty(singleQuestionGroupVO.getQuestionIdList())) {
            if (type <= 0) {
                log.error("题组类型不能为空");
                throw new BizException(EssayErrors.NO_EXSITED_SIMILAR_GROUP_TYPE);
            }
            LinkedList<EssayQuestionBase> questions = essayQuestionBaseRepository.findList(singleQuestionGroupVO.getQuestionIdList());
            List<Long> detailIds = Lists.newLinkedList();
            Set<Long> detailSets = Sets.newHashSet();
            questions.forEach(i -> {
                detailIds.add(i.getDetailId());
                detailSets.add(i.getDetailId());
            });
            if (singleQuestionGroupVO.getQuestionIdList().size() != detailIds.size()) {
                log.error("单题组中添加重复题目");
                throw new BizException(EssayErrors.INSERT_SAME_QUESTION_IN_SIMILAR_GROUP);
            }
            List<EssayQuestionDetail> details = essayQuestionDetailRepository.findByIdIn(detailIds);
            for (EssayQuestionDetail detail : details) {
                if (type != detail.getType()) {
                    log.error("单题组类型和题目类型不一致");
                    throw new BizException(EssayErrors.TYPE_NO_MATCH_SIMILAR_QUESTION);
                }
            }
        }

        //保存基本信息
        EssaySimilarQuestionGroupInfo similarQuestionGroupInfo;
        if (singleQuestionGroupVO.getId() > 0) {
            //修改
            similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.findOne(singleQuestionGroupVO.getId());
            if (similarQuestionGroupInfo.getBizStatus() == EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()) {
                throw new BizException(EssayErrors.NO_DOWN_SERVER);
            }
            similarQuestionGroupInfo.setId(singleQuestionGroupVO.getId());
            similarQuestionGroupInfo.setShowMsg(singleQuestionGroupVO.getShowMsg());
        } else {
            similarQuestionGroupInfo = EssaySimilarQuestionGroupInfo.builder()
                    .showMsg(singleQuestionGroupVO.getShowMsg())
                    .build();
            AdminQuestionTypeVO questionTypeVO = essayQuestionService.getQuestionType(type);
            List<Long> questionType = questionTypeVO.getQuestionType();
            similarQuestionGroupInfo.setPType(questionType.get(0).intValue());
            similarQuestionGroupInfo.setStatus(EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            similarQuestionGroupInfo.setBizStatus(EssayQuestionConstant.EssayQuestionBizStatusEnum.OFFLINE.getBizStatus());
        }
        if (type <= 0) {
            log.warn("参数错误，题目类型不能为空");
//            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        } else {
            similarQuestionGroupInfo.setType(type);
        }
        similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.save(similarQuestionGroupInfo);

        //遍历题目
        //先将所有题目停用
        int l = essaySimilarQuestionRepository.upToDeleteBySimilarId(similarQuestionGroupInfo.getId());

        if (CollectionUtils.isNotEmpty(singleQuestionGroupVO.getQuestionIdList())) {
            for (Long questionId : singleQuestionGroupVO.getQuestionIdList()) {
                EssaySimilarQuestion similarQuestion = new EssaySimilarQuestion();
                //判断是否之前已经存在关联数据
                if (0 < singleQuestionGroupVO.getId()) {
                    //修改单题组 的话，查询旧数据在判断
                    List<EssaySimilarQuestion> oldSimilarQuestion = essaySimilarQuestionRepository.findByQuestionBaseIdAndSimilarId(questionId, similarQuestionGroupInfo.getId());
                    if (CollectionUtils.isNotEmpty(oldSimilarQuestion)) {
                        similarQuestion = oldSimilarQuestion.get(0);
                        similarQuestion.setStatus(EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
                        essaySimilarQuestionRepository.save(similarQuestion);
                    } else {
                        createSimilarQuestion(questionId, similarQuestionGroupInfo.getId());
                    }
                } else {
                    //新增单题组的话；直接新增数据
                    createSimilarQuestion(questionId, similarQuestionGroupInfo.getId());
                }
            }
        } else {
            log.warn("参数错误，单题组关联题目列表不能为空");
//            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }
        singleQuestionGroupVO.setId(similarQuestionGroupInfo.getId());
        return singleQuestionGroupVO;
    }


    private EssaySimilarQuestion createSimilarQuestion(long questionBaseId, long similarId) {
        EssaySimilarQuestion similarQuestion = EssaySimilarQuestion.builder()
                .questionBaseId(questionBaseId)
                .similarId(similarId)
                .build();
        similarQuestion.setStatus(EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        return essaySimilarQuestionRepository.save(similarQuestion);
    }


    /**
     * 查询试题详情（多个答案兼容单个答案）
     *
     * @param questionBaseId
     * @param userId
     * @param modeTypeEnum
     * @return
     * @throws BizException
     */
    @Override
    public EssayQuestionVO findQuestionDetailV1(long questionBaseId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) throws BizException {
        //查询对应的base信息（取时限）
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        if (null == questionBase) {
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_BASE);
        }
        //根据detailId查询题目详情
        EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionBase.getDetailId());
        List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                (questionBase.getDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(standardAnswerList)) {
            log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + questionBase.getDetailId());
            throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
        }
        EssayStandardAnswer standardAnswer = standardAnswerList.get(0);

        EssayQuestionVO vo = EssayQuestionVO.builder()
                .questionDetailId(questionBase.getDetailId())//试题的detailId
                .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明
                .stem(questionDetail.getStem())//题干信息
                .score(questionDetail.getScore())//题目分数
                .inputWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数
                .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                .limitTime(questionBase.getLimitTime())//答题限时
                .questionBaseId(questionBaseId)//题目的baseId
                .answerComment(standardAnswer.getAnswerComment())//标准答案
                .subTopic(standardAnswer.getSubTopic())
                .topic(standardAnswer.getTopic())
                .callName(standardAnswer.getCallName())
                .correctRule(questionDetail.getCorrectRule())
                //答案类型(0 参考答案  1标准答案)(V1单个答案根据阅卷规则判断)
                .answerFlag(StringUtils.isNotEmpty(questionDetail.getCorrectRule()) ? 1 : 0)
                /**
                 * 临时替换落款日期和落款人字段（解决客户端展示问题）
                 */
                .inscribedDate(standardAnswer.getInscribedName())
                .inscribedName(standardAnswer.getInscribedDate())
                .build();

        List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository.findByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc(userId,
                questionBaseId,
                0,
                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(),
                modeTypeEnum.getType());
        if (CollectionUtils.isNotEmpty(questionAnswers)) {
            EssayQuestionAnswer questionAnswer = questionAnswers.get(0);
            if (null != questionAnswer) {
                //未交卷，返回用户答题信息
                vo.setAnswerCardId(questionAnswer.getId());
                vo.setContent(questionAnswer.getContent());
                vo.setSpendTime(questionAnswer.getSpendTime());
                vo.setInputWordNum(questionAnswer.getInputWordNum());
            }
        }
        return vo;
    }


    /**
     * 查询题目详情（多个答案）
     *
     * @param questionBaseId
     * @param userId
     * @param modeTypeEnum
     * @return
     * @throws BizException
     */
    @Override
    public EssayQuestionVO findQuestionDetailV2(long questionBaseId, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) throws BizException {
        //查询对应的base信息（取时限）
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        if (null == questionBase) {
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_BASE);
        }
        //根据detailId查询题目详情
        EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionBase.getDetailId());
        List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                (questionBase.getDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(standardAnswerList)) {
            log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + questionBase.getDetailId());
            throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
        }
        EssayQuestionVO vo = EssayQuestionVO.builder()
                .questionDetailId(questionBase.getDetailId())//试题的detailId
                .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明
                .stem(questionDetail.getStem())//题干信息
                .score(questionDetail.getScore())//题目分数
                .inputWordNumMax(questionDetail.getInputWordNumMax())//最多录入字数（录入的字数）
                .commitWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数（提交的字数）
                .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                .limitTime(questionBase.getLimitTime())//答题限时
                .questionBaseId(questionBaseId)//题目的baseId
                .build();
        vo.setAnswerList(standardAnswerList);

        List<EssayQuestionAnswer> questionAnswers = essayQuestionAnswerRepository.findByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc(userId,
                questionBaseId,
                0,
                EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(),
                modeTypeEnum.getType());
        if (CollectionUtils.isNotEmpty(questionAnswers)) {
            EssayQuestionAnswer questionAnswer = questionAnswers.get(0);
            if (null != questionAnswer) {
                //未交卷，返回用户答题信息
                vo.setAnswerCardId(questionAnswer.getId());
                vo.setContent(questionAnswer.getContent());
                vo.setSpendTime(questionAnswer.getSpendTime());
                vo.setInputWordNum(questionAnswer.getInputWordNum());
            }
        }
        return vo;
    }

    @Override
    public List<EssayQuestionTypeVO> findQuestionTypeV2() {
        List<EssayQuestionTypeVO> questionTypeTree = findQuestionType();
        return questionTypeTree;
    }


    /*
      查询题目类型树
     */
    public List<EssayQuestionTypeVO> getQuestionTypeTree(long parentId) {
        LinkedList<EssayQuestionTypeVO> questionTypeTree = null;
        //1.查询所有题目类型(走缓存)
        List<EssayQuestionTypeVO> questionTypeAll = findQuestionType();
        //2.根据pid取出所有下级id
        List<EssayQuestionTypeVO> questionTypeList = new LinkedList<>();
        for (EssayQuestionTypeVO questionTypeVO : questionTypeAll) {
            if (questionTypeVO.getPid() == parentId) {
                questionTypeList.add(questionTypeVO);
            }
        }
        //3.迭代查询下级信息
        if (CollectionUtils.isNotEmpty(questionTypeList)) {
            questionTypeTree = new LinkedList<EssayQuestionTypeVO>();
            for (EssayQuestionTypeVO essayQuestionType : questionTypeList) {

                List<EssayQuestionTypeVO> subList = getQuestionTypeTree(essayQuestionType.getId());
                //查询下级
                essayQuestionType.setSubList(subList);
                questionTypeTree.add(essayQuestionType);
            }
        }
        return questionTypeTree;
    }


    /**
     * 根据题目类型查询题目列表
     *
     * @param pageRequest
     * @param type
     * @param normal
     * @return
     */
    @Override
    public List<EssayQuestionVO> findSingleQuestionListV2(Pageable pageRequest, int type, int userId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        ValueOperations<String, List<EssaySimilarQuestionGroupInfo>> valueOperations = redisTemplate.opsForValue();
        //1.根据类目，查类目下题组信息
        //如果有二级类目，查询所有二级类目下的题组信息（走缓存,五分钟失效）
        List<EssaySimilarQuestionGroupInfo> similarQuestionList = valueOperations.get(RedisKeyConstant.getSingleQuestionPrefix(type, pageRequest.getPageNumber(), pageRequest.getPageSize()));
        if (CollectionUtils.isEmpty(similarQuestionList)) {
            similarQuestionList = Lists.newArrayList();
            //查类目信息
            //判断类目下是否存在二级类目（走缓存）
            List<EssayQuestionTypeVO> subList = getQuestionTypeTree(type);
            if (CollectionUtils.isNotEmpty(subList)) {
                for (EssayQuestionTypeVO subType : subList) {
                    List<EssaySimilarQuestionGroupInfo> tempSimilarQuestionList = essaySimilarQuestionGroupInfoRepository.findByBizStatusAndStatusAndType(
                            EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(),
                            EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus(),
                            (int) subType.getId(), pageRequest);
                    similarQuestionList.addAll(tempSimilarQuestionList);
                }

            } else {
                //如果没有二级类目，查询类目下的题组信息（走缓存,五分钟失效）
                similarQuestionList = essaySimilarQuestionGroupInfoRepository.findByBizStatusAndStatusAndType(
                        EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(),
                        EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus(),
                        type, pageRequest);
            }
            valueOperations.set(RedisKeyConstant.getSingleQuestionPrefix(type, pageRequest.getPageNumber(), pageRequest.getPageSize()), similarQuestionList, 300, TimeUnit.SECONDS);
        }

        //对列表进行重新排序（有二级类目的时候，是多个二级类目拼起来的，需要重新排序）
        Collections.sort(similarQuestionList, Comparator.comparing(EssaySimilarQuestionGroupInfo::getShowMsg));

        List list = new ArrayList<EssayQuestionVO>();
        //2.根据题组信息，查题组下具体题目列表
        if (CollectionUtils.isNotEmpty(similarQuestionList)) {
            for (EssaySimilarQuestionGroupInfo similarQuestion : similarQuestionList) {
                //遍历单题组，填充信息（地区&&题干信息）
                EssayQuestionVO vo = findAreaListBySimilarQuestion(similarQuestion, userId, modeTypeEnum);
                list.add(vo);
            }
        }
        return list;

    }

    /**
     * 根据题目类型查询题目个数
     *
     * @return
     */
    @Override
    public long countSingleQuestionByTypeV2(int type) {
        long count = 0L;
        LinkedList<Integer> typeList = new LinkedList<Integer>();
        List<EssayQuestionTypeVO> subList = getQuestionTypeTree(type);
        if (CollectionUtils.isNotEmpty(subList)) {

            subList.forEach(i -> {
                typeList.add((int) i.getId());
            });
            count = essaySimilarQuestionGroupInfoRepository.countByBizStatusAndStatusAndTypeIn
                    (EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus(), typeList);
        } else {
            typeList.add(type);
            count = essaySimilarQuestionGroupInfoRepository.countByBizStatusAndStatusAndTypeIn
                    (EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus(), typeList);
        }

        return count;
    }

    @Override
    public int delQuestion(long questionId) {
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionId);
        if (null == questionBase) {
            log.error("题目id错误，questionId：{}", questionId);
            throw new BizException(EssayErrors.ERROR_QUESTION_ID);
        } else {
            /**
             * 校验状态
             */
            //1.判断所属试卷是否是上线状态
            EssayPaperBase paperBase = essayPaperBaseRepository.findOne(questionBase.getPaperId());

            if (paperBase.getBizStatus() == EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()) {
                log.error("试题所属试卷处于上线状态，paperBaseId:{}", questionBase.getPaperId());
                throw new BizException(EssayErrors.QUESTION_DEL_ERRROR_PAPER_ONLINE);
            }


            //2.判断所属题组是否是上线状态
            List<EssaySimilarQuestion> similarQuestions = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(questionId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            if (CollectionUtils.isNotEmpty(similarQuestions)) {
                long similarId = similarQuestions.get(0).getSimilarId();
                EssaySimilarQuestionGroupInfo groupInfo = essaySimilarQuestionGroupInfoRepository.findOne(similarId);
                if (groupInfo != null && groupInfo.getStatus() == EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus()
                        && groupInfo.getBizStatus() == EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus()) {
                    log.error("试题删除失败。试题所属题组处于上线状态，请先将题组下线再进行操作");
                    throw new BizException(EssayErrors.QUESTION_DEL_ERRROR_QUESTION_GROUP_ONLINE);
                }
            }
            //3。状态正确，更新批注题目位删除状态
            return essayQuestionBaseRepository.updateToDel(questionId);

        }


    }


    /**
     * 发送MQ消息，更新题组信息
     *
     * @param groupId
     */
    @Override
    public void sendSimilarQuestion2Search(Long groupId, int type) {
        EssaySimilarQuestionGroupInfo groupInfo = essaySimilarQuestionGroupInfoRepository.findOne(groupId);
        if (null == groupInfo) {
            log.error("单题组不存在，题组id有误。id = {}", groupId);
            return;
        }

        List<Long> questionIds = essaySimilarQuestionRepository.findQuestionBaseIdBySimilarIdAndStatus(groupId, EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        List<EssayQuestionSearchVO> questionList = new ArrayList<>();
        questionIds.forEach(questionId -> {
            List<EssayMaterialVO> materialList = findMaterialList(questionId);
            List<Map<String, Object>> materialMapList = new LinkedList<>();
            materialList.forEach(vo -> {
                        HashMap<String, Object> materialMap = new HashMap<>();
                        materialMap.put("id", vo.getId());
                        materialMap.put("sort", vo.getSort());
                        materialMap.put("content", vo.getContent());
                        materialMapList.add(materialMap);
                    }
            );
            EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionId);
            long detailId = questionBase.getDetailId();
            EssayQuestionDetail detail = essayQuestionDetailRepository.findById(detailId);
            EssayQuestionSearchVO questionSearchVO = EssayQuestionSearchVO.builder()
                    .sort(questionBase.getSort())
                    .baseId(questionId)
                    .detailId(detailId)
                    .areaId(questionBase.getAreaId())
                    .subAreaId(questionBase.getSubAreaId())
                    .areaName(StringUtils.isNoneEmpty(questionBase.getAreaName()) ? questionBase.getAreaName() : "")
                    .subAreaName(StringUtils.isNoneEmpty(questionBase.getSubAreaName()) ? questionBase.getSubAreaName() : "")
                    .stem(detail.getStem())
                    .materialList(materialMapList)
                    .build();
            questionList.add(questionSearchVO);
        });


        Map map = Maps.newHashMap();
        map.put("index", "essay-question");
        map.put("type", "question");
        //下线操作
        if (type == AdminPaperConstant.UP_TO_OFFLINE) {
            map.put("operation", "delete");
            //上线操作
        } else if (type == AdminPaperConstant.UP_TO_ONLINE) {
            map.put("operation", "save_clean");
        }
        //拼接试卷查询对象
        EssayQuestionGroupSearchVO searchVO = EssayQuestionGroupSearchVO.builder()
                .id(groupId)
                .type(groupInfo.getPType())
                .groupId(groupId)
                .showMsg(groupInfo.getShowMsg())
                .questionList(questionList)
                .build();


        map.put("data", searchVO);
        //新版搜索引擎数据上传
        rabbitTemplate.convertAndSend("pandora_search", "com.ht.essay.search", JSON.toJSONString(map));
        log.info("发送搜索引擎队列信息：{}", JSON.toJSON(map));
    }

    @Override
    public EssayQuestionVO findQuestionDetailV3(long questionBaseId, int userId, int correctMode, Integer bizStatus, Long answerId, EssayAnswerCardEnum.ModeTypeEnum modeTypeEnum) {
        //查询对应的base信息（取时限）
        EssayQuestionBase questionBase = essayQuestionBaseRepository.findOne(questionBaseId);
        EssayAnswerConstant.EssayAnswerBizStatusEnum essayAnswerBizStatusEnum = EssayAnswerConstant.EssayAnswerBizStatusEnum.create(bizStatus);
        if (null == questionBase) {
            throw new BizException(EssayErrors.NO_EXISTED_QUESTION_BASE);
        }
        //根据detailId查询题目详情
        EssayQuestionDetail questionDetail = essayQuestionDetailRepository.findOne(questionBase.getDetailId());
        List<EssayStandardAnswer> standardAnswerList = essayStandardAnswerRepository.findByQuestionIdAndStatusOrderByIdAsc
                (questionBase.getDetailId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(standardAnswerList)) {
            log.warn(EssayErrors.ANSWER_LIST_EMPTY.getMessage() + "，questionDetailId: " + questionBase.getDetailId());
            throw new BizException(EssayErrors.ANSWER_LIST_EMPTY);
        }
        EssayQuestionVO vo = EssayQuestionVO.builder()
                .questionDetailId(questionBase.getDetailId())//试题的detailId
                .answerRequire(questionDetail.getAnswerRequire())//答题要求 文字说明
                .stem(questionDetail.getStem())//题干信息
                .score(questionDetail.getScore())//题目分数
                .inputWordNumMax(questionDetail.getInputWordNumMax())//最多录入字数（录入的字数）
                .commitWordNumMax(questionDetail.getInputWordNumMax())//最多答题字数（提交的字数）
                .inputWordNumMin(questionDetail.getInputWordNumMin())//最少答题字数
                .limitTime(questionBase.getLimitTime())//答题限时
                .questionBaseId(questionBaseId)//题目的baseId
                .correctMode(correctMode)//批改类型
                .questionType(questionDetail.getType())
                .build();
        vo.setAnswerList(standardAnswerList);

        EssayQuestionAnswer questionAnswer = null;
        switch (modeTypeEnum){
            case COURSE:
                questionAnswer = getCourseAnswer(answerId);
                break;
            case NORMAL:
                questionAnswer = getNormalAnswer(answerId,vo,essayAnswerBizStatusEnum,questionBaseId,userId,correctMode);
        }

        if (null != questionAnswer) {
            //未交卷，返回用户答题信息
            vo.setAnswerCardId(questionAnswer.getId());
            vo.setContent(questionAnswer.getContent());
            vo.setSpendTime(questionAnswer.getSpendTime());
            vo.setInputWordNum(questionAnswer.getInputWordNum());

            if (correctMode != CorrectModeEnum.INTELLIGENCE.getMode()) {
                // 需要返回imglist相关信息
                List<CorrectImage> imglist = essayCorrectImageRepository.findByQuestionAnswerIdAndStatusOrderBySort(
                        questionAnswer.getId(), EssayStatusEnum.NORMAL.getCode());
                if (CollectionUtils.isNotEmpty(imglist)) {
                    List<CorrectImageVO> collect = imglist.stream().map(i -> {
                        CorrectImageVO correctImageVO = new CorrectImageVO();
                        BeanUtils.copyProperties(i, correctImageVO);
                        return correctImageVO;
                    }).collect(Collectors.toList());
                    vo.setUserMeta(collect);
                }
            }
        }
        return vo;
    }

    private EssayQuestionAnswer getCourseAnswer(Long answerId) {
        return essayQuestionAnswerRepository.findByIdAndStatus(answerId, EssayAnswerConstant.EssayAnswerStatusEnum.NORMAL.getStatus());
    }

    private EssayQuestionAnswer getNormalAnswer(Long answerId, EssayQuestionVO vo, EssayAnswerConstant.EssayAnswerBizStatusEnum essayAnswerBizStatusEnum, long questionBaseId, int userId, int correctMode) {
        EssayQuestionAnswer questionAnswer = null;
        List<EssayQuestionAnswer> questionAllAnswers = Lists.newArrayList();
        if (null != essayAnswerBizStatusEnum && essayAnswerBizStatusEnum == EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN) {
            if (answerId.longValue() > 0) {
                questionAnswer = essayQuestionAnswerRepository.findOne(answerId);
            }
        } else{
            //智能和人工所有的未完成的试题答题卡
            questionAllAnswers.addAll(essayQuestionAnswerRepository.findByUserIdAndQuestionBaseIdAndPaperIdAndStatusAndBizStatusAndAnswerCardTypeOrderByGmtModifyDesc(userId,
                    questionBaseId,
                    0,
                    EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(),
                    EssayAnswerConstant.EssayAnswerBizStatusEnum.UNFINISHED.getBizStatus(), EssayAnswerCardEnum.ModeTypeEnum.NORMAL.getType()
            ));
            Optional<EssayQuestionAnswer> findFirst = questionAllAnswers.stream().filter(qa -> null != qa.getCorrectMode() && qa.getCorrectMode() == correctMode).findFirst();
            Optional<EssayQuestionAnswer> otherFirst = questionAllAnswers.stream().filter(qa -> null != qa.getCorrectMode() && qa.getCorrectMode() != correctMode).findFirst();
            if (findFirst.isPresent()) {
                questionAnswer = findFirst.get();
            }
            if (otherFirst.isPresent()) {
                vo.setOtherAnswerCardId(otherFirst.get().getId());
            }
        }
        return questionAnswer;
    }

    /**
     * 根据试题ID查询单题组名称
     *
     * @param questionBaseId
     * @return
     */
    public String getSimilarNameByQuestionId(long questionBaseId) {
        List<EssaySimilarQuestion> similarQuestionList = essaySimilarQuestionRepository.findByQuestionBaseIdAndStatus(questionBaseId, EssayMockExamConstant.EssayMockExamStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isNotEmpty(similarQuestionList)) {
            long similarId = similarQuestionList.get(0).getSimilarId();
            EssaySimilarQuestionGroupInfo similarQuestionGroupInfo = essaySimilarQuestionGroupInfoRepository.getOne(similarId);
            if (similarQuestionGroupInfo != null) {
                return similarQuestionGroupInfo.getShowMsg();
            }
        }
        return "";
    }
}
