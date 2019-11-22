package com.huatu.tiku.essay.service.impl.courseExercises;

import com.google.common.collect.Lists;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.cache.GuavaKeyConstant;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant.EssayAnswerBizStatusEnum;
import com.huatu.tiku.essay.constant.status.EssayPaperBaseConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.courseExercises.EssayCourseExercisesQuestion;
import com.huatu.tiku.essay.entity.courseExercises.EssayExercisesAnswerMeta;
import com.huatu.tiku.essay.essayEnum.CourseExerciseSearchTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.TeacherOrderTypeEnum;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.repository.courseExercises.EssayCourseExercisesQuestionRepository;
import com.huatu.tiku.essay.repository.courseExercises.EssayExercisesAnswerMetaRepository;
import com.huatu.tiku.essay.service.EssayMaterialService;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.ZtkUserService;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.courseExercises.EssayCourseExercisesService;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.ResponseMsg;
import com.huatu.tiku.essay.vo.admin.courseExercise.*;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.courseExercises.ExercisesListVO;
import com.huatu.tiku.essay.vo.resp.courseExercises.ExercisesListVO.ExercisesItemVO;
import com.huatu.tiku.essay.vo.user.ZtkUserVO;
import com.huatu.ztk.commons.JsonUtil;
import com.rits.cloning.Cloner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.huatu.tiku.essay.constant.error.EssayErrors.COURSE_EXERCISE_EXIST;
import static com.huatu.tiku.essay.constant.error.EssayErrors.PAPER_ONLY_CAN_BIND_ONE;


/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/26
 * @描述
 */
@Service
public class EssayCourseExercisesQuestionServiceImpl implements EssayCourseExercisesService {

    private static final Logger logger = LoggerFactory.getLogger(EssayCourseExercisesQuestionServiceImpl.class);

    @Autowired
    private ZtkUserService ztkUserService;

    @Autowired
    private EssayCourseExercisesQuestionRepository courseExercisesQuestionRepository;

    @Autowired
    private EssayExercisesAnswerMetaRepository exercisesAnswerMetaRepository;

    @Autowired
    private EssayQuestionService essayQuestionService;

    @Autowired
    private EssayQuestionAnswerService essayQuestionAnswerService;

    @Autowired
    private EssaySimilarQuestionService essaySimilarQuestionService;


    @Autowired
    private EssayQuestionDetailRepository questionDetailRepository;

    @Autowired
    private EssayPaperBaseRepository paperBaseRepository;

    @Autowired
    private EssayQuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private EntityManager entityManager;


    @Autowired
    private EssayPaperAnswerRepository paperAnswerRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EssayMaterialService essayMaterialService;

    @Autowired
    private EssayQuestionBaseRepository questionBaseRepository;

    @Value("${updateQuestionCountUrl}")
    private String updateQuestionCountUrl;

    @Autowired
    private EssayQuestionDetailRepository essayQuestionDetailRepository;

    @Autowired
    private CorrectOrderService correctOrderService;

    private Cloner cloner = new Cloner();

    @Override
    public Object getRefundMoney(String userName, long courseId) {
        ZtkUserVO user = ztkUserService.getByUsernameOrderMobile(userName);
        if (user == null) {
            throw new BizException(EssayErrors.USER_NOT_EXIST);
        }

        // 查询已做试题信息
        List<EssayExercisesAnswerMeta> answerMetaList = exercisesAnswerMetaRepository
                .findByCourseIdAndUserIdAndStatus(courseId, user.getId().intValue(), EssayStatusEnum.NORMAL.getCode());
        double expendPrice = answerMetaList.stream()
                .filter(meta -> meta.getBizStatus() >= EssayAnswerBizStatusEnum.COMMIT.getBizStatus() && meta.getBizStatus() != EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus())
                .mapToDouble(answerMeta -> {
                    List<EssayCourseExercisesQuestion> exercisesQuestions = courseExercisesQuestionRepository
                            .findByCourseWareIdAndCourseTypeAndTypeAndStatusAndPQid(answerMeta.getCourseWareId(),
                                    answerMeta.getCourseType(), answerMeta.getAnswerType(),
                                    EssayStatusEnum.NORMAL.getCode(), answerMeta.getPQid());
                    if (!CollectionUtils.isEmpty(exercisesQuestions)) {

                        return exercisesQuestions.get(0).getCorrectPrice();
                    }
                    return 0;
                }).sum();
        return expendPrice;
    }


    /**
     * 待绑定试题列表
     *
     * @return
     */
    @Override
    public PageUtil<Object> getQuestionList(AdminCourseExerciseSearchVo adminCourseExerciseSearchVo) {
        if (adminCourseExerciseSearchVo.getType() == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
            return getSearchQuestionList(adminCourseExerciseSearchVo);
        } else if (adminCourseExerciseSearchVo.getType() == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            return getSearchPaperList(adminCourseExerciseSearchVo);
        } else {
            throw new BizException(EssayErrors.COURSE_EXERCISE_TYPE_WRONG);
        }
    }


    /**
     * 查询试题信息
     *
     * @return
     */
    private PageUtil<Object> getSearchQuestionList(AdminCourseExerciseSearchVo adminCourseExerciseSearchVo) {

        PageRequest pageable = new PageRequest(adminCourseExerciseSearchVo.getPage() - 1, adminCourseExerciseSearchVo.getPageSize(), Sort.Direction.DESC, "id");
        StringBuffer dataSql = new StringBuffer();
        StringBuffer countSql = new StringBuffer();
        StringBuffer titleSql = new StringBuffer();

        titleSql.append(" question.id,question.area_id,question.question_year,question.sort," +
                "question.paper_id,question.detail_id,question.limit_time,question.area_name," +
                "question.question_date,question.biz_status,question.creator,question.gmt_create,question.modifier," +
                "question.gmt_modify,question.status,question.pdf_path,question.pdf_size," +
                "question.sub_area_id,question.sub_area_name,question.area_sort,question.is_lack,question.video_id," +
                "question.download_count ");

        dataSql.append("SELECT ");
        dataSql.append(titleSql);
        dataSql.append(" FROM v_essay_question_base question");
        dataSql.append(" LEFT JOIN v_essay_question_detail detail ON question.detail_id =detail.id");
        dataSql.append(" WHERE question.status=1  and detail.real_question=1 and question.area_id>0");//只查询真题
        dataSql.append(" and question.biz_status=");
        dataSql.append(EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus());
        //年份
        if (null != adminCourseExerciseSearchVo.getYear()) {
            dataSql.append(" and question.question_year=");
            dataSql.append(adminCourseExerciseSearchVo.getYear());
        }
        //地区
        if (null != adminCourseExerciseSearchVo.getAreaId()) {
            dataSql.append("  and  question.area_id =");
            dataSql.append(adminCourseExerciseSearchVo.getAreaId());
        }
        //试题类型
        if (null != adminCourseExerciseSearchVo.getQuestionType()) {
            dataSql.append(" and detail.type=");
            dataSql.append(adminCourseExerciseSearchVo.getQuestionType());
        }

        //搜索类型
        if (null != adminCourseExerciseSearchVo.getSearchType() && StringUtils.isNotEmpty(adminCourseExerciseSearchVo.getSearchContent())) {
            if (adminCourseExerciseSearchVo.getSearchType() == CourseExerciseSearchTypeEnum.QUESTION_ID.getValue()) {
                checkSearchContent(adminCourseExerciseSearchVo.getSearchContent());
                dataSql.append(" and  question.id =");
                dataSql.append(adminCourseExerciseSearchVo.getSearchContent());
            }
            if (adminCourseExerciseSearchVo.getSearchType() == CourseExerciseSearchTypeEnum.QUESTION_CONTENT.getValue()) {
                dataSql.append("  and detail.stem like '%");
                dataSql.append(adminCourseExerciseSearchVo.getSearchContent());
                dataSql.append("%'");
            }
        }
        dataSql.append("  order by question.id desc");
        countSql.append(" SELECT count(1) FROM( ");
        countSql.append(dataSql.toString());
        countSql.append(" ) as a ");

        logger.info("dataSql执行结果:{},countSql执行结果:{}", dataSql.toString(), countSql.toString());

        Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), EssayQuestionBase.class);
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        final Object singleResult = countQuery.getSingleResult();
        long totalLong = singleResult == null ? 0L : Long.valueOf(singleResult.toString());
        // 分页数据
        dataQuery.setFirstResult(pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<EssayQuestionBase> content2 = totalLong > pageable.getOffset() ? dataQuery.getResultList()
                : Collections.<EssayQuestionBase>emptyList();
        PageImpl<EssayQuestionBase> correctOrderPage = new PageImpl<>(content2, pageable, totalLong);
        List<EssayQuestionBase> questionBases = correctOrderPage.getContent();

        List<EssayQuestionBase> questionBasesList = filterSelectedQuestion(adminCourseExerciseSearchVo, questionBases);
        List<AdminCourseExerciseQuestionVO> voList = questionBasesList.stream().map(questionBase -> {
            return buildCourseExerciseQuestionVo(questionBase);
        }).collect(Collectors.toList());

        // 设置分页
        long totalElements = correctOrderPage.getTotalElements();
        PageUtil resultPageUtil = PageUtil.builder().result(voList)
                .totalPage(0 == totalElements / adminCourseExerciseSearchVo.getPageSize() ? totalElements / adminCourseExerciseSearchVo.getPageSize() : totalElements / adminCourseExerciseSearchVo.getPageSize() + 1)
                .next(pageable.getPageNumber()).total(totalElements).build();
        return resultPageUtil;
    }

    public void checkSearchContent(String searchContent) {
        String searchPattern = "[0-9]";
        Pattern pattern = Pattern.compile(searchPattern);
        Matcher matcher = pattern.matcher(searchContent);
        if (matcher.find() == false) {
            throw new BizException(EssayErrors.SEARCH_CONTENT_MUST_NUMBER);
        }
    }

    /**
     * 过滤已经选择试题
     *
     * @param adminCourseExerciseSearchVo
     * @param questionBases
     * @return
     */
    private List<EssayQuestionBase> filterSelectedQuestion(AdminCourseExerciseSearchVo adminCourseExerciseSearchVo, List<EssayQuestionBase> questionBases) {
        if (CollectionUtils.isNotEmpty(questionBases)) {
            List<EssayCourseExercisesQuestion> userSelectedQuestion = courseExercisesQuestionRepository.findByCourseWareIdAndCourseTypeAndTypeAndStatus(adminCourseExerciseSearchVo.getCourseWareId(),
                    adminCourseExerciseSearchVo.getCourseType(), EssayAnswerCardEnum.TypeEnum.QUESTION.getType(),
                    EssayStatusEnum.NORMAL.getCode());

            if (CollectionUtils.isNotEmpty(userSelectedQuestion)) {
                List<Long> questionIdList = userSelectedQuestion.stream().map(EssayCourseExercisesQuestion::getPQid).collect(Collectors.toList());
                return questionBases.stream().filter(question -> !questionIdList.contains(question.getId()))
                        .collect(Collectors.toList());
            }
        }
        return questionBases;
    }

    /**
     * 过滤已经选择的套卷
     *
     * @param adminCourseExerciseSearchVo
     * @param paperBaseList
     * @return
     */
    public List<EssayPaperBase> filterSelectedPaper(AdminCourseExerciseSearchVo adminCourseExerciseSearchVo, List<EssayPaperBase> paperBaseList) {
        if (CollectionUtils.isNotEmpty(paperBaseList)) {

            List<EssayCourseExercisesQuestion> userSelectPapers = courseExercisesQuestionRepository.findByCourseWareIdAndCourseTypeAndTypeAndStatus(adminCourseExerciseSearchVo.getCourseWareId(),
                    adminCourseExerciseSearchVo.getCourseType(), EssayAnswerCardEnum.TypeEnum.PAPER.getType(), EssayStatusEnum.NORMAL.getCode());
            if (CollectionUtils.isNotEmpty(userSelectPapers)) {
                return paperBaseList.stream().filter(paperBase -> !paperBaseList.contains(paperBase.getId()))
                        .collect(Collectors.toList());
            }
        }
        return paperBaseList;
    }

    /**
     * 查询试卷信息
     */
    public PageUtil<Object> getSearchPaperList(AdminCourseExerciseSearchVo adminCourseExerciseSearchVo) {

        Integer searchType = adminCourseExerciseSearchVo.getSearchType();
        String searchContent = adminCourseExerciseSearchVo.getSearchContent();

        int page = adminCourseExerciseSearchVo.getPage();
        int pageSize = adminCourseExerciseSearchVo.getPageSize();
        //查询套卷的
        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "id");
        Specification<EssayPaperBase> specification = new Specification<EssayPaperBase>() {
            @Override
            public Predicate toPredicate(Root<EssayPaperBase> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                List<Predicate> list = new ArrayList<>();
                //年份
                if (null != adminCourseExerciseSearchVo.getYear()) {
                    list.add(builder.equal(root.get("paperYear").as(String.class), adminCourseExerciseSearchVo.getYear()));
                }
                //地区
                if (null != adminCourseExerciseSearchVo.getAreaId()) {
                    list.add(builder.equal(root.get("areaId").as(Long.class), adminCourseExerciseSearchVo.getAreaId()));
                }
                //搜索类型
                if (null != searchType && StringUtils.isNotEmpty(searchContent)) {
                    if (searchType == CourseExerciseSearchTypeEnum.PAPER_ID.getValue()) {
                        checkSearchContent(adminCourseExerciseSearchVo.getSearchContent());
                        list.add(builder.equal(root.get("id").as(Long.class), searchContent));
                    }
                    if (searchType == CourseExerciseSearchTypeEnum.PAPER_CONTENT.getValue()) {
                        //试卷名称
                        list.add(builder.like(root.get("name").as(String.class), searchContent));
                    }
                }
                list.add(builder.equal(root.get("status").as(Integer.class), EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus()));
                list.add(builder.equal(root.get("bizStatus").as(Integer.class), EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus()));
                list.add(builder.equal(root.get("type").as(Integer.class), 1));
                Predicate[] p = new Predicate[list.size()];
                return builder.and(list.toArray(p));
            }
        };
        Page<EssayPaperBase> paperBasePage = paperBaseRepository.findAll(specification, pageable);
        Long totalElements = paperBasePage.getTotalElements();
        List<EssayPaperBase> paperBaseList = paperBasePage.getContent();
        //过滤已经选择的套卷
        List<EssayPaperBase> paperBaseResult = filterSelectedPaper(adminCourseExerciseSearchVo, paperBaseList);

        List<AdminCourseExerciseQuestionVO> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(paperBaseList)) {
            voList = paperBaseResult.stream().map(paperBase -> {
                return buildCourseExercisePaperVo(paperBase);
            }).collect(Collectors.toList());

        }
        PageUtil resultPageUtil = PageUtil.builder().result(voList)
                .totalPage(0 == totalElements / pageSize ? totalElements / pageSize : totalElements / pageSize + 1)
                .next(pageable.getPageNumber()).total(totalElements).build();
        return resultPageUtil;
    }


    /**
     * 组转套卷展示数据
     *
     * @param paperBase
     * @return
     */
    private AdminCourseExerciseQuestionVO buildCourseExercisePaperVo(EssayPaperBase paperBase) {
        if (null != paperBase) {
            AdminCourseExerciseQuestionVO vo = AdminCourseExerciseQuestionVO.builder().paperId(paperBase.getId())
                    .stem(paperBase.getName())
                    .source(paperBase.getName()).build();
            //材料
            List<String> materialContentList = new ArrayList<>();
            List<EssayMaterialVO> materials = essayMaterialService.findMaterialsByPaperId(paperBase.getId());
            if (CollectionUtils.isNotEmpty(materials)) {
                materials.sort(Comparator.comparing(EssayMaterialVO::getSort));
                materialContentList = materials.stream()
                        .filter(material -> StringUtils.isNotEmpty(material.getContent()) == true)
                        .map(EssayMaterialVO::getContent)
                        .collect(Collectors.toList());
            }
            vo.setMaterials(materialContentList);
            //训练量
            Long answerCount = paperAnswerRepository.countByPaperBaseIdAndStatus(paperBase.getId(),
                    EssayStatusEnum.NORMAL.getCode());
            vo.setAnswerCount(answerCount.intValue());
            //答题要求
            List<String> answerRequires = getAnswerRequires(paperBase.getId());
            if (CollectionUtils.isNotEmpty(answerRequires)) {
                vo.setAnswerRequire(answerRequires);
            } else {
                vo.setAnswerRequire(Lists.newArrayList());
            }
            vo.setExerciseType(EssayAnswerCardEnum.TypeEnum.PAPER.getType());
            return vo;
        }
        return null;

    }

    /**
     * 获取答题要求
     *
     * @param paperId
     * @return
     */
    private List<String> getAnswerRequires(Long paperId) {
        List<EssayQuestionBase> questions = questionBaseRepository.findByPaperIdAndStatusOrderBySortAsc(paperId,
                EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
        if (CollectionUtils.isEmpty(questions)) {
            return null;
        }
        List<Long> detailIds = questions.stream().map(EssayQuestionBase::getDetailId).distinct().collect(Collectors.toList());
        List<EssayQuestionDetail> details = questionDetailRepository.findByIdIn(detailIds);
        if (CollectionUtils.isEmpty(details)) {
            return null;
        }
        List<String> answerRequireList = details.stream()
                .filter(detail -> StringUtils.isNotEmpty(detail.getAnswerRequire()) == true)
                .map(EssayQuestionDetail::getAnswerRequire).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(answerRequireList)) {
            return answerRequireList;
        }
        return null;
    }


    /**
     * 组装展示数据
     *
     * @param questionBase
     * @return
     */
    private AdminCourseExerciseQuestionVO buildCourseExerciseQuestionVo(EssayQuestionBase questionBase) {
        AdminCourseExerciseQuestionVO vo = new AdminCourseExerciseQuestionVO();
        //试题ID
        vo.setQuestionId(questionBase.getId());
        //来源
        EssayPaperBase paperBase = paperBaseRepository.findByIdAndBizStatusAndStatus(questionBase.getPaperId(),
                EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(),
                EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus());
        if (null != paperBase) {
            vo.setSource(paperBase.getName());
        }
        //题干,答题要求
        EssayQuestionDetail questionDetail = questionDetailRepository.findById(questionBase.getDetailId());
        if (null != questionDetail) {
            vo.setStem(questionDetail.getStem());

            vo.setAnswerRequire(Lists.newArrayList(questionDetail.getAnswerRequire()));
        }
        //训练量
        int answerCount = questionAnswerRepository.countByQuestionBaseIdAndStatusAndBizStatus(questionBase.getId(), EssayStatusEnum.NORMAL.getCode(),
                EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus());
        vo.setAnswerCount(answerCount);
        //获取材料
        List<String> materialContentList = new ArrayList<>();
        List<EssayMaterialVO> materialList = essaySimilarQuestionService.findMaterialList(questionBase.getId());
        if (CollectionUtils.isNotEmpty(materialList)) {
            materialList.sort(Comparator.comparing(EssayMaterialVO::getSort));
            materialContentList = materialList.stream()
                    .filter(material -> StringUtils.isNotEmpty(material.getContent()) == true)
                    .map(EssayMaterialVO::getContent).collect(Collectors.toList());
        }
        vo.setMaterials(materialContentList);
        vo.setExerciseType(EssayAnswerCardEnum.TypeEnum.QUESTION.getType());
        return vo;
    }


    /**
     * 保存课后作业
     */
    public Object saveCourseExercise(AdminCourseExercisesRepVo courseExercisesRepVo) {

        List<EssayCourseExercisesQuestion> essayCourseExercisesQuestionList = courseExercisesQuestionRepository.findByCourseWareIdAndCourseTypeAndPQidAndStatus(
                courseExercisesRepVo.getCourseWareId(), courseExercisesRepVo.getCourseType(),
                courseExercisesRepVo.getPaperOrQuestionId(), EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isNotEmpty(essayCourseExercisesQuestionList)) {
            throw new BizException(COURSE_EXERCISE_EXIST);
        }
        //课后练习个数
        Long afterExercisesNum = courseExercisesQuestionRepository.countByCourseWareIdAndCourseTypeAndTypeAndStatus(courseExercisesRepVo.getCourseWareId(),
                courseExercisesRepVo.getCourseType(), courseExercisesRepVo.getType(), EssayStatusEnum.NORMAL.getCode());

        if (courseExercisesRepVo.getType() == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            //套卷只能绑定一套
            if (afterExercisesNum >= 1) {
                throw new BizException(PAPER_ONLY_CAN_BIND_ONE);
            }
        }

        EssayCourseExercisesQuestion courseExercisesQuestion = EssayCourseExercisesQuestion.builder()
                .courseWareId(courseExercisesRepVo.getCourseWareId())//课件ID
                .pQid(courseExercisesRepVo.getPaperOrQuestionId())
                .correctMode(courseExercisesRepVo.getCorrectMode())
                .courseType(courseExercisesRepVo.getCourseType())
                .type(courseExercisesRepVo.getType())
                .sort(afterExercisesNum == null ? 0 : afterExercisesNum.intValue() + 1)
                .correctPrice(courseExercisesRepVo.getCorrectPrice())
                .build();
        courseExercisesQuestion.setStatus(EssayStatusEnum.NORMAL.getCode());

        //获取地区信息
        if (courseExercisesRepVo.getType() == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
            EssayQuestionBase essayQuestion = questionBaseRepository.findByIdAndStatus(courseExercisesRepVo.getPaperOrQuestionId(), EssayStatusEnum.NORMAL.getCode());
            if (null != essayQuestion) {
                courseExercisesQuestion.setQuestionDetailId(essayQuestion.getDetailId());
                courseExercisesQuestion.setAreaId(essayQuestion.getAreaId());
                courseExercisesQuestion.setAreaName(essayQuestion.getAreaName());
            }
        } else if (courseExercisesRepVo.getType() == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            //查询试卷ID
            EssayPaperBase essayPaperBase = paperBaseRepository.findOne(courseExercisesRepVo.getPaperOrQuestionId());
            if (null != essayPaperBase) {
                courseExercisesQuestion.setAreaId(essayPaperBase.getAreaId());
                courseExercisesQuestion.setAreaName(essayPaperBase.getAreaName());
            }
        }
        //通知php数量
        noticePHPUpdateCourseNum(afterExercisesNum.intValue() + 1, courseExercisesRepVo.getCourseWareId(),
                courseExercisesRepVo.getCourseType(), courseExercisesRepVo.getType());
        courseExercisesQuestionRepository.save(courseExercisesQuestion);

        return courseExercisesQuestion.getId();
    }

    /**
     * 通知PHP更新绑定课后练习题信息
     *
     * @param afterExercisesNum
     * @param courseWardId
     * @param courseType
     * @param questionType
     */

    @Override
    public void noticePHPUpdateCourseNum(int afterExercisesNum, Long courseWardId, int courseType, int questionType) {
        AdminUpdateExerciseNumReqVO exerciseNumReqVO = AdminUpdateExerciseNumReqVO.builder()
                .afterExercisesNum(afterExercisesNum)
                .classId(courseWardId)//课件ID
                .courseType(courseType)
                .subjectType(2) //1 申论 2申论科目
                .buildType(questionType)//0单题 1套题
                .build();
        logger.info("上报参数是:{}", JsonUtil.toJson(exerciseNumReqVO));
        ResponseMsg responseMsg = restTemplate.postForObject(updateQuestionCountUrl, exerciseNumReqVO, ResponseMsg.class);
        if (responseMsg.getCode() != 10000) {
            logger.info("更新php失败", responseMsg.getMsg());
        }
    }

    /**
     * 获取已绑定课后作业列表
     *
     * @param courseWareId 课件ID
     * @return
     */
    public List<AdminCourseExerciseQuestionVO> getSelectedQuestionList(Long courseWareId, int courseType) {

        List<EssayCourseExercisesQuestion> questionList = courseExercisesQuestionRepository.findByCourseWareIdAndCourseTypeAndStatusOrderBySort(courseWareId, courseType, EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isEmpty(questionList)) {
            return Lists.newArrayList();
        }
        List<AdminCourseExerciseQuestionVO> courseExerciseQuestionVOList = questionList.stream().map(exerciseQuestion -> {

            AdminCourseExerciseQuestionVO questionVO = new AdminCourseExerciseQuestionVO();
            if (exerciseQuestion.getType() == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
                EssayQuestionBase essayQuestionBase = questionBaseRepository.findByIdAndStatus(exerciseQuestion.getPQid(), EssayStatusEnum.NORMAL.getCode());
                // EssayQuestionBase essayQuestionBase = questionBaseRepository.findOne(exerciseQuestion.getPQid());
                if (null != essayQuestionBase) {
                    questionVO = buildCourseExerciseQuestionVo(essayQuestionBase);
                }
            } else {
                EssayPaperBase essayPaperBase = paperBaseRepository.findByIdAndBizStatusAndStatus(exerciseQuestion.getPQid(),
                        EssayPaperBaseConstant.EssayPaperBizStatusEnum.ONLINE.getBizStatus(),
                        EssayPaperBaseConstant.EssayPaperStatusEnum.CHECK_PASS.getStatus());
                //EssayPaperBase essayPaperBase = paperBaseRepository.findOne(exerciseQuestion.getPQid());
                if (null != essayPaperBase) {
                    questionVO = buildCourseExercisePaperVo(essayPaperBase);
                }
            }
            questionVO.setSort(exerciseQuestion.getSort());
            questionVO.setCorrectPrice(exerciseQuestion.getCorrectPrice());
            questionVO.setId(exerciseQuestion.getId());
            return questionVO;
        }).collect(Collectors.toList());
        return courseExerciseQuestionVOList;
    }

    /**
     * 编辑课后作业
     *
     * @param exerciseEditVos
     * @return
     */
    public Object editCourseExercise(List<AdminCourseExerciseEditVo> exerciseEditVos) {

        if (CollectionUtils.isNotEmpty(exerciseEditVos)) {
            exerciseEditVos.stream().forEach(vo -> {
                courseExercisesQuestionRepository.updateQuestionSort(vo.getSort(),
                        vo.getId(), EssayStatusEnum.NORMAL.getCode());
            });
        }
        return null;
    }

    /**
     * 删除课后作业
     *
     * @param id
     * @return
     */
    public Object delCourseExercise(Long id) {

        EssayCourseExercisesQuestion courseExercisesQuestion = courseExercisesQuestionRepository.findByIdAndStatus(id, EssayStatusEnum.NORMAL.getCode());
        if (null == courseExercisesQuestion) {
            throw new BizException(EssayErrors.ID_WRONG);
        }
        //课后练习个数
        Long afterExercisesNum = courseExercisesQuestionRepository.countByCourseWareIdAndCourseTypeAndTypeAndStatus(courseExercisesQuestion.getCourseWareId(),
                courseExercisesQuestion.getCourseType(), courseExercisesQuestion.getType(), EssayStatusEnum.NORMAL.getCode());
        //添加校验
        courseExercisesQuestionRepository.updateById(id, EssayStatusEnum.DELETED.getCode());

        //通知php更新数量
        noticePHPUpdateCourseNum(afterExercisesNum.intValue() - 1, courseExercisesQuestion.getCourseWareId(),
                courseExercisesQuestion.getCourseType(), courseExercisesQuestion.getType());
        return SuccessMessage.create("撤销成功!");
    }

    @Override
    public ExercisesListVO getCourseExerciseQuestionList(Integer userId, Long courseWareId, Integer courseType, Long syllabusId) {
        List<ExercisesItemVO> exercisesItemList = GuavaKeyConstant.EXERCISESITEMVOCACHE.getIfPresent(courseWareId + "_" + courseType);
        if (CollectionUtils.isEmpty(exercisesItemList)) {
            exercisesItemList = new ArrayList<>();
            List<EssayCourseExercisesQuestion> questionList = courseExercisesQuestionRepository
                    .findByCourseWareIdAndCourseTypeAndStatusOrderBySort(courseWareId, courseType, EssayStatusEnum.NORMAL.getCode());

            for (EssayCourseExercisesQuestion question : questionList) {
                EssayQuestionDetail questionDetail = essayQuestionService
                        .findQuestionDetailById(question.getQuestionDetailId());
                ExercisesItemVO build = ExercisesItemVO.builder().questionBaseId(question.getPQid())
                        .questionType(questionDetail.getType()).sort(question.getSort())
                        .score(questionDetail.getScore()).stem(questionDetail.getStem())
                        .questionDetailId(question.getQuestionDetailId()).areaId(question.getAreaId())
                        .areaName(question.getAreaName()).build();
                exercisesItemList.add(build);
            }
            GuavaKeyConstant.EXERCISESITEMVOCACHE.put(courseWareId + "_" + courseType, exercisesItemList);
        }

        // 此处只可能为多题
        Integer finishedCount = 0;
        List<EssayExercisesAnswerMeta> answerMetaList = exercisesAnswerMetaRepository
                .findBySyllabusIdAndUserIdAndStatus(syllabusId, userId, EssayStatusEnum.NORMAL.getCode());
        Map<Long, EssayExercisesAnswerMeta> answerMetaListMap = answerMetaList.stream().collect(Collectors
                .toMap(answerMeta -> answerMeta.getPQid(), answerMeta -> answerMeta, (answerMeta1, answerMeta2) -> {
                    if (answerMeta1.getCorrectNum() > answerMeta2.getCorrectNum()) {
                        return answerMeta1;
                    } else {
                        return answerMeta2;
                    }
                }));
        List<ExercisesItemVO> exercisesItemListCopy = cloner.deepClone(exercisesItemList);

        for (ExercisesItemVO exercisesItem : exercisesItemListCopy) {
            EssayExercisesAnswerMeta answerMeta = answerMetaListMap.get(exercisesItem.getQuestionBaseId());
            if (answerMeta == null || answerMeta.getBizStatus() == EssayAnswerBizStatusEnum.INIT.getBizStatus()) {
                // 未作答题数
                exercisesItem.setBizStatus(EssayAnswerBizStatusEnum.INIT.getBizStatus());
            } else {
                // 做过课后作业
                Long questionAnswerId = answerMeta.getAnswerId();
                EssayQuestionAnswer questionAnswer = essayQuestionAnswerService.findById(questionAnswerId);
                if (null != questionAnswer) {
                    exercisesItem.setBizStatus(questionAnswer.getBizStatus());
                    exercisesItem.setInputWordNum(questionAnswer.getInputWordNum());
                    exercisesItem.setExamScore(questionAnswer.getExamScore());
                    exercisesItem.setSpendtime(questionAnswer.getSpendTime());
                    exercisesItem.setQuestionAnswerId(questionAnswer.getId());
                    if (questionAnswer.getBizStatus() == EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus()) {
                        // 查询被退回内容
                        CorrectOrder order = correctOrderService.findByAnswerId(questionAnswer.getId(),
                                EssayAnswerCardEnum.TypeEnum.QUESTION);
                        String baseReturn = "本次人工批改申请因『%s』被驳回，如需继续申请批改请修改后重新提交。";
                        exercisesItem.setCorrectMemo(String.format(baseReturn, order.getCorrectMemo()));
                    } else if (questionAnswer.getBizStatus() == EssayAnswerBizStatusEnum.COMMIT.getBizStatus() ||
                            questionAnswer.getBizStatus() == EssayAnswerBizStatusEnum.CORRECTING.getBizStatus()) {
                        CorrectOrder order = correctOrderService.findByAnswerId(questionAnswer.getId(),
                                EssayAnswerCardEnum.TypeEnum.QUESTION);
                        if (null != order) {
                            exercisesItem.setClickContent(
                                    TeacherOrderTypeEnum.reportContent(
                                            TeacherOrderTypeEnum.convert(exercisesItem.getQuestionType()),
                                            order.getDelayStatus()));
                        } else {
                            exercisesItem.setClickContent(StringUtils.EMPTY);
                        }

                    }
                    if (questionAnswer.getBizStatus() == EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
                        finishedCount++;
                    }
                }
                exercisesItem.setCorrectNum(answerMeta.getCorrectNum());
            }
        }
        ExercisesListVO exercisesListVO = ExercisesListVO.builder().total(exercisesItemList.size())
                .finishedCount(finishedCount).exercisesItemList(exercisesItemListCopy).build();

        return exercisesListVO;
    }


    /**
     * @param courseWareId 课件ID
     * @return
     */
    public Object cleanBindData(Long courseWareId, int courseType) {
        return courseExercisesQuestionRepository.updateStatus(courseWareId, courseType, EssayStatusEnum.DELETED.getCode());
    }


}
