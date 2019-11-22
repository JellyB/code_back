package com.huatu.tiku.essay.manager;

import com.google.common.collect.Lists;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.entity.EssayTeacherOrderType;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.TeacherOrderTypeEnum;
import com.huatu.tiku.essay.essayEnum.YesNoEnum;
import com.huatu.tiku.essay.repository.EssayTeacherOrderTypeRepository;
import com.huatu.tiku.essay.repository.EssayTeacherRepository;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.vo.req.CreateOrUpdateTeacherReq;
import com.huatu.tiku.essay.vo.req.FetchTeacherReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by duanxiangchao on 2019/7/9
 */
@Slf4j
@Service
public class TeacherManager {

    @Resource
    private EssayTeacherRepository teacherRepository;
    @Resource
    private EssayTeacherOrderTypeRepository teacherOrderTypeRepository;

    @Resource
    private CorrectOrderRepository correctOrderRepository;

    @Autowired
    private EntityManager entityManager;


    public List<EssayTeacher> getAllTeacher() {
        return teacherRepository.findAll();
    }

    public void saveTeacher(EssayTeacher essayTeacher) {
        teacherRepository.save(essayTeacher);
    }

    public void saveTeacher(EssayTeacher essayTeacher, CreateOrUpdateTeacherReq req) {

        if (essayTeacher.getId() != 0) {
            //修改
            teacherOrderTypeRepository.deleteById(essayTeacher.getId());
            req.getCorrectType().forEach(correctType -> {
                TeacherOrderTypeEnum orderTypeEnum = TeacherOrderTypeEnum.create(correctType);
                EssayTeacherOrderType teacherOrderType = teacherOrderTypeRepository.findByTeacherIdAndOrderType(essayTeacher.getId(), correctType);
                if (teacherOrderType == null) {
                    teacherOrderType = new EssayTeacherOrderType();
                    teacherOrderType.setTeacherId(essayTeacher.getId());
                    teacherOrderType.setOrderType(correctType);
                    teacherOrderType.setOrderAmount(0);
                    teacherOrderType.setCurrentFinishAmount(0);
                    teacherOrderType.setDispatchAmount(0);
                    teacherOrderType.setReceiptRate(0);
                }
                switch (orderTypeEnum) {
                    case QUESTION:
                        teacherOrderType.setMaxOrderLimit(req.getQuestionLimit());
                        reCalculateRate(req.getQuestionLimit(), teacherOrderType);
                        break;
                    case ARGUMENT:
                        teacherOrderType.setMaxOrderLimit(req.getArgumentLimit());
                        reCalculateRate(req.getArgumentLimit(), teacherOrderType);
                        break;
                    case PRACTICAL:
                        teacherOrderType.setMaxOrderLimit(req.getPracticalLimit());
                        reCalculateRate(req.getPracticalLimit(), teacherOrderType);
                        break;
                    case SET_QUESTION:
                        teacherOrderType.setMaxOrderLimit(req.getSetQuestionLimit());
                        reCalculateRate(req.getSetQuestionLimit(), teacherOrderType);
                        break;
                }
                if (req.getOrderType().contains(correctType)) {
                    teacherOrderType.setReceiptStatus(YesNoEnum.YES.getValue());
                } else {
                    teacherOrderType.setReceiptStatus(YesNoEnum.NO.getValue());
                }
                teacherOrderType.setStatus(EssayStatusEnum.NORMAL.getCode());
                teacherOrderTypeRepository.save(teacherOrderType);
            });
            Optional.ofNullable(teacherRepository.findOne(essayTeacher.getId()))
                    .ifPresent(i -> {
                                essayTeacher.setTeacherScore(i.getTeacherScore());
                                teacherRepository.save(essayTeacher);
                            }
                    );
        } else {
            teacherRepository.save(essayTeacher);
            if (req.getOrderType().size() > 0) {
                List<EssayTeacherOrderType> teacherOrderTypes = Lists.newArrayList();
                req.getCorrectType().forEach(correctType -> {
                    EssayTeacherOrderType teacherOrderType = new EssayTeacherOrderType();
                    teacherOrderTypes.add(teacherOrderType);
                    teacherOrderType.setTeacherId(essayTeacher.getId());
                    teacherOrderType.setOrderType(correctType);
                    teacherOrderType.setOrderAmount(0);
                    teacherOrderType.setDispatchAmount(0);
                    teacherOrderType.setReceiptRate(0);
                    if (req.getOrderType().contains(correctType)) {
                        teacherOrderType.setReceiptStatus(YesNoEnum.YES.getValue());
                    } else {
                        teacherOrderType.setReceiptStatus(YesNoEnum.NO.getValue());
                    }
                    teacherOrderType.setCurrentFinishAmount(0);
                    teacherOrderType.setStatus(EssayStatusEnum.NORMAL.getCode());
                    TeacherOrderTypeEnum orderTypeEnum = TeacherOrderTypeEnum.create(correctType);

                    switch (orderTypeEnum) {
                        case QUESTION:
                            teacherOrderType.setOrderLimit(req.getQuestionLimit());
                            teacherOrderType.setMaxOrderLimit(req.getQuestionLimit());
                            break;
                        case ARGUMENT:
                            teacherOrderType.setOrderLimit(req.getArgumentLimit());
                            teacherOrderType.setMaxOrderLimit(req.getArgumentLimit());
                            break;
                        case PRACTICAL:
                            teacherOrderType.setOrderLimit(req.getPracticalLimit());
                            teacherOrderType.setMaxOrderLimit(req.getPracticalLimit());
                            break;
                        case SET_QUESTION:
                            teacherOrderType.setOrderLimit(req.getSetQuestionLimit());
                            teacherOrderType.setMaxOrderLimit(req.getSetQuestionLimit());
                            break;
                    }
                });
                teacherOrderTypeRepository.save(teacherOrderTypes);
            }
        }
    }

    /**
     * 重新计算接单比例
     *
     * @param limitCount
     * @param orderType
     */
    public void reCalculateRate(Integer limitCount, EssayTeacherOrderType orderType) {
        Integer orderLimit = orderType.getOrderLimit() == null ? 0 : orderType.getOrderLimit();
        limitCount = limitCount == null ? 0 : limitCount;
        boolean questionFlag = limitCount < orderLimit;
        if (questionFlag) {
            // 管理员设置的批改量小于个人设置的
            orderType.setOrderLimit(limitCount);
            // 修改接单比例
            orderLimit = limitCount == 0 ? 1 : limitCount;
            orderType.setReceiptRate(orderType.getDispatchAmount() * 100 / orderLimit);
        }
    }

    public EssayTeacher getTeacherById(Long teacherId) {
        EssayTeacher essayTeacher = teacherRepository.findOne(teacherId);
        if (essayTeacher == null) {
            throw new BizException(ErrorResult.create(1001, "教师不存在"));
        }
        return essayTeacher;
    }

    public EssayTeacher getTeacherByUCenterId(Long uCenterId) {
        EssayTeacher essayTeacher = teacherRepository.findByUCenterId(uCenterId);
//        if (essayTeacher == null) {
//            throw new BizException(ErrorResult.create(1001, "教师不存在"));
//        }
        return essayTeacher;
    }

    public Page<EssayTeacher> pageTeacher(FetchTeacherReq fetchTeacherReq) {
        PageRequest pageable = new PageRequest(fetchTeacherReq.getPage() - 1, fetchTeacherReq.getPageSize(), Sort.Direction.ASC, "id");
        Specification<EssayTeacher> specification = new Specification<EssayTeacher>() {
            @Override
            public Predicate toPredicate(Root<EssayTeacher> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> list = Lists.newArrayList();
                if (fetchTeacherReq.getTeacherId() != null) {
                    list.add(criteriaBuilder.equal(root.get("id").as(Long.class), fetchTeacherReq.getTeacherId()));
                }
                if (StringUtils.isNotBlank(fetchTeacherReq.getTeacherName())) {
                    list.add(criteriaBuilder.equal(root.get("realName").as(String.class), fetchTeacherReq.getTeacherName()));
                }
                if (fetchTeacherReq.getTeacherStatus() != null) {
                    list.add(criteriaBuilder.equal(root.get("teacherStatus").as(Integer.class), fetchTeacherReq.getTeacherStatus()));
                }
                if (fetchTeacherReq.getCorrectType() != null) {
                    list.add(criteriaBuilder.like(root.get("correctType").as(String.class), "%" + fetchTeacherReq.getCorrectType() + "%"));
                }
                if (fetchTeacherReq.getTeacherLevel() != null) {
                    list.add(criteriaBuilder.equal(root.get("teacherLevel").as(Integer.class), fetchTeacherReq.getTeacherLevel()));
                }
                if (StringUtils.isNotBlank(fetchTeacherReq.getPhoneNum())) {
                    list.add(criteriaBuilder.equal(root.get("phoneNum").as(String.class), fetchTeacherReq.getPhoneNum()));
                }
                Predicate[] p = new Predicate[list.size()];
                return criteriaBuilder.and(list.toArray(p));
            }
        };
        final Page<EssayTeacher> paperList = teacherRepository.findAll(specification, pageable);
        return paperList;
    }

    public List<EssayTeacher> listTeacher(FetchTeacherReq fetchTeacherReq) {
        Specification<EssayTeacher> specification = new Specification<EssayTeacher>() {
            @Override
            public Predicate toPredicate(Root<EssayTeacher> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> list = Lists.newArrayList();
                if (fetchTeacherReq.getTeacherId() != null) {
                    list.add(criteriaBuilder.equal(root.get("id").as(Long.class), fetchTeacherReq.getTeacherId()));
                }
                if (StringUtils.isNotBlank(fetchTeacherReq.getTeacherName())) {
                    list.add(criteriaBuilder.equal(root.get("realName").as(String.class), fetchTeacherReq.getTeacherName()));
                }
                if (fetchTeacherReq.getTeacherStatus() != null) {
                    list.add(criteriaBuilder.equal(root.get("teacherStatus").as(Integer.class), fetchTeacherReq.getTeacherStatus()));
                }
                if (fetchTeacherReq.getCorrectType() != null) {
                    list.add(criteriaBuilder.equal(root.get("correctType").as(Integer.class), fetchTeacherReq.getCorrectType()));
                }
                if (fetchTeacherReq.getTeacherLevel() != null) {
                    list.add(criteriaBuilder.equal(root.get("teacherLevel").as(Integer.class), fetchTeacherReq.getTeacherLevel()));
                }
                if (StringUtils.isNotBlank(fetchTeacherReq.getPhoneNum())) {
                    list.add(criteriaBuilder.equal(root.get("phoneNum").as(String.class), fetchTeacherReq.getPhoneNum()));
                }
                Predicate[] p = new Predicate[list.size()];
                return criteriaBuilder.and(list.toArray(p));
            }
        };
        return teacherRepository.findAll(specification);
    }

    public List<EssayTeacherOrderType> listOrderType(List<Long> teacherIds) {
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Lists.newArrayList();
        }
        return teacherOrderTypeRepository.findByStatusAndTeacherIdIn(EssayStatusEnum.NORMAL.getCode(), teacherIds);
    }

    public List<EssayTeacherOrderType> listOrderType(Long teacherId) {
        return teacherOrderTypeRepository.findByStatusAndTeacherId(EssayStatusEnum.NORMAL.getCode(), teacherId);
    }

    public List<EssayTeacherOrderType> getSettings(String uCentername) {
        EssayTeacher teacher = teacherRepository.findByUCenterName(uCentername);
        if (teacher == null) {
            throw new BizException(ErrorResult.create(1001, "教师不存在"));
        }

        List<EssayTeacherOrderType> teacherTypeList = teacherOrderTypeRepository.findByStatusAndTeacherId(EssayStatusEnum.NORMAL.getCode(), teacher.getId());
        return teacherTypeList;

    }


    public Page<EssayTeacher> pageDistributionTeacher(FetchTeacherReq fetchTeacherReq) {
        PageRequest pageable = new PageRequest(fetchTeacherReq.getPage() - 1, fetchTeacherReq.getPageSize(),
                Sort.Direction.DESC, "id");
        StringBuffer dataSql = new StringBuffer();
        StringBuffer countSql = new StringBuffer();
        String sqlQuery = " DISTINCT(teacher.id),teacher.real_name,teacher.nick_name,teacher.phone_num,teacher.teacher_level,teacher.teacher_status,teacher.department,teacher.area_id,teacher.entry_date,teacher.biz_status,teacher.u_center_id,teacher.u_center_name,teacher.teacher_score,teacher.creator, "
                + " teacher.gmt_create,teacher.email,teacher.bank_name,teacher.bank_branch,teacher.bank_address,teacher.id_card,teacher.bank_user_name,"
                + " teacher.bank_num,teacher.`status`,teacher.gmt_modify,teacher.correct_type,teacher.modifier";
        dataSql.append("SELECT ");
        dataSql.append(sqlQuery);
        dataSql.append(" FROM v_essay_teacher teacher ");
        dataSql.append(" LEFT join  v_essay_teacher_order_type teacher_order_type  on teacher.id=teacher_order_type.teacher_id and teacher_order_type.status = 1");
        dataSql.append(" where teacher.status=1 AND teacher_order_type.receipt_status = 1");

        if (fetchTeacherReq.getTeacherOrdertype() != null) {
            dataSql.append(" and teacher_order_type.order_type = ").append(fetchTeacherReq.getTeacherOrdertype());
        }
        // 手机号 查询
        if (StringUtils.isNoneBlank(fetchTeacherReq.getPhoneNum())) {
            dataSql.append(" and teacher.phone_num = '").append(fetchTeacherReq.getPhoneNum());
            dataSql.append("'");
        }
        //老师姓名查询
        if (StringUtils.isNotEmpty(fetchTeacherReq.getTeacherName())) {
            dataSql.append(" and teacher.real_name like '%");
            dataSql.append(fetchTeacherReq.getTeacherName());
            dataSql.append("%'");
        }

        countSql.append(" SELECT count(1) FROM( ");
        countSql.append(dataSql.toString());
        countSql.append(" ) as a ");
        log.info(dataSql + "");
        log.info("老师列表查询sql是:{}", dataSql.toString());

        Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), EssayTeacher.class);
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        final Object singleResult = countQuery.getSingleResult();

        long totalLong = singleResult == null ? 0L : Long.valueOf(singleResult.toString());

        // 分页数据
        dataQuery.setFirstResult(pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<EssayTeacher> content2 = totalLong > pageable.getOffset() ? dataQuery.getResultList()
                : Collections.<EssayTeacher>emptyList();
        PageImpl<EssayTeacher> teacherPage = new PageImpl<>(content2, pageable, totalLong);

        return teacherPage;
    }

    /**
     * 根据订单操作信息改变老师订单统计数据
     *
     * @param teacherId
     * @param teacherOrderTypeEnum
     * @param operateEnum
     */
    public void updateTeacherCurrentOrder(long teacherId,
                                          TeacherOrderTypeEnum teacherOrderTypeEnum,
                                          CorrectOrderStatusEnum.OperateEnum operateEnum) {
        if (teacherId <= 0) {
            return;
        }
        EssayTeacherOrderType teacherOrderType = teacherOrderTypeRepository.findByTeacherIdAndOrderType(teacherId,
                teacherOrderTypeEnum.getValue());
        if (teacherOrderType != null) {
            checkOrderTypeClearStatus(teacherOrderType);

            Integer currentFinishAmount = teacherOrderType.getCurrentFinishAmount(); // 当天完成数量
            Integer orderLimit = teacherOrderType.getOrderLimit() == null ? 1 : teacherOrderType.getOrderLimit(); // 接单限度
            Integer rate = teacherOrderType.getReceiptRate(); // 已分配比例
            Integer dispatchAmount = teacherOrderType.getDispatchAmount() == null ? 0
                    : teacherOrderType.getDispatchAmount(); // 分配数量
            Integer orderAmount = teacherOrderType.getOrderAmount() == null ? 0 : teacherOrderType.getOrderAmount(); // 总完成数量
            orderLimit = orderLimit == 0 ? 1 : orderLimit;
            switch (operateEnum) {
                case DISPATCH_AUTO:
                case DISPATCH_MANUAL:
                    dispatchAmount++;
                    rate = dispatchAmount * 100 / orderLimit;
                    break;
                case END_CORRECT:
                    currentFinishAmount++;
                    orderAmount++;
                    break;
                case ALLOW_BACK:
                case RECALL:
                case RETURN_USER:
                case RECALL_AUTO:
                    dispatchAmount--;
                    rate = dispatchAmount * 100 / orderLimit;
                    break;
                case INIT:
                case RECEIPT:
                case START_CORRECT:
                case APPLY_BACK:
                case REJECT_BACK:
                case FEED_BACK:

            }
            teacherOrderType.setCurrentFinishAmount(currentFinishAmount);
            teacherOrderType.setReceiptRate(rate);
            teacherOrderType.setOrderAmount(orderAmount);
            teacherOrderType.setDispatchAmount(dispatchAmount);
            teacherOrderType.setGmtModify(new Date());
            teacherOrderTypeRepository.save(teacherOrderType);
        }
    }

    private void checkOrderTypeClearStatus(EssayTeacherOrderType teacherOrderType) {
        Date gmtClear = teacherOrderType.getGmtClear();
        long todayStartMillions = DateUtil.getTodayStartMillions();
        if (gmtClear == null || todayStartMillions > gmtClear.getTime()) {
            clearOrderType(teacherOrderType);
        }
    }

    /**
     * 清空今日批改量和派单量，同时矫正订单累计批改量
     *
     * @param orderType
     */
    public void clearOrderType(EssayTeacherOrderType orderType) {
        orderType.setDispatchAmount(0);
        orderType.setCurrentFinishAmount(0);
        orderType.setReceiptRate(0);
        orderType.setGmtClear(new Date());
        long l = correctOrderRepository.countByReceiveOrderTeacherAndTypeAndBizStatusIn(orderType.getTeacherId(),
                orderType.getOrderType(),
                Lists.newArrayList(CorrectOrderStatusEnum.CORRECTED.getValue(), CorrectOrderStatusEnum.FEEDBACK.getValue()));
        if (orderType.getOrderAmount().intValue() < l) {      //矫正累计批改量
            orderType.setOrderAmount(new Long(l).intValue());
        }
        teacherOrderTypeRepository.save(orderType);
    }
}
