package com.huatu.tiku.essay.service.impl.dispatch;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.entity.BaseEntity;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.entity.EssayTeacherOrderType;
import com.huatu.tiku.essay.entity.correct.CorrectFeedBack;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import com.huatu.tiku.essay.essayEnum.CorrectOrderSnapshotChannelEnum;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum;
import com.huatu.tiku.essay.essayEnum.CourseExerciseTypeEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.TeacherOrderTypeEnum;
import com.huatu.tiku.essay.manager.TeacherManager;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.repository.v2.EssayCorrectFeedBackRepository;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.dispatch.CorrectOrderAutoDispatchService;
import com.huatu.tiku.essay.service.dispatch.DispatchFilterService;
import com.huatu.tiku.essay.service.v2.correctOrder.CorrectOrderSnapshotService;
import com.huatu.tiku.essay.util.common.MDSmsUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author huangqingpeng
 * @title: CorrectOrderAutoDispatchServiceImpl
 * @description: 实现自动派单接口
 * @date 2019-07-2610:28
 */
@Service
@Slf4j
public class CorrectOrderAutoDispatchServiceImpl implements CorrectOrderAutoDispatchService {

    @Autowired
    DispatchFilterService dispatchFilterService;

    @Autowired
    CorrectOrderRepository correctOrderRepository;

    @Autowired
    EssayTeacherService essayTeacherService;

    @Autowired
    EssayCorrectFeedBackRepository correctFeedBackRepository;

    @Autowired
    CorrectOrderSnapshotService correctOrderSnapshotService;

    @Autowired
    CorrectOrderService correctOrderService;

    @Autowired
    TeacherManager teacherManager;

    private static final Gson gson = new Gson();


    @Override
    public void autoBack(long time) {
        //待接单订单查询
        List<CorrectOrder> orders = correctOrderRepository.findByStatusAndBizStatusOrderByGmtCreateAsc(
                EssayStatusEnum.NORMAL.getCode(),
                CorrectOrderStatusEnum.WAIT_RECEIPT.getValue());
        List<CorrectOrder> collect = orders.parallelStream().filter(i -> i.getGmtModify().getTime() < time)
                .filter(correctOrderSnapshotService::checkNoAdmin)
                .collect(Collectors.toList());
        for (CorrectOrder order : collect) {
            CorrectOrderSnapshot correctOrderSnapshot = CorrectOrderSnapshot.builder()
                    .correctTeacherId(order.getReceiveOrderTeacher())
                    .orderId(order.getId())
                    .channel(CorrectOrderSnapshotChannelEnum.SYSTEM.getValue())
                    .build();
            order.setGmtModify(new Date());
            order.setBizStatus(EssayGoodsOrderConstant.EssayGoodsOrderBizStatusEnum.INIT.getStatus());
            order.setReceiveOrderTeacher(0);
            correctOrderRepository.save(order);
            correctOrderSnapshot.setGmtCreate(new Date());
            correctOrderSnapshotService.save(correctOrderSnapshot, CorrectOrderStatusEnum.OperateEnum.RECALL_AUTO);
            teacherManager.updateTeacherCurrentOrder(correctOrderSnapshot.getCorrectTeacherId(),
                    TeacherOrderTypeEnum.create(order.getType()), CorrectOrderStatusEnum.OperateEnum.RECALL_AUTO);
            log.info("撤回订单信息：{}", gson.toJson(order));
        }
    }

    @Override
    public List<CorrectOrder> findWaitDispatchOrderList() {
        List<Integer> orderTypes = Arrays.stream(TeacherOrderTypeEnum.values()).map(i -> i.getValue())
                .filter(essayTeacherService::checkCanCorrect)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderTypes)) {
            log.info("所有派单老师都已饱和，派单暂停");
            return Lists.newArrayList();
        }
        List<CorrectOrder> orders = correctOrderRepository.findByStatusAndBizStatusOrderByGmtCreateAsc(
                EssayStatusEnum.NORMAL.getCode(),
                CorrectOrderStatusEnum.INIT.getValue());
        return orders.stream().filter(i -> orderTypes.contains(i.getType()))
                .filter(dispatchFilterService::hasDispatchCount)
                .filter(dispatchFilterService::checkDispatchTime)
                .filter(dispatchFilterService::checkDispatchSnapshot)
                //.filter(order->order.getExercisesType() == CourseExerciseTypeEnum.normal.getCode())
                .collect(Collectors.toList());
    }

    /**
     * 实现派单
     *
     * @param correctOrder
     */
    @Override
    public void dispatch(CorrectOrder correctOrder) {
        long userId = correctOrder.getUserId();
        long teacherId = checkBackTeacherDispatch(correctOrder);
        if (teacherId > 0) {
            dispatch2Teacher(correctOrder, teacherId);
            return;
        }
        List<CorrectOrderSnapshot> operates = dispatchFilterService.findOperate(correctOrder.getId(), CorrectOrderStatusEnum.OperateEnum.DISPATCH_AUTO);
        List<Long> filterTeacher = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(operates)) {
            filterTeacher.addAll(operates.stream().map(CorrectOrderSnapshot::getCorrectTeacherId).collect(Collectors.toList()));
        }
        List<EssayTeacherOrderType> canCorrectTeachers = essayTeacherService.findCanCorrectTeachers(correctOrder.getType());
        if (CollectionUtils.isEmpty(canCorrectTeachers)) {
            return;
        }
        List<CorrectFeedBack> correctFeedBacks = correctFeedBackRepository.findByUserIdAndStatus(userId, EssayStatusEnum.NORMAL.getCode());

        if (CollectionUtils.isNotEmpty(correctFeedBacks)) {
            Map<Long, List<CorrectFeedBack>> feedBackMap = correctFeedBacks.stream()
                    .filter(i -> i.getTeacherId() > 0)
                    .filter(i -> i.getStar() > 0)
                    .filter(i -> !filterTeacher.contains(i.getTeacherId()))
                    .collect(Collectors.groupingBy(CorrectFeedBack::getTeacherId));
            if (MapUtils.isNotEmpty(feedBackMap)) {
                Map<Long, Double> filterMap = feedBackMap.entrySet().stream().collect(Collectors.toMap(i -> i.getKey(),
                        i -> i.getValue().stream().mapToDouble(CorrectFeedBack::getStar).average().getAsDouble()));
                teacherId = canCorrectTeachers.stream().filter(i -> null != filterMap.get(i.getTeacherId()))
                        .max(Comparator.comparing(i -> i.getTeacherId()))
                        .orElse(EssayTeacherOrderType.builder().teacherId(-1L).build())
                        .getTeacherId();
                log.info("评分策略派单老师：{}-->{}", correctOrder.getId(), teacherId);
            }
        }
        if (teacherId < 0) {
            List<CorrectOrder> correctOrders = correctOrderRepository.findByUserIdAndBizStatusIn(userId, Lists.newArrayList(
                    CorrectOrderStatusEnum.CORRECTED.getValue(),
                    CorrectOrderStatusEnum.FEEDBACK.getValue(),
                    CorrectOrderStatusEnum.WAIT_CORRECT.getValue()
            ));
            if (CollectionUtils.isNotEmpty(correctOrders)) {
                Map<Long, List<CorrectOrder>> orderMap = correctOrders.stream().collect(Collectors.groupingBy(i -> i.getReceiveOrderTeacher()));
                teacherId = canCorrectTeachers.stream().filter(i -> orderMap.containsKey(i.getId()))
                        .filter(i -> !filterTeacher.contains(i.getTeacherId()))
                        .max(Comparator.comparing(i -> orderMap.get(i.getId()).size()))
                        .orElse(EssayTeacherOrderType.builder().teacherId(-1L).build()).getTeacherId();
                log.info("历史订单策略派单老师：{}-->{}", correctOrder.getId(), teacherId);
            }
        }

        if (teacherId < 0) {
            teacherId = canCorrectTeachers.stream()
                    .filter(i -> !filterTeacher.contains(i.getTeacherId()))
                    .sorted(Comparator.comparing(i -> -i.getReceiptRate()))
                    .findFirst().orElse(EssayTeacherOrderType.builder().teacherId(-1L).build()).getTeacherId();
            log.info("平衡策略派单老师：{}-->{}", correctOrder.getId(), teacherId);
        }
        if (teacherId < 0) {
            log.error("订单{}暂无老师可派", correctOrder.getId());
            return;
        }
        dispatch2Teacher(correctOrder, teacherId);
    }

    private void dispatch2Teacher(CorrectOrder correctOrder, long teacherId) {
        correctOrder.setReceiveOrderTeacher(teacherId);
        correctOrder.setGmtModify(new Date());
        correctOrder.setBizStatus(CorrectOrderStatusEnum.WAIT_RECEIPT.getValue());
        correctOrderRepository.save(correctOrder);
        CorrectOrderSnapshot build = CorrectOrderSnapshot.builder().channel(CorrectOrderSnapshotChannelEnum.SYSTEM.getValue())
                .orderId(correctOrder.getId())
                .build();

        log.info("自动派单信息 = {}" + gson.toJson(correctOrder));
        build.setCreator("后台系统");
        //批改老师
        build.setCorrectTeacherId(teacherId);
        correctOrderSnapshotService.save(build, CorrectOrderStatusEnum.OperateEnum.DISPATCH_AUTO);
        teacherManager.updateTeacherCurrentOrder(teacherId,
                TeacherOrderTypeEnum.create(correctOrder.getType()), CorrectOrderStatusEnum.OperateEnum.DISPATCH_AUTO);
        //发送短信
        EssayTeacher teacher = teacherManager.getTeacherById(teacherId);
//        if (teacher.getPhoneNum() != null) {
//            MDSmsUtil.sendCorrectOrderMsg(teacher.getPhoneNum());
//        }
        if (teacher.getPhoneNum() != null) {
//            MDSmsUtil.sendCorrectOrderMsg(teacher.getPhoneNum());
            MDSmsUtil.sendMsg(teacher.getPhoneNum(),
                    String.format(MDSmsUtil.DISPATCH_ORDER_TEMPLATE,
                            Optional.ofNullable(correctOrder.getType())
                                    .map(TeacherOrderTypeEnum::create)
                                    .map(TeacherOrderTypeEnum::getTitle)
                                    .orElse("未知订单类型"),
                            correctOrderService.getQuestionName(correctOrder.getAnswerCardId(), correctOrder.getAnswerCardType())
                    ));
        }
    }

    /**
     * 检查是否有申请退单的日志，有，如果满足1订单最后一次派单操作，之后还未重新派单，则优先派给之前的被派单老师
     *
     * @param correctOrder
     */
    private Long checkBackTeacherDispatch(CorrectOrder correctOrder) {

        List<CorrectOrderSnapshot> backSnapshots = dispatchFilterService.findOperate(correctOrder.getId(), CorrectOrderStatusEnum.OperateEnum.RETURN_USER);
        if (CollectionUtils.isEmpty(backSnapshots)) {
            return -1L;
        }
        Date backDate = backSnapshots.stream().map(BaseEntity::getGmtCreate).max(Comparator.comparingLong(i -> i.getTime())).get();
        List<CorrectOrderSnapshot> operates = dispatchFilterService.findOperate(correctOrder.getId(), CorrectOrderStatusEnum.OperateEnum.DISPATCH_AUTO);
        if (CollectionUtils.isEmpty(operates)) {
            return -1L;
        }
        boolean present = operates.stream().filter(i -> i.getGmtCreate().after(backDate)).findAny().isPresent();    //是否存在退回后，派单日志
        if(present){
            return -1L;
        }
        Optional<EssayTeacherOrderType> first = operates.stream().sorted(Comparator.comparing(BaseEntity::getGmtCreate).reversed())
                .map(CorrectOrderSnapshot::getCorrectTeacherId)
                .map(i -> essayTeacherService.findTeacherOrderType(correctOrder.getType(), i))
                .filter(i -> null != i)
                .filter(i -> null == i.getReceiptRate() || i.getReceiptRate() < 100)
                .findFirst();
        if (first.isPresent()) {
            EssayTeacherOrderType essayTeacherOrderType = first.get();
            return essayTeacherOrderType.getTeacherId();
        }
        return -1L;
    }
}
