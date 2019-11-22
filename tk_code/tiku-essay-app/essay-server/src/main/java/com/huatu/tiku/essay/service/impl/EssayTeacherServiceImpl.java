package com.huatu.tiku.essay.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ht.base.start.security.module.base.UserDetails;
import com.ht.base.start.security.service.UserOption;
import com.ht.base.user.module.security.UserInfo;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.common.utils.date.DateFormatUtil;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.entity.EssayTeacherOrderType;
import com.huatu.tiku.essay.entity.correct.CorrectFeedBack;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.CorrectFeedBackEnum;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum;
import com.huatu.tiku.essay.essayEnum.DepartmentEnum;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.SettlementStatusEnum;
import com.huatu.tiku.essay.essayEnum.TeacherLevelEnum;
import com.huatu.tiku.essay.essayEnum.TeacherOrderTypeEnum;
import com.huatu.tiku.essay.essayEnum.TeacherStatusEnum;
import com.huatu.tiku.essay.manager.AreaManager;
import com.huatu.tiku.essay.manager.CorrectOrderManager;
import com.huatu.tiku.essay.manager.QuestionManager;
import com.huatu.tiku.essay.manager.TeacherManager;
import com.huatu.tiku.essay.repository.EssayTeacherOrderTypeRepository;
import com.huatu.tiku.essay.repository.EssayTeacherRepository;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.repository.v2.EssayCorrectFeedBackRepository;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.v2.question.QuestionTypeService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.admin.TeacherWrapper;
import com.huatu.tiku.essay.util.date.DateCompareUtil;
import com.huatu.tiku.essay.util.date.DateUtil;
import com.huatu.tiku.essay.vo.req.CreateOrUpdateTeacherReq;
import com.huatu.tiku.essay.vo.req.FetchTeacherReq;
import com.huatu.tiku.essay.vo.req.TeacherOrderTypeReq;
import com.huatu.tiku.essay.vo.resp.CorrectOrderVo;
import com.huatu.tiku.essay.vo.resp.TeacherDetailVo;
import com.huatu.tiku.essay.vo.resp.TeacherSalaryVo;
import com.huatu.tiku.essay.vo.resp.TeacherVo;
import com.huatu.tiku.essay.vo.resp.UCenterTeacherVo;
import com.huatu.tiku.essay.vo.teacher.CorrectOrderQuery;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by duanxiangchao on 2019/7/10
 */
@Slf4j
@Service
public class EssayTeacherServiceImpl implements EssayTeacherService {

    @Resource
    private TeacherManager teacherManager;
    @Resource
    private CorrectOrderManager correctOrderManager;
    @Resource
    private QuestionManager questionManager;
    @Resource
    private AreaManager areaManager;
    @Resource
    private UserOption userOption;
    @Value("${teacher.user-center-role-id}")
    private Long userCenterRoleId;

    @Autowired
    private EssayTeacherRepository essayTeacherRepository;

    @Autowired
    EssayCorrectFeedBackRepository correctFeedBackRepository;

    @Autowired
    private EssayTeacherOrderTypeRepository teacherOrderTypeRepository;

    @Autowired
    private CorrectOrderRepository correctOrderRepository;

    @Autowired
    private QuestionTypeService questionTypeService;

    @Override
    public List<UCenterTeacherVo> listTeacher(String name) {
        List<EssayTeacher> essayTeachers = teacherManager.getAllTeacher();
        List<Long> uCenterIds = Lists.newArrayList();
        essayTeachers.forEach(essayTeacher -> {
            uCenterIds.add(essayTeacher.getUCenterId());
        });
        List<UCenterTeacherVo> teacherVos = Lists.newArrayList();
        userOption.getUserByRoles(new Long[]{userCenterRoleId}).forEach(userInfo -> {
			log.info("待添加的老师信息:{}", userInfo.getUsername());
            if ((!uCenterIds.contains(userInfo.id))
                    && (StringUtils.isBlank(name)
                    || (StringUtils.isNotBlank(userInfo.getName()) && userInfo.getName().contains(name))
                    || (StringUtils.isNotBlank(userInfo.getUsername()) && userInfo.getUsername().contains(name)))) {
                UCenterTeacherVo teacherVo = new UCenterTeacherVo();
                teacherVo.setUCenterId(userInfo.getId());
                teacherVo.setUCenterName(userInfo.getUsername());
                teacherVo.setRealName(userInfo.getName());
                teacherVo.setPhoneNum(userInfo.getPhone() == null? "" : userInfo.getPhone().toString());
                teacherVos.add(teacherVo);
            }
        });
        return teacherVos;
    }

    @Override
    public Long addOrUpdateTeacher(CreateOrUpdateTeacherReq teacherReq) {

        if (teacherReq.getTeacherId() == 0) {
            EssayTeacher essayTeacher = teacherManager.getTeacherByUCenterId(teacherReq.getUCenterId());
            if (essayTeacher != null) {
                throw new BizException(ErrorResult.create(1100000, "ucenterId已经添加"));
            }
        }
        EssayTeacher essayTeacher = new EssayTeacher();
        essayTeacher.setId(teacherReq.getTeacherId());
        essayTeacher.setUCenterId(teacherReq.getUCenterId());
        essayTeacher.setUCenterName(teacherReq.getUCenterName());
        essayTeacher.setRealName(teacherReq.getRealName());
        essayTeacher.setNickName(teacherReq.getNickName());
        essayTeacher.setPhoneNum(teacherReq.getPhoneNum());
        essayTeacher.setEmail(teacherReq.getEmail());
        essayTeacher.setTeacherLevel(teacherReq.getTeacherLevel());
        essayTeacher.setTeacherStatus(teacherReq.getTeacherStatus());
        essayTeacher.setDepartment(teacherReq.getDepartment());
        essayTeacher.setAreaId(teacherReq.getAreaId());
        if (teacherReq.getEntryDate() != null) {
            essayTeacher.setEntryDate(new Date(teacherReq.getEntryDate()));
        }
        essayTeacher.setBankName(teacherReq.getBankName());
        essayTeacher.setBankBranch(teacherReq.getBankBranch());
        essayTeacher.setBankAddress(teacherReq.getBankAddress());
        essayTeacher.setIdCard(teacherReq.getIdCard());
        essayTeacher.setBankUserName(teacherReq.getBankUserName());
        essayTeacher.setBankNum(teacherReq.getBankNum());
        if (teacherReq.getCorrectType().size() > 0) {
            StringBuffer stringBuffer = new StringBuffer("");
            teacherReq.getCorrectType().forEach(correctType -> {
                stringBuffer.append(correctType + ",");
            });
            essayTeacher.setCorrectType(stringBuffer.substring(0, stringBuffer.length() - 1));
        } else {
            essayTeacher.setCorrectType("");
        }
        essayTeacher.setStatus(EssayStatusEnum.NORMAL.getCode());
        teacherManager.saveTeacher(essayTeacher, teacherReq);
        return essayTeacher.getId();
    }

    @Override
    public PageUtil<List<TeacherVo>> fetchTeacher(FetchTeacherReq fetchTeacherReq) {
        Page<EssayTeacher> teachers = teacherManager.pageTeacher(fetchTeacherReq);
        PageUtil packageTeacherInfo = packageTeacherInfo(teachers);
		return packageTeacherInfo;
    }

    @Override
    public TeacherDetailVo teacherDetail(Long teacherId) {

        EssayTeacher teacher = teacherManager.getTeacherById(teacherId);
        List<EssayTeacherOrderType> teacherOrderTypes = teacherManager.listOrderType(teacherId);
        TeacherDetailVo teacherDetailVo = TeacherWrapper.wrapperToTeacherDetailVo(teacher, teacherOrderTypes);
        AreaManager.AreaTree areaTree = areaManager.getAreaTree();
        teacherDetailVo.setAreaText(areaTree.getAreaNameMap().get(teacher.getAreaId()));
        teacherDetailVo.setAreaIds(areaTree.getAreaIdsMap().get(teacher.getAreaId()));
        return teacherDetailVo;
    }

    public PageUtil<List<CorrectOrderVo>> fetchCorrectOrder(Long teacherId, Integer page, Integer pageSize) {
        if (teacherId == null) {
            UserInfo userInfo = userOption.getUserInfo();
            EssayTeacher essayTeacher = teacherManager.getTeacherByUCenterId(userInfo.getId());
            if (essayTeacher == null) {
                return new PageUtil<List<CorrectOrderVo>>(Collections.emptyList());
            }
            teacherId = essayTeacher.getId();
        }
        CorrectOrderQuery query = new CorrectOrderQuery();
        query.setReceiveOrderTeacher(teacherId);
        query.setPage(page);
        query.setPageSize(pageSize);
        Page<CorrectOrder> correctOrders = correctOrderManager.pageCorrectOrder(query);
        List<CorrectOrderVo> correctRecords = Lists.newArrayList();
        if (correctOrders.getTotalElements() > 0) {
            Map<Long, CorrectOrder> orderMap = Maps.newHashMap();
            List<Long> questionAnswerIds = Lists.newArrayList();
            List<Long> paperAnswerIds = Lists.newArrayList();
            List<Long> answerIds = Lists.newArrayList();
            List<Long> alreadyFeedBackOrders = Lists.newArrayList();
            //  答题卡id  任务id
            Map<Long, Long> orderCardMap = Maps.newHashMap();
            correctOrders.getContent().forEach(correctOrder -> {
                if (correctOrder.getAnswerCardType() == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
                    questionAnswerIds.add(correctOrder.getAnswerCardId());
                } else {
                    paperAnswerIds.add(correctOrder.getAnswerCardId());
                }
                answerIds.add(correctOrder.getAnswerCardId());
                orderCardMap.put(correctOrder.getId(), correctOrder.getAnswerCardId());
                orderMap.put(correctOrder.getAnswerCardId(), correctOrder);
                if(correctOrder.getFeedBackStatus() == CorrectFeedBackEnum.YES.getCode()){
                    alreadyFeedBackOrders.add(correctOrder.getId());
                }
            });

            Map<Long, String> contentMap = Maps.newHashMap();
            // 散题
            List<EssayQuestionAnswer> questionAnswers = correctOrderManager.listQuestionAnswer(questionAnswerIds);
            List<Long> questionIds = Lists.newArrayList();
            // questionId  cardId
            Map<Long, Long> cardQuestionMap = Maps.newHashMap();
            questionAnswers.forEach(essayQuestionAnswer -> {
                questionIds.add(essayQuestionAnswer.getQuestionDetailId());
                cardQuestionMap.put(essayQuestionAnswer.getId(), essayQuestionAnswer.getQuestionDetailId());
            });
            List<EssayQuestionDetail> questionDetails = questionManager.listQuestionDetail(questionIds);
            Map<Long, String> questionContentMap = Maps.newHashMap();
            questionDetails.forEach(essayQuestionDetail -> {
                questionContentMap.put(essayQuestionDetail.getId(), essayQuestionDetail.getStem());
            });

            // 套题
            List<EssayPaperAnswer> paperAnswers = correctOrderManager.listPaperAnswer(paperAnswerIds);
            Map<Long, String> paperContentMap = Maps.newHashMap();
            paperAnswers.forEach(essayPaperAnswer -> {
                paperContentMap.put(essayPaperAnswer.getId(), essayPaperAnswer.getName());
            });

            correctOrders.getContent().forEach(correctOrder -> {
                if (correctOrder.getAnswerCardType() == 0) {
                    //散题
                    contentMap.put(correctOrder.getId(), questionContentMap.get(cardQuestionMap.get(orderCardMap.get(correctOrder.getId()))));
                } else {
                    //套题
                    contentMap.put(correctOrder.getId(), paperContentMap.get(orderCardMap.get(correctOrder.getId())));
                }
            });

            //评价
            List<CorrectFeedBack> feedBacks = correctOrderManager.listFeedBackByOrderId(alreadyFeedBackOrders);
            Map<Long, CorrectFeedBack> feedBackMap = Maps.newHashMap();
            feedBacks.forEach(correctFeedBack -> feedBackMap.put(correctFeedBack.getOrderId(), correctFeedBack));
            correctOrders.forEach(order -> {
                CorrectOrderVo record = new CorrectOrderVo();
                correctRecords.add(record);
                record.setTaskId(order.getId());
                record.setTaskType(TeacherOrderTypeEnum.create(order.getType()).getTitle());
                record.setQuestionContent(contentMap.get(order.getId()));
                record.setOrderStatus(CorrectOrderStatusEnum.create(order.getBizStatus()).getTitle());
                if (order.getEndTime() != null && order.getCorrectTime() != null) {
                    record.setUsedTime(DateCompareUtil.getDuration(order.getCorrectTime(), order.getEndTime()));
                    record.setFinishTime(DateFormatUtil.DEFAULT_FORMAT.format(order.getEndTime()));
                }
                record.setActualSalary(TeacherOrderTypeEnum.create(order.getType()).getSalary());
                record.setSettlementStatus(SettlementStatusEnum.create(order.getSettlementStatus()).getTitle());
                CorrectFeedBack feedBack = feedBackMap.get(order.getId());
                if (feedBack != null) {
                    record.setTeacherScore(feedBack.getStar());
                    record.setComment(feedBack.getContent());
                }
            });
        }
        return new PageUtil<List<CorrectOrderVo>>(correctRecords, correctOrders);
    }

    @Override
    public UserInfo getUserInfo() {

        if (null == SecurityContextHolder.getContext() || null == SecurityContextHolder.getContext().getAuthentication()) {
        	 throw new BizException(ErrorResult.create(1100000, "登录信息无效"));
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUserInfo();
        }
        throw new BizException(ErrorResult.create(1100000, "登录信息无效"));
    }

    @Override
    public Object getSettings(String uCentername) {
        return teacherManager.getSettings(uCentername);
    }

    @Override
    public EssayTeacher findById(long teacherId) {
        return essayTeacherRepository.findByIdAndStatus(teacherId, EssayStatusEnum.NORMAL.getCode());
    }

    @Override
    public Integer updateTeacherSettings(List<TeacherOrderTypeReq> teacherOrderTypeReqList) {
        UserInfo userInfo = getUserInfo();
        if (userInfo == null) {
            throw new BizException(EssayErrors.LOGIN_INVALID_ERROR);
        }
        EssayTeacher teacher = essayTeacherRepository.findByUCenterName(userInfo.getUsername());
        List<EssayTeacherOrderType> orderTypelist = Lists.newArrayList();
		teacherOrderTypeReqList.forEach(teacherOrderType -> {
			EssayTeacherOrderType findOne = teacherOrderTypeRepository.findOne(teacherOrderType.getId());
			if (findOne != null && teacher.getId() == findOne.getTeacherId()) {
				int max = findOne.getMaxOrderLimit() == null ? 0 : findOne.getMaxOrderLimit();
				int orderLimit = teacherOrderType.getOrderLimit() == null ? 0 : teacherOrderType.getOrderLimit();
				if (orderLimit > max) {
					throw new BizException(EssayErrors.ERROR_TEACHER_INFO_ORDERLIMIT_ERROR);
				}
				BeanUtils.copyProperties(teacherOrderType, findOne);
			}
			orderTypelist.add(findOne);
		});
        teacherOrderTypeRepository.save(orderTypelist);
        return EssayStatusEnum.NORMAL.getCode();
    }

    @Override
    public List<TeacherSalaryVo> getSalaryList(Long teacherId, Date startDate, Date endDate) {
        List<Object[]> result = correctOrderRepository.getSalaryList(teacherId, startDate, endDate);

        Map<Integer, Long> resultDic = Maps.newHashMap();

        result.forEach(data -> {
            resultDic.put((int) data[0], (long) data[1]);
        });

        List<TeacherSalaryVo> salaryVos = Lists.newArrayList();

        TeacherSalaryVo total = new TeacherSalaryVo();
        total.setLabel("汇总");

        salaryVos.add(total);

        int countTotal = 0;
        int totalMoneyTotal = 0;

        for (TeacherOrderTypeEnum teacherOrderTypeEnum : TeacherOrderTypeEnum.values()) {
            TeacherSalaryVo teacherSalaryVo = new TeacherSalaryVo();
            teacherSalaryVo.setLabel(teacherOrderTypeEnum.getTitle());

            Long count = resultDic.get(teacherOrderTypeEnum.getValue());

            teacherSalaryVo.setCount(count == null ? 0 : count.intValue());

            teacherSalaryVo.setTotalMoney(teacherSalaryVo.getCount() * teacherOrderTypeEnum.getSalary());

            salaryVos.add(teacherSalaryVo);

            countTotal += teacherSalaryVo.getCount();
            totalMoneyTotal += teacherSalaryVo.getTotalMoney();
        }

        total.setCount(countTotal);
        total.setTotalMoney(totalMoneyTotal);

        return salaryVos;
    }

    @Override
    public boolean checkCanCorrect(Integer orderType) {
        //根据接单比例小于100并且处于可接单状态的数量
        long count = teacherOrderTypeRepository.countByOrderTypeAndReceiptStatusAndReceiptRateLessThanAndOrderLimitGreaterThan(orderType, 1,
                100,0);
        boolean flag = true;
        if (count == 0) {
            flag = false;
        }
        return flag;
    }

    @Override
    public void validTeacherIsMe(long teacherId, String message) {
        EssayTeacher teacher = findById(teacherId);
        UserInfo userInfo = getUserInfo();
        if (null != userInfo && null != teacher &&
                null != userInfo.getId() &&
                userInfo.getId().equals(teacher.getUCenterId())) {
            return;
        }
        throw new BizException(ErrorResult.create(1021231, String.format(message,
                (null != teacher && StringUtils.isNotBlank(teacher.getUCenterName())) ? teacher.getUCenterName() : Strings.EMPTY)));

    }

    @Override
    public List<EssayTeacherOrderType> findCanCorrectTeachers(int orderType) {
        List<EssayTeacherOrderType> teachers = teacherOrderTypeRepository.findByOrderTypeAndReceiptStatusAndStatusAndReceiptRateLessThanAndOrderLimitGreaterThan(orderType,
                EssayStatusEnum.NORMAL.getCode(),EssayStatusEnum.NORMAL.getCode(), 100, 0);
        return teachers;
    }

    @Override
    public EssayTeacherOrderType findTeacherOrderType(int orderType, long teacherId) {
        return teacherOrderTypeRepository.findByTeacherIdAndOrderTypeAndStatus(teacherId,orderType, EssayStatusEnum.NORMAL.getCode());
    }



	@Override
	public PageUtil fetchDistributionTeacher(FetchTeacherReq fetchTeacherReq) {
		Page<EssayTeacher> teachers = teacherManager.pageDistributionTeacher(fetchTeacherReq);
		PageUtil packageTeacherInfo = packageTeacherInfo(teachers);
		return packageTeacherInfo;
	}

    @Override
    public void initTodayAmount() {
        long todayStartMillions = DateUtil.getTodayStartMillions();
        List<EssayTeacherOrderType> orderTypes = teacherOrderTypeRepository.findByStatusAndGmtClearLessThanEqual(EssayStatusEnum.NORMAL.getCode(),
                new Date(todayStartMillions));
        if(CollectionUtils.isNotEmpty(orderTypes)){
            for (EssayTeacherOrderType orderType : orderTypes) {
                teacherManager.clearOrderType(orderType);
            }
        }
    }



    private PageUtil packageTeacherInfo(Page<EssayTeacher> teachers) {
		Map<Long, TeacherVo> teacherVoMap = Maps.newHashMap();
		List<TeacherVo> teacherVos = Lists.newArrayList();
		List<Long> teacherIds = Lists.newArrayList();
		Map<Long, String> areaNameMap = areaManager.getAreaTree().getAreaNameMap();
		teachers.getContent().forEach(essayTeacher -> {
			TeacherVo teacherVo = new TeacherVo();
			teacherVos.add(teacherVo);
			teacherVoMap.put(essayTeacher.getId(), teacherVo);
			teacherVo.setTeacherId(essayTeacher.getId());
			teacherVo.setRealName(essayTeacher.getRealName());
			teacherVo.setNickName(essayTeacher.getNickName());
			teacherVo.setPhoneNum(essayTeacher.getPhoneNum());
			teacherVo.setTeacherLevel(TeacherLevelEnum.create(essayTeacher.getTeacherLevel()).getTitle());
			teacherVo.setTeacherStatus(TeacherStatusEnum.create(essayTeacher.getTeacherStatus()).getTitle());
			teacherVo.setDepartment(DepartmentEnum.create(essayTeacher.getDepartment()).getTitle());
			teacherVo.setArea(areaNameMap.get(essayTeacher.getAreaId()));
			teacherVo.setTeacherScore(essayTeacher.getTeacherScore());
			if (StringUtils.isNotBlank(essayTeacher.getCorrectType())) {
				List<String> correctTypes = Lists.newArrayList();
				teacherVo.setCorrectTypes(correctTypes);
				String[] strings = essayTeacher.getCorrectType().split(",");
				for (int i = 0; i < strings.length; i++) {
					correctTypes.add(TeacherOrderTypeEnum.create(Integer.parseInt(strings[i])).getTitle());
				}
			}
			teacherIds.add(essayTeacher.getId());
		});

		List<EssayTeacherOrderType> orderTypes = teacherManager.listOrderType(teacherIds);
		Map<Long, StringBuffer> totalAmountMap = Maps.newHashMap();
		Map<Long, StringBuffer> currentAmountMap = Maps.newHashMap();
		for (int i = 0; i < orderTypes.size(); i++) {
			EssayTeacherOrderType teacherOrderType = orderTypes.get(i);
			StringBuffer totalBuffer = totalAmountMap.get(teacherOrderType.getTeacherId());
			if (totalBuffer == null) {
				totalBuffer = new StringBuffer();
				totalAmountMap.put(teacherOrderType.getTeacherId(), totalBuffer);
			}
			StringBuffer currentBuffer = currentAmountMap.get(teacherOrderType.getTeacherId());
			if (currentBuffer == null) {
				currentBuffer = new StringBuffer();
				currentAmountMap.put(teacherOrderType.getTeacherId(), currentBuffer);
			}
			totalBuffer.append(teacherOrderType.getOrderAmount() + ",");
			currentBuffer.append(teacherOrderType.getCurrentFinishAmount() + ",");
		}
		teacherVos.forEach(teacherVo -> {
			StringBuffer totalBuffer = totalAmountMap.get(teacherVo.getTeacherId());
			if(totalBuffer != null) {
				teacherVo.setTotalCorrectAmount(totalBuffer.substring(0, totalBuffer.length() - 1));
				StringBuffer currentBuffer = currentAmountMap.get(teacherVo.getTeacherId());
				teacherVo.setCurrentCorrectAmount(currentBuffer.substring(0, currentBuffer.length() - 1));
			}
		});
		return new PageUtil<List<TeacherVo>>(teacherVos, teachers);
	}

    /**
     * 根据权限中心ID查询老师
     *
     * @param uCenterId 权限中心ID
     * @return 老师信息
     */
    @Override
    public EssayTeacher getTeacherByUCenterId(Long uCenterId) {
        return teacherManager.getTeacherByUCenterId(uCenterId);
    }
}
