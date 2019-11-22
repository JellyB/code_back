package com.huatu.tiku.essay.service.impl.correct;

import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.constant.status.EssayAnswerConstant;
import com.huatu.tiku.essay.constant.status.SystemConstant;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.entity.correct.CorrectImage;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import com.huatu.tiku.essay.essayEnum.*;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum.DelayStatusEnum;
import com.huatu.tiku.essay.manager.TeacherManager;
import com.huatu.tiku.essay.repository.*;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.repository.v2.EssayCorrectImageRepository;
import com.huatu.tiku.essay.repository.v2.EssayPaperLabelTotalRepository;
import com.huatu.tiku.essay.service.CorrectPushService;
import com.huatu.tiku.essay.service.EssaySimilarQuestionService;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.courseExercises.EssayExercisesAnswerMetaService;
import com.huatu.tiku.essay.service.paper.EssayPaperAnswerService;
import com.huatu.tiku.essay.service.question.EssayQuestionAnswerService;
import com.huatu.tiku.essay.service.v2.CorrectFeedBackService;
import com.huatu.tiku.essay.service.v2.correctOrder.CorrectOrderSnapshotService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.util.common.MDSmsUtil;
import com.huatu.tiku.essay.util.enu.IEnum;
import com.huatu.tiku.essay.vo.admin.correct.CorrectFeedBackVo;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderBaseVo;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderDetailVo;
import com.huatu.tiku.essay.vo.req.CorrectOperateRep;
import com.huatu.tiku.essay.vo.req.CorrectOrderRep;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.swing.text.html.Option;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CorrectOrderServiceImpl implements CorrectOrderService {


    @Autowired
    EssayTeacherService essayTeacherService;

    @Autowired
    EssayTeacherRepository essayTeacherRepository;

    @Autowired
    EssayPaperAnswerService essayPaperAnswerService;

    @Autowired
    EssayPaperAnswerRepository essayPaperAnswerRepository;

    @Autowired
    EssayQuestionAnswerService essayQuestionAnswerService;

    @Autowired
    EssayQuestionAnswerRepository essayQuestionAnswerRepository;

    @Autowired
    EssayQuestionDetailRepository essayQuestionDetailRepository;

    @Autowired
    CorrectOrderRepository correctOrderRepository;

    @Autowired
    CorrectOrderSnapshotService correctOrderSnapshotService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    EntityManager entityManager;

    @Autowired
    TeacherManager teacherManager;

    @Autowired
    CorrectFeedBackService correctFeedBackService;

    @Autowired
    EssayTeacherOrderTypeRepository essayTeacherOrderTypeRepository;

    @Autowired
    EssayGoodsOrderDetailRepository essayGoodsOrderDetailRepository;

    @Autowired
    EssayUserCorrectGoodsRepository essayUserCorrectGoodsRepository;

    @Autowired
    EssayLabelTotalRepository essayLabelTotalRepository;

    @Autowired
    EssayPaperLabelTotalRepository essayPaperLabelTotalRepository;

    @Autowired
    EssayCorrectImageRepository essayCorrectImageRepository;

    @Autowired
    private CorrectPushService correctPushService;

    @Autowired
    EssayExercisesAnswerMetaService essayExercisesAnswerMetaService;

    /**
     * 根据答题卡ID,答题卡类型,查询订单
     *
     * @param answerCardId
     * @param paper
     * @return
     */
    @Override
    public CorrectOrder findByAnswerId(long answerCardId, EssayAnswerCardEnum.TypeEnum paper) {
        CorrectOrder correctOrder = correctOrderRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(answerCardId,
                paper.getType(), EssayStatusEnum.NORMAL.getCode());
        return correctOrder;
    }

    @Override
    public void finished(Long answerId, EssayAnswerCardEnum.TypeEnum typeEnum) {
        /**
         * 修改订单状态
         */
        CorrectOrder order = findByAnswerId(answerId, typeEnum);
        if (null != order) {
            order.setBizStatus(CorrectOrderStatusEnum.CORRECTED.getValue());
            order.setEndTime(new Date());
            correctOrderRepository.save(order);
            CorrectOrderSnapshot snapshot = CorrectOrderSnapshot.builder().orderId(order.getId()).channel(CorrectOrderSnapshotChannelEnum.TEACHER.getValue())
                    .correctTeacherId(order.getReceiveOrderTeacher())
                    .build();
            snapshot.setCreator(essayTeacherService.getUserInfo().getUsername());
            correctOrderSnapshotService.save(snapshot, CorrectOrderStatusEnum.OperateEnum.END_CORRECT);
            teacherManager.updateTeacherCurrentOrder(order.getReceiveOrderTeacher(),
                    TeacherOrderTypeEnum.create(order.getType()), CorrectOrderStatusEnum.OperateEnum.END_CORRECT);
        }
        sendManualCorrectMessage(answerId, typeEnum);
    }

    @Override
    public void startLabel(long answerId, EssayAnswerCardEnum.TypeEnum typeEnum) {
        CorrectOrder order = findByAnswerId(answerId, typeEnum);
        if (null != order) {
            order.setBizStatus(CorrectOrderStatusEnum.ON_GOING.getValue());
            order.setCorrectTime(new Date());
            correctOrderRepository.save(order);
            CorrectOrderSnapshot snapshot = CorrectOrderSnapshot.builder().orderId(order.getId()).channel(CorrectOrderSnapshotChannelEnum.TEACHER.getValue()).correctTeacherId(order.getReceiveOrderTeacher()).build();
            snapshot.setCreator(essayTeacherService.getUserInfo().getUsername());
            correctOrderSnapshotService.save(snapshot, CorrectOrderStatusEnum.OperateEnum.START_CORRECT);
        }
    }

    /**
     * 创建订单
     *
     * @param correctOrder
     */
    @Override
//    @Async
    public void createOrder(CorrectOrder correctOrder) {
        correctOrder.setBizStatus(CorrectOrderStatusEnum.INIT.getValue());
        correctOrderRepository.save(correctOrder);
        CorrectOrderSnapshot snapshot = CorrectOrderSnapshot.builder().orderId(correctOrder.getId()).channel(CorrectOrderSnapshotChannelEnum.SYSTEM.getValue()).build();
        snapshot.setCreator("后台系统");
        correctOrderSnapshotService.save(snapshot, CorrectOrderStatusEnum.OperateEnum.INIT);
        List<CorrectOrder> correctOrders = correctOrderRepository.findByStatusAndBizStatusOrderByGmtCreateAsc(EssayStatusEnum.NORMAL.getCode(),
                CorrectOrderStatusEnum.INIT.getValue());
        int type = correctOrder.getType();
        TeacherOrderTypeEnum orderTypeEnum = TeacherOrderTypeEnum.create(type);
        String stem = getQuestionName(correctOrder.getAnswerCardId(), correctOrder.getAnswerCardType());
        MDSmsUtil.sendAdminOrderMsg(
                Optional.ofNullable(orderTypeEnum).map(TeacherOrderTypeEnum::getTitle).orElse("未知订单类型"),
                stem,
                Optional.ofNullable(correctOrders).map(i -> i.size()).orElse(0));
    }


    /**
     * 管理员～任务列表
     *
     * @return
     */
    public PageUtil<CorrectOrderBaseVo> taskList(CorrectOrderRep correctOrderRep) {
        PageUtil<CorrectOrderBaseVo> orderBaseVoPageUtil = searchParams(correctOrderRep);
        return orderBaseVoPageUtil;
    }

    /**
     * 管理员～查看任务列表
     *
     * @param id
     * @return
     */
    public CorrectOrderDetailVo lookTaskDetail(long id) {
        CorrectOrder correctOrder = correctOrderRepository.findByIdAndStatus(id, EssayStatusEnum.NORMAL.getCode());
        if (null != correctOrder) {
            EssayTeacher teacher = essayTeacherService.findById(correctOrder.getReceiveOrderTeacher());
            CorrectOrderDetailVo correctOrderDetailVo = convertEntityToVo(correctOrder, teacher);
            //判断是否可以再次批改
            if (correctOrder.getOldOrderId() > 0) {
                correctOrderDetailVo.setReCorrectStatus(EssayStatusEnum.NORMAL.getCode());
            }
            return correctOrderDetailVo;
        }
        return null;
    }


    /**
     * 试卷名称
     *
     * @param answerId
     * @param answerType
     * @return
     */
    public String getQuestionName(long answerId, int answerType) {
        //套题
        if (answerType == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            EssayPaperAnswer essayPaperAnswer = essayPaperAnswerRepository.findOne(answerId);
            if (null != essayPaperAnswer) {
                return essayPaperAnswer.getName();
            }
        } else {
            //单题
            EssayQuestionAnswer essayQuestionAnswer = essayQuestionAnswerRepository.findOne(answerId);
            if (essayQuestionAnswer != null) {
                return Optional.ofNullable(essayQuestionDetailRepository.getOne(essayQuestionAnswer.getQuestionDetailId()))
                        .map(EssayQuestionDetail::getStem).orElse("");
            }
        }
        return null;
    }


    /**
     * 管理员～分配老师(更新工单状态:待批改)
     * 操作日志记录～人工派单
     *
     * @param taskId    任务ID
     * @param teacherId 老师ID
     * @return
     */
    public Object distributeTask(long taskId, long teacherId, String admin) {
        CorrectOrder one = correctOrderRepository.findOne(taskId);
        EssayTeacher teacher = teacherManager.getTeacherById(teacherId);
        checkTeacher(one, teacher);
        if (one.getBizStatus() != CorrectOrderStatusEnum.INIT.getValue()) {
            throw new BizException(ErrorResult.create(1022132, "订单状态（" + CorrectOrderStatusEnum.create(one.getBizStatus()).getTitle() + "）不允许分配老师"));
        }
        correctOrderRepository.updateBizStatusAndReceiveTimeById(taskId, teacherId, new Date(),
                CorrectOrderStatusEnum.WAIT_CORRECT.getValue());

        CorrectOrderSnapshot correctOrderSnapshot = CorrectOrderSnapshot.builder()
                .orderId(taskId)
                .correctTeacherId(teacherId)
                .channel(CorrectOrderSnapshotChannelEnum.ADMIN.getValue()).build();

        correctOrderSnapshot.setCreator(essayTeacherService.getUserInfo().getUsername());
        correctOrderSnapshotService.save(correctOrderSnapshot, CorrectOrderStatusEnum.OperateEnum.DISPATCH_MANUAL);
        // 更新老师当日批改量

        teacherManager.updateTeacherCurrentOrder(teacherId,
                TeacherOrderTypeEnum.create(one.getType()), CorrectOrderStatusEnum.OperateEnum.DISPATCH_MANUAL);
        //发送短信
        if (teacher.getPhoneNum() != null) {
//            MDSmsUtil.sendCorrectOrderMsg(teacher.getPhoneNum());
            MDSmsUtil.sendMsg(teacher.getPhoneNum(),
                    String.format(MDSmsUtil.DISPATCH_ORDER_TEMPLATE,
                            Optional.ofNullable(one.getType())
                                    .map(TeacherOrderTypeEnum::create)
                                    .map(TeacherOrderTypeEnum::getTitle)
                                    .orElse("未知订单类型"),
                            getQuestionName(one.getAnswerCardId(), one.getAnswerCardType())
                    ));
        }
        return null;
    }

    /**
     * 校验老师操作订单的权限
     *
     * @param order
     * @param teacher
     */
    private void checkTeacher(CorrectOrder order, EssayTeacher teacher) {
        String[] strings = teacher.getCorrectType().split(",");
        List<Integer> teacherTypes = Arrays.stream(strings).map(Integer::valueOf).map(TeacherOrderTypeEnum::create).map(TeacherOrderTypeEnum::getValue).collect(Collectors.toList());
        if (!teacherTypes.contains(order.getType())) {
            throw new BizException(ErrorResult.create(1022131, "老师无批改" + TemplateEnum.QuestionLabelEnum.create(order.getType()).getValue() + "的权限"));
        }
        if (teacher.getPhoneNum() == null) {
            throw new BizException(ErrorResult.create(1022132, "请完善接单老师手机号"));
        }
    }

    /**
     * 老师接单
     */
    @Override
    public Object acceptOrder(long orderId, String uCenterName) {
        EssayTeacher essayTeacher = essayTeacherRepository.findByUCenterName(uCenterName);
        if (essayTeacher == null) {
            throw new BizException(EssayErrors.ERROR_TEACHER_INFO_NOT_EXIST);
        }
        int status = correctOrderRepository.updateBizStatusAndReceiveTimeById(orderId, essayTeacher.getId(),
                new Date(), CorrectOrderStatusEnum.WAIT_CORRECT.getValue());
        if (status != 1) {
            throw new BizException(EssayErrors.ERROR_CORRECT_ORDER_BIND_ERROR);
        }
        // 更新订单记录表
        CorrectOrderSnapshot build = CorrectOrderSnapshot.builder().orderId(orderId)
                .correctTeacherId(essayTeacher.getId())
                .channel(CorrectOrderSnapshotChannelEnum.TEACHER.getValue()).build();
        build.setCreator(essayTeacherService.getUserInfo().getUsername());
        correctOrderSnapshotService.save(build, CorrectOrderStatusEnum.OperateEnum.RECEIPT);
        return status;
    }

    /**
     * 老师拒接接单
     */
    @Override
    public Object refuseOrder(CorrectOperateRep correctOrderRep, String admin, Integer orderType) {
      /*  if (StringUtils.isEmpty(correctOrderRep.getOtherReason())) {
            throw new BizException(EssayErrors.REFUSE_ORDER_REASON_CANNOT_EMPTY);
        }*/
        long orderId = correctOrderRep.getOrderId();
        EssayTeacher essayTeacher = essayTeacherRepository.findByUCenterName(admin);
        if (essayTeacher == null) {
            throw new BizException(EssayErrors.ERROR_TEACHER_INFO_NOT_EXIST);
        }
        int status = correctOrderRepository.updateBizStatusByOrderId(0, orderId, CorrectOrderStatusEnum.INIT.getValue());
        if (status != 1) {
            throw new BizException(EssayErrors.ERROR_CORRECT_ORDER_BIND_ERROR);
        }
        CorrectOrderSnapshot build = CorrectOrderSnapshot.builder().orderId(orderId)
                .channel(CorrectOrderSnapshotChannelEnum.TEACHER.getValue())
                .correctTeacherId(essayTeacher.getId())
                .description(correctOrderRep.getOtherReason()).build();
        build.setCreator(essayTeacherService.getUserInfo().getUsername());
        correctOrderSnapshotService.save(build, CorrectOrderStatusEnum.OperateEnum.APPLY_BACK);
        //关闭个人配置
        essayTeacherOrderTypeRepository.updateReceiptStatusByTeacherIdAndOrderType(YesNoEnum.NO.getValue(), essayTeacher.getId(), orderType);
        return null;
    }


    /**
     * 管理员-退回学员
     * 更新状态:已退回
     * 订单日志:管理员退回学员
     *
     * @return
     */
    @Transactional
    public Object returnUser(CorrectOperateRep correctOrderRep, String admin) {
        long orderId = correctOrderRep.getOrderId();
        CorrectOrder correctOrder = correctOrderRepository.findByIdAndStatus(orderId, EssayStatusEnum.NORMAL.getCode());
        if (null == correctOrder) {
            throw new BizException(EssayErrors.CORRECT_ORRDER_ERROR);
        }
        if (correctOrder.getCorrectMode() == CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode()) {
            throw new BizException(EssayErrors.MANUAL_CANNOT_RETURN_USER);
        }
        long oldTeacherId = correctOrder.getReceiveOrderTeacher();
        long goodsOrderDetailId = correctOrder.getGoodsOrderDetailId();
        if (goodsOrderDetailId > 0) {
            UserOrderUtil.returnCorrectTimes(correctOrder, essayGoodsOrderDetailRepository, essayUserCorrectGoodsRepository);
        }
        //1.更新订单状态:已退回;保存被退回原因;批改老师改为0
        correctOrder.setReceiveOrderTeacher(0);//
        correctOrder.setBizStatus(CorrectOrderStatusEnum.BACKED.getValue());
        String returnUserReason = getReason(correctOrderRep.getReasonId(), correctOrderRep.getOtherReason());
        correctOrder.setCorrectMemo(returnUserReason);
        correctOrderRepository.save(correctOrder);
        //2.更新答题卡状态:被退回
        int answerCardType = correctOrder.getAnswerCardType();
        if (answerCardType == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
            EssayQuestionAnswer questionAnswer = essayQuestionAnswerService.findById(correctOrder.getAnswerCardId());
            if (null != questionAnswer && questionAnswer.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
                questionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus());
                essayQuestionAnswerRepository.save(questionAnswer);
                essayExercisesAnswerMetaService.updateQuestionStatus(questionAnswer);
                //消息推送
                correctPushService.correctReturn4Push(questionAnswer, correctOrder.getAnswerCardType(), correctOrder.getExercisesType(), correctOrder.getCorrectMemo());
            }
        } else if (answerCardType == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            EssayPaperAnswer essayPaperAnswer = essayPaperAnswerRepository.findByIdAndStatus(correctOrder.getAnswerCardId(), EssayStatusEnum.NORMAL.getCode());
            if (null != essayPaperAnswer && essayPaperAnswer.getBizStatus() != EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT.getBizStatus()) {
                essayPaperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.CORRECT_RETURN.getBizStatus());
                essayPaperAnswerRepository.save(essayPaperAnswer);
                essayExercisesAnswerMetaService.updatePaperStatus(essayPaperAnswer);
                //消息推送
                correctPushService.correctReturn4Push(essayPaperAnswer, correctOrder.getAnswerCardType(), correctOrder.getExercisesType(), correctOrder.getCorrectMemo());
            }
        }
        //3.新增操作日志
        CorrectOrderSnapshot correctOrderSnapshot = CorrectOrderSnapshot.builder()
                .orderId(orderId)
                .channel(CorrectOrderSnapshotChannelEnum.ADMIN.getValue())
                .description(returnUserReason)
                .correctTeacherId(oldTeacherId)
                .build();

        correctOrderSnapshot.setCreator(essayTeacherService.getUserInfo().getUsername());
        correctOrderSnapshotService.save(correctOrderSnapshot, CorrectOrderStatusEnum.OperateEnum.RETURN_USER);
        //4.更新老师完成量数据
        if (oldTeacherId > 0) {
            teacherManager.updateTeacherCurrentOrder(correctOrder.getReceiveOrderTeacher(),
                    TeacherOrderTypeEnum.create(correctOrder.getType()), CorrectOrderStatusEnum.OperateEnum.RETURN_USER);
        }
        return null;
    }


    /**
     * 管理员～撤回任务(工单状态更新为:待分批)
     */
    public Object cancelTask(CorrectOperateRep correctOrderRep, String admin) {
        if (null == correctOrderRep) {
            throw new BizException(EssayErrors.CORRECT_ORRDER_ERROR);
        }

        //撤回任务时候，需要将totalId 状态改为学员不可看的
        long orderId = correctOrderRep.getOrderId();
        CorrectOrder correctOrder = correctOrderRepository.findByIdAndStatus(orderId, EssayStatusEnum.NORMAL.getCode());
        long answerCardId = correctOrder.getAnswerCardId();
        if (correctOrder.getAnswerCardType() == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
            //单题,更新 EssayLabelTotal
            essayLabelTotalRepository.updateLabelFlag(answerCardId, EssayStatusEnum.NORMAL.getCode(), LabelFlagEnum.TEACHING_AND_RESEARCH.getCode());
        } else if (correctOrder.getAnswerCardType() == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            //套卷,更新 EssayPaperLabelTotal
            essayPaperLabelTotalRepository.updateLabelFlag(answerCardId, EssayStatusEnum.NORMAL.getCode(), LabelFlagEnum.TEACHING_AND_RESEARCH.getCode());
        }

        EssayTeacher teacher = essayTeacherRepository.findByUCenterName(admin);
        //新增工单记录
        CorrectOrderSnapshot correctOrderSnapshot = CorrectOrderSnapshot.builder()
                .description(correctOrderRep.getOtherReason())
                .orderId(correctOrderRep.getOrderId())
                .correctTeacherId(correctOrder.getReceiveOrderTeacher())
                .channel(CorrectOrderSnapshotChannelEnum.ADMIN.getValue()).build();

        if (null != teacher) {
            correctOrderSnapshot.setCreator(essayTeacherService.getUserInfo().getUsername());
        }
        correctOrder.setReceiveOrderTeacher(0);
        correctOrder.setBizStatus(CorrectOrderStatusEnum.INIT.getValue());
        correctOrder.setGmtModify(new Date());
        correctOrderRepository.save(correctOrder);
        correctOrderSnapshotService.save(correctOrderSnapshot, CorrectOrderStatusEnum.OperateEnum.RECALL);
        teacherManager.updateTeacherCurrentOrder(correctOrderSnapshot.getCorrectTeacherId(),
                TeacherOrderTypeEnum.create(correctOrder.getType()), CorrectOrderStatusEnum.OperateEnum.RECALL);
        //需要通知老师被撤回
        if (null != teacher) {
            MDSmsUtil.sendReturnMsg(teacher.getPhoneNum());
        }
        return null;
    }

    /**
     * 再次批改
     *
     * @param taskId
     * @return
     */
    public Object reCorrect(long taskId, String admin, long teacherId) {
        //创建答题卡（查询原来答题卡信息,copy到新答题卡）
        CorrectOrder correctOrder = correctOrderRepository.findByIdAndStatus(taskId, EssayStatusEnum.NORMAL.getCode());
        if (null == correctOrder) {
            throw new BizException(EssayErrors.CORRECT_ORRDER_ERROR);
        }
        if (correctOrder.getReceiveOrderTeacher() == teacherId) {
            throw new BizException(EssayErrors.RE_CORRECT_CAN_NOT_SAME_TEACHER);
        }
        List<CorrectOrder> correctOrderList = correctOrderRepository.findByOldOrderIdAndStatus(taskId, EssayStatusEnum.NORMAL.getCode());
        if (CollectionUtils.isNotEmpty(correctOrderList)) {
            throw new BizException(EssayErrors.ERROR_EXIST_RECORRECT_ORDER);
        }
        EssayTeacher teacher = teacherManager.getTeacherById(teacherId);
        checkTeacher(correctOrder, teacher);
        if (correctOrder.getBizStatus() != CorrectOrderStatusEnum.CORRECTED.getValue() &&
                correctOrder.getBizStatus() != CorrectOrderStatusEnum.FEEDBACK.getValue()) {
            throw new BizException(ErrorResult.create(1022132, "订单状态（" + CorrectOrderStatusEnum.create(correctOrder.getBizStatus()).getTitle() + "）不允许分配老师"));
        }

        CorrectOrderBaseVo correctOrderVo = new CorrectOrderBaseVo();
        BeanUtils.copyProperties(correctOrder, correctOrderVo);
        Long targetAnswerId = 0L;
        Integer targetAnswerType = 0;

        int answerCardType = correctOrderVo.getAnswerCardType();
        if (answerCardType == EssayAnswerCardEnum.TypeEnum.QUESTION.getType()) {
            EssayQuestionAnswer questionAnswer = essayQuestionAnswerService.findById(correctOrderVo.getAnswerCardId());
            if (null != questionAnswer) {
                targetAnswerId = createNewQuestionAnswer(questionAnswer, 0L);
                targetAnswerType = EssayAnswerCardEnum.TypeEnum.QUESTION.getType();
            }
        } else if (answerCardType == EssayAnswerCardEnum.TypeEnum.PAPER.getType()) {
            EssayPaperAnswer paperAnswer = essayPaperAnswerService.findById(correctOrderVo.getAnswerCardId());
            if (null != paperAnswer) {
                EssayPaperAnswer targetPaperAnswer = EssayPaperAnswer.builder()
                        .paperBaseId(paperAnswer.getPaperBaseId())
                        .name(paperAnswer.getName())
                        .userId(paperAnswer.getUserId())
                        .score(paperAnswer.getScore())
                        .unfinishedCount(paperAnswer.getUnfinishedCount())
                        .speed(paperAnswer.getSpeed())
                        .lastIndex(paperAnswer.getLastIndex())
                        .speed(paperAnswer.getSpendTime())
                        .areaId(paperAnswer.getAreaId())
                        .areaName(paperAnswer.getAreaName())
                        .type(paperAnswer.getType())
                        .correctMode(paperAnswer.getCorrectMode())
                        .build();
                //套卷状态改为:已交卷
                targetPaperAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
                essayPaperAnswerRepository.save(targetPaperAnswer);
                targetAnswerId = targetPaperAnswer.getId();
                targetAnswerType = EssayAnswerCardEnum.TypeEnum.PAPER.getType();
                //创建试题答题卡
                List<EssayQuestionAnswer> questionAnswerList = essayQuestionAnswerService.findByAnswerId(paperAnswer.getId());
                if (CollectionUtils.isNotEmpty(questionAnswerList)) {
                    questionAnswerList.forEach(questionAnswer -> {
                        createNewQuestionAnswer(questionAnswer, targetPaperAnswer.getId());
                    });
                }
            }
        }
        //旧订单更新废弃状态
        correctOrder.setEffectiveStatus(EssayStatusEnum.DELETED.getCode());
        correctOrderRepository.save(correctOrder);

        //老师订单数量更新
        teacherManager.updateTeacherCurrentOrder(correctOrder.getReceiveOrderTeacher(),
                TeacherOrderTypeEnum.create(correctOrder.getType()), CorrectOrderStatusEnum.OperateEnum.END_CORRECT);
        //创建新订单,关联新旧订单
        // 计算预计完成时间
        Date deadLine = calculateDeadLine(correctOrderVo.getType(), correctOrderVo.getDelayStatus());
        CorrectOrder targetCorrectOrder = CorrectOrder.builder().type(correctOrderVo.getType())
                .gmtDeadLine(deadLine)
                .answerCardId(targetAnswerId)
                .answerCardType(targetAnswerType)
                .userId(correctOrderVo.getUserId())
                .type(correctOrderVo.getType())
                .correctMode(correctOrderVo.getCorrectMode())
                .receiveOrderTeacher(teacherId)
                .oldOrderId(correctOrderVo.getId())
                .build();
        targetCorrectOrder.setBizStatus(CorrectOrderStatusEnum.WAIT_RECEIPT.getValue());
        correctOrderRepository.save(targetCorrectOrder);

        //新增订单操作记录
        CorrectOrderSnapshot build = CorrectOrderSnapshot.builder().channel(CorrectOrderSnapshotChannelEnum.TEACHER.getValue())
                .orderId(targetCorrectOrder.getId()).build();
        correctOrderSnapshotService.save(build, CorrectOrderStatusEnum.OperateEnum.DISPATCH_MANUAL);
        if (teacher.getPhoneNum() != null) {
//            MDSmsUtil.sendCorrectOrderMsg(teacher.getPhoneNum());
            MDSmsUtil.sendMsg(teacher.getPhoneNum(),
                    String.format(MDSmsUtil.DISPATCH_ORDER_TEMPLATE,
                            Optional.ofNullable(correctOrder.getType())
                                    .map(TeacherOrderTypeEnum::create)
                                    .map(TeacherOrderTypeEnum::getTitle)
                                    .orElse("未知订单类型"),
                            getQuestionName(correctOrder.getAnswerCardId(), correctOrder.getAnswerCardType())
                    ));
        }
        log.info("新答题ID是:{},新订单ID是:{}", targetAnswerId, targetCorrectOrder.getId());
        return null;
    }


    /**
     * 创建新的试题答题卡
     *
     * @param questionAnswer 被copy的试题答题卡
     */
    public Long createNewQuestionAnswer(EssayQuestionAnswer questionAnswer, Long paperAnswerId) {

        if (null != questionAnswer) {
            long questionAnswerId = questionAnswer.getId();
            EssayQuestionAnswer targetQuestionAnswer = EssayQuestionAnswer.builder()
                    .content(questionAnswer.getContent())
                    .score(questionAnswer.getScore())
                    .userId(questionAnswer.getUserId())
                    .areaId(questionAnswer.getAreaId())
                    .areaName(questionAnswer.getAreaName())
                    .questionBaseId(questionAnswer.getQuestionBaseId())
                    .questionDetailId(questionAnswer.getQuestionDetailId())
                    .paperAnswerId(paperAnswerId)
                    .questionYear(questionAnswer.getQuestionYear())
                    .paperId(questionAnswer.getPaperId())
                    .terminal(questionAnswer.getTerminal())
                    .spendTime(questionAnswer.getSpendTime())
                    .questionType(questionAnswer.getQuestionType())
                    .correctMode(questionAnswer.getCorrectMode())
                    .build();
            ///状态:已经交卷
            targetQuestionAnswer.setBizStatus(EssayAnswerConstant.EssayAnswerBizStatusEnum.COMMIT.getBizStatus());
            essayQuestionAnswerRepository.save(targetQuestionAnswer);

            long newQuestionAnswerId = targetQuestionAnswer.getId();

            //人工批改图片
            if (questionAnswer.getCorrectMode() == CorrectModeEnum.MANUAL.getMode()) {
                List<CorrectImage> correctImageList = essayCorrectImageRepository.findByQuestionAnswerIdAndStatusOrderBySort(questionAnswerId, EssayStatusEnum.NORMAL.getCode());
                if (CollectionUtils.isNotEmpty(correctImageList)) {
                    List<CorrectImage> newCorrectImageList = correctImageList.stream().map(image -> {
                        CorrectImage build = CorrectImage.builder().questionAnswerId(newQuestionAnswerId)
                                .content(image.getContent())
                                .imageUrl(image.getImageUrl())
                                .roll(image.getRoll())
                                .sort(image.getSort()).build();
                        return build;
                    }).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(newCorrectImageList)) {
                        essayCorrectImageRepository.save(newCorrectImageList);
                    }
                }
            }
            log.info("再次批改,新答题卡ID是:{},新答题ID是:{}", paperAnswerId, newQuestionAnswerId);
            return newQuestionAnswerId;
        }
        return 0L;
    }


    /**
     * 管理员～驳回（老师退回）请求
     * 订单状态更改为:待批改
     *
     * @param correctOrderRep
     * @return
     */
    public void rejectRequest(CorrectOperateRep correctOrderRep, String admin) {
        long taskId = correctOrderRep.getOrderId();
        CorrectOrder correctOrder = correctOrderRepository.findByIdAndStatus(taskId, EssayStatusEnum.NORMAL.getCode());
        if (null != correctOrder) {
            correctOrder.setBizStatus(CorrectOrderStatusEnum.WAIT_CORRECT.getValue());
            correctOrderRepository.save(correctOrder);

            CorrectOrderSnapshot correctOrderSnapshot = CorrectOrderSnapshot.builder()
                    .channel(CorrectOrderSnapshotChannelEnum.ADMIN.getValue())
                    .correctTeacherId(correctOrder.getReceiveOrderTeacher())
                    .orderId(taskId)
                    .description(correctOrderRep.getOtherReason())
                    .build();
            correctOrderSnapshot.setCreator(essayTeacherService.getUserInfo().getUsername());
            correctOrderSnapshotService.save(correctOrderSnapshot, CorrectOrderStatusEnum.OperateEnum.REJECT_BACK);
        }
    }

    @Override
    public CorrectOrder getNext(long answerCardId, EssayAnswerCardEnum.TypeEnum typeEnum) {
        CorrectOrder oldOrder = correctOrderRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(answerCardId, typeEnum.getType(), EssayStatusEnum.NORMAL.getCode());
        long teacherId = oldOrder.getReceiveOrderTeacher();
        List<CorrectOrder> orders = correctOrderRepository.findByReceiveOrderTeacherAndAnswerCardTypeAndBizStatus(teacherId, typeEnum.getType(), CorrectOrderStatusEnum.WAIT_CORRECT.getValue());
        if (CollectionUtils.isEmpty(orders)) {
            throw new BizException(ErrorResult.create(2019727, "无待批改的订单"));
        }
        return orders.get(0);
    }

    @Override
    public void sendManualCorrectMessage(long answerId, EssayAnswerCardEnum.TypeEnum typeEnum) {
        try {
            Map sendMap = Maps.newHashMap();
            sendMap.put("answerId", answerId);
            sendMap.put("answerType", typeEnum.getType());
            rabbitTemplate.convertAndSend(SystemConstant.ESSAY_MANUAL_CORRECT_FINISH_QUEUE, sendMap);
            log.info("sendManualCorrectMessage mq , answerId={},typeEnum={}", answerId, typeEnum);
        } catch (Exception e) {
            log.warn("发送消息队列失败，mq , answerId={},typeEnum={}", answerId, typeEnum);
        }
    }

    private CorrectOrderDetailVo convertEntityToVo(CorrectOrder correctOrder, EssayTeacher teacher) {
        CorrectOrderDetailVo detailVo = new CorrectOrderDetailVo();
        BeanUtils.copyProperties(correctOrder, detailVo);
        detailVo.setSettlementStatus(0);//暂时都是未结算
        detailVo.setCorrectTypeName("普通批改");//暂时都是普通批改
        //批改状态(批改完成&&已经评价,展示状态为：已经评价)
        if (correctOrder.getBizStatus() == CorrectOrderStatusEnum.CORRECTED.getValue() && correctOrder.getFeedBackStatus() == CorrectFeedBackEnum.YES.getCode()) {
            detailVo.setBizStatus(CorrectOrderStatusEnum.FEEDBACK.getValue());
            detailVo.setBizStatusName(CorrectOrderStatusEnum.FEEDBACK.getTitle());
        }
        detailVo.setBizStatusName(CorrectOrderStatusEnum.create(correctOrder.getBizStatus()).getTitle());
        //试题类型
        detailVo.setTypeName(TemplateEnum.QuestionLabelEnum.create(detailVo.getType()).getValue());
        //批改类型名称
        detailVo.setCorrectMode(correctOrder.getCorrectMode());
        detailVo.setCorrectModeName(CorrectModeEnum.create(correctOrder.getCorrectMode()).getName());

        //批改类型
        EssayTeacher essayTeacher = essayTeacherService.findById(detailVo.getReceiveOrderTeacher());
        if (null != essayTeacher) {
            detailVo.setTeacherName(essayTeacher.getRealName());
            detailVo.setPhoneNum(essayTeacher.getPhoneNum());
        }
        //试题内容
        detailVo.setStem(getQuestionName(correctOrder.getAnswerCardId(), correctOrder.getAnswerCardType()));

        //累计用时计算
        if (correctOrder.getBizStatus() == CorrectOrderStatusEnum.FEEDBACK.getValue() ||
                correctOrder.getBizStatus() == CorrectOrderStatusEnum.CORRECTED.getValue()) {
            // 批改完成,完成时间-提交时间
            detailVo.setTotalTime(getTime(correctOrder.getGmtCreate(), correctOrder.getEndTime()));
            detailVo.setLeftTime(0L);
        } else {
            // 未批改完成,当前时间-提交时间
            detailVo.setTotalTime(getTime(correctOrder.getGmtCreate(), new Date()));
            // 剩余时间=预计完成时间-当前时间
            detailVo.setLeftTime(getTime(new Date(), correctOrder.getGmtDeadLine()));

        }

        if (null != correctOrder.getGmtDeadLine()) {
            //订单超时,未完成状态 && 超过预期完成时间】
            Date endTime = correctOrder.getEndTime();
            endTime = endTime == null ? new Date() : endTime;
            if (endTime.compareTo(correctOrder.getGmtDeadLine()) >= 0)
                detailVo.setTimeOutStatus(EssayStatusEnum.NORMAL.getCode());
        }
        // 用户信息 （跟产品沟通，userName直接展示userId即可）
        detailVo.setUserId(correctOrder.getUserId());
        detailVo.setUserName(correctOrder.getUserId() + "");
        if (TemplateEnum.QuestionLabelEnum.TT.getCode() != correctOrder.getType()) {
            EssayQuestionAnswer one = essayQuestionAnswerRepository.findOne(correctOrder.getAnswerCardId());
           /* detailVo.setUserName(one.getUserId() + "");
            detailVo.setUserId(one.getUserId());*/
            if (one != null) {
                detailVo.setSubmitTime(one.getSubmitTime());
            }
        } else {
            EssayPaperAnswer one = essayPaperAnswerRepository.findOne(correctOrder.getAnswerCardId());
           /* detailVo.setUserName(one.getUserId() + "");
            detailVo.setUserId(one.getUserId());*/
            if (one != null) {
                detailVo.setSubmitTime(one.getSubmitTime());
            }
        }
        //用户信息展示
        detailVo.setUserName(correctOrder.getUserName());
        detailVo.setUserId(correctOrder.getUserId());
        detailVo.setUserPhoneNum(correctOrder.getUserPhoneNum());
        //老师基本金额
        detailVo.setMoney(TeacherOrderTypeEnum.create(correctOrder.getType()).getSalary().doubleValue());
        //老师评分
        List<CorrectFeedBackVo> correctFeedBackVos = correctFeedBackService.findByAnswerId(correctOrder.getAnswerCardId(), correctOrder.getAnswerCardType());
        if (CollectionUtils.isNotEmpty(correctFeedBackVos)) {
            detailVo.setScore(correctFeedBackVos.stream().findFirst().get().getStar());
        }
        return detailVo;
    }


    public long getTime(Date startTime, Date endTime) {
        if (null == startTime || null == endTime) {
            return 0L;
        }
        Long start = startTime.getTime();
        Long end = endTime.getTime();
        Long result = end - start;
        return result;
    }

    /**
     * 老师～查看任务列表
     */
    public PageUtil<CorrectOrderBaseVo> teacherTaskList(CorrectOrderRep correctOrderRep, String admin) {

        EssayTeacher essayTeacher = essayTeacherRepository.findByUCenterName(admin);
        if (null == essayTeacher) {
            throw new BizException(EssayErrors.ERROR_TEACHER_INFO_NOT_EXIST);
        }
        correctOrderRep.setTeacherId(essayTeacher.getId());
        return searchParams(correctOrderRep);
    }


    public PageUtil<CorrectOrderBaseVo> searchParams(CorrectOrderRep correctOrderRep) {

        PageRequest pageable = new PageRequest(correctOrderRep.getPage() - 1, correctOrderRep.getPageSize(),
                Sort.Direction.DESC, "id");
        StringBuffer dataSql = new StringBuffer();
        StringBuffer countSql = new StringBuffer();
        StringBuffer queryTitleSql = new StringBuffer();
        queryTitleSql.append(" DISTINCT(correct.id),correct.correct_time,correct.biz_status,correct.creator,correct.gmt_create, " +
                "correct.gmt_modify ,correct.modifier, correct.status, correct.gmt_dead_line, correct.name ,correct.type, correct.answer_card_id," +
                "correct.answer_card_type, correct.receive_order_teacher, correct.user_id, correct.correct_memo, correct.feed_back_status, " +
                "correct.end_time, correct.receive_time, correct.delay_status, correct.correct_mode, correct.effective_status, correct.old_order_id," +
                "correct.settlement_status, correct.goods_order_detail_id,correct.teacher_return_reason,correct.user_name,correct.user_phone_num,exercises_type,");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowTime = formatter.format(new Date());
        dataSql.append(" select ");
        dataSql.append(queryTitleSql);
        dataSql.append(" IF (TIMEDIFF(now(),correct.gmt_dead_line) > 0 AND correct.biz_status<4,1,0) timeOutStatus ");
//        dataSql.append(" IF (correct.biz_status!=4 and correct.biz_status!=7 ,0,1) biz_sort ");
        dataSql.append(" FROM v_essay_correct_order correct ");
        dataSql.append(" LEFT join  v_essay_teacher teacher  on correct.receive_order_teacher=teacher.id");
        dataSql.append(" LEFT JOIN v_essay_question_answer question ON correct.answer_card_id = question.id and correct.answer_card_type = 0");
        dataSql.append(" LEFT JOIN v_essay_question_detail detail on  question.question_detail_id =detail.id");
        dataSql.append(" LEFT JOIN v_essay_paper_answer paper ON correct.answer_card_id = paper.id and correct.answer_card_type = 1");
//        dataSql.append(" LEFT JOIN v_essay_similar_question similarQuestion ON question.question_base_id = similarQuestion.question_base_id");
//        dataSql.append(" LEFT JOIN v_essay_similar_question_group similarGroup ON similarQuestion.similar_id = similarGroup.id");

        dataSql.append(" where correct.status=1 ");
        // dataSql.append(" and teacher.status=1 and question.status=1 and
        // paper.status=1");

        // id 查询
        if (null != correctOrderRep.getId()) {
            dataSql.append(" and correct.id = ").append(correctOrderRep.getId());
        }
        // 任务状态
        if (StringUtils.isNotEmpty(correctOrderRep.getTaskStatus())) {
            dataSql.append(" and correct.biz_status in (");
            dataSql.append(correctOrderRep.getTaskStatus());
            dataSql.append(")");
        }
        // 任务类型
        if (StringUtils.isNotEmpty(correctOrderRep.getTaskType())) {
            dataSql.append(" and correct.type in (");
            dataSql.append(correctOrderRep.getTaskType());
            dataSql.append(")");
        }
        // 是否顺延
        if (null != correctOrderRep.getDelayStatus()) {
            dataSql.append(" and correct.delay_status=");
            dataSql.append(correctOrderRep.getDelayStatus());
        }
        // 老师ID
        if (null != correctOrderRep.getTeacherId()) {
            dataSql.append(" and correct.receive_order_teacher=");
            dataSql.append(correctOrderRep.getTeacherId());
        }
        // 老师名称
        if (StringUtils.isNotEmpty(correctOrderRep.getTeacherName())) {
            dataSql.append(" and teacher.real_name like '%");
            dataSql.append(correctOrderRep.getTeacherName());
            dataSql.append("%'");
        }
        // 老师手机号
        if (StringUtils.isNotEmpty(correctOrderRep.getPhoneNum())) {
            dataSql.append(" and teacher.phone_num ='");
            dataSql.append(correctOrderRep.getPhoneNum());
            dataSql.append("'");
        }
        // 试题内容查询
        if (StringUtils.isNotEmpty(correctOrderRep.getQuestionContent())) {
            dataSql.append(" And ( detail.stem LIKE '%");
            dataSql.append(correctOrderRep.getQuestionContent());
            dataSql.append("%'");
            dataSql.append(" or paper.NAME LIKE '%");
            dataSql.append(correctOrderRep.getQuestionContent());
            dataSql.append("%')");
        }
        //超时时间查询
        if (null != correctOrderRep.getTimeOutStatus()) {
            dataSql.append(" and correct.gmt_dead_line");
            if (correctOrderRep.getTimeOutStatus() == EssayStatusEnum.NORMAL.getCode()) {
                // 超时,当前时间>预期完成时间
                dataSql.append("<'");
            } else {
                dataSql.append(">'");
            }
            dataSql.append(nowTime);
            dataSql.append("'");
        }
        //学生姓名查询
        if (StringUtils.isNotEmpty(correctOrderRep.getUserName())) {
            dataSql.append(" and correct.user_name like '%");
            dataSql.append(correctOrderRep.getUserName());
            dataSql.append("%'");
        }
        //学生手机号查询
        if (StringUtils.isNotEmpty(correctOrderRep.getUserPhoneNum())) {
            dataSql.append(" and  correct.user_phone_num like '%");
            dataSql.append(correctOrderRep.getUserPhoneNum());
            dataSql.append("%'");
        }
        //批改模式查询(2 人工批改 3 智能转人工)
        if (null != correctOrderRep.getCorrectMode()) {
            dataSql.append(" and correct.correct_mode=");
            dataSql.append(correctOrderRep.getCorrectMode());
        }

        dataSql.append(" ORDER BY correct.biz_status, timeOutStatus DESC,correct.gmt_dead_line");
        log.info("数据查询sql是:{}", dataSql.toString());
        countSql.append(" SELECT count(1) FROM( ");
        countSql.append(dataSql.toString());
        countSql.append(" ) as a ");
        //log.info("数量查询sql是:{}", countSql.toString());

        Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), CorrectOrder.class);
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        final Object singleResult = countQuery.getSingleResult();

        long totalLong = singleResult == null ? 0L : Long.valueOf(singleResult.toString());
        // 分页数据
        dataQuery.setFirstResult(pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<CorrectOrder> content2 = totalLong > pageable.getOffset() ? dataQuery.getResultList()
                : Collections.<CorrectOrder>emptyList();
        PageImpl<CorrectOrder> correctOrderPage = new PageImpl<>(content2, pageable, totalLong);
        List<CorrectOrder> correctOrders = correctOrderPage.getContent();

        List<CorrectOrderDetailVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(correctOrders)) {
            correctOrders.stream().forEach(correctOrder -> {
                CorrectOrderDetailVo correctOrderDetailVo = convertEntityToVo(correctOrder, null);
                voList.add(correctOrderDetailVo);
            });
        }

        // 设置分页
        long totalElements = correctOrderPage.getTotalElements();
        int size = pageable.getPageSize();

        PageUtil resultPageUtil = PageUtil.builder().result(voList)
                .totalPage(0 == totalElements / size ? totalElements / size : totalElements / size + 1)
                .next(pageable.getPageNumber()).total(totalElements).build();
        return resultPageUtil;
    }

    /**
     * 计算订单超时时间
     */
    @Override
    public Date calculateDeadLine(int correctOrderType, int delayStatus) {
        // 不顺延
        TeacherOrderTypeEnum orderType = TeacherOrderTypeEnum.create(correctOrderType);
        int hours = orderType.getCompleteTime();
        LocalDateTime localDateTime = LocalDateTime.now().plusHours(hours);
        if (delayStatus == DelayStatusEnum.YES.getCode()) {
            localDateTime = localDateTime.plusDays(orderType.getDelayTime());
        }

        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return date;
    }

    /**
     * 老师退回学员
     */
    @Override
    public int returnOrder(CorrectOperateRep correctOperateRep, String ucName) {
        EssayTeacher essayTeacher = essayTeacherRepository.findByUCenterName(ucName);
        if (essayTeacher == null) {
            throw new BizException(EssayErrors.ERROR_TEACHER_INFO_NOT_EXIST);
        }
        //更新订单状态:待退回;更新退回原因
        CorrectOrder correctOrder = correctOrderRepository.findByIdAndStatus(correctOperateRep.getOrderId(), EssayStatusEnum.NORMAL.getCode());
        if (null == correctOrder) {
            throw new BizException(EssayErrors.CORRECT_ORRDER_ERROR);
        }
        //智能转人工答题卡禁止退回学员
        if (correctOrder.getCorrectMode() == CorrectModeEnum.INTELLIGENCE_2_MANUAL.getMode()) {
            throw new BizException(EssayErrors.MANUAL_CANNOT_RETURN_USER);
        }
        if (essayTeacher.getId() != correctOrder.getReceiveOrderTeacher()) {
            throw new BizException(EssayErrors.CAN_NOT_RETURN_USER);
        }
        correctOrder.setTeacherReturnReason(getReason(correctOperateRep.getReasonId(), correctOperateRep.getOtherReason()));
        correctOrder.setBizStatus(CorrectOrderStatusEnum.WAIT_BACK.getValue());
        correctOrderRepository.save(correctOrder);

        //添加操作记录
        CorrectOrderSnapshot build = CorrectOrderSnapshot.builder().orderId(correctOperateRep.getOrderId())
                .channel(CorrectOrderSnapshotChannelEnum.TEACHER.getValue()).correctTeacherId(essayTeacher.getId())
                .description(correctOperateRep.getReasonId() == 0 ? correctOperateRep.getOtherReason()
                        : CorrectReturnReasonEnum.create(correctOperateRep.getReasonId()).getTitle())
                .build();
        build.setCreator(essayTeacherService.getUserInfo().getUsername());
        correctOrderSnapshotService.save(build, CorrectOrderStatusEnum.OperateEnum.APPLY_BACK);
        return 1;
    }

    /**
     * 获取退回原因
     */
    private String getReason(Integer reasonId, String otherReason) {
        StringBuffer finalReason = new StringBuffer();
        if (reasonId != 0) {
            finalReason.append(CorrectReturnReasonEnum.create(reasonId).getTitle());
        }
        if (StringUtils.isNotEmpty(otherReason)) {
            finalReason.append(":");
            finalReason.append(otherReason);
        }
        return finalReason.toString();
    }
}





