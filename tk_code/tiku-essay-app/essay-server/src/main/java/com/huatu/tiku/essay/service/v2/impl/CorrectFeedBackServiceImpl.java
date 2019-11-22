package com.huatu.tiku.essay.service.v2.impl;

import com.google.common.collect.Lists;
import com.huatu.common.SuccessMessage;
import com.huatu.common.exception.BizException;
import com.huatu.common.spring.event.EventPublisher;
import com.huatu.tiku.common.bean.reward.RewardMessage;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.common.consts.RabbitConsts;
import com.huatu.tiku.essay.constant.error.EssayErrors;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.entity.correct.CorrectFeedBack;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.entity.correct.CorrectOrderSnapshot;
import com.huatu.tiku.essay.essayEnum.CorrectOrderSnapshotChannelEnum;
import com.huatu.tiku.essay.essayEnum.CorrectOrderStatusEnum;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.essayEnum.OrderFeedBackEnum;
import com.huatu.tiku.essay.manager.TeacherManager;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.repository.v2.EssayCorrectFeedBackRepository;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.v2.CorrectFeedBackService;
import com.huatu.tiku.essay.service.v2.correctOrder.CorrectOrderSnapshotService;
import com.huatu.tiku.essay.vo.admin.FeedBackStatisticVO;
import com.huatu.tiku.essay.vo.admin.correct.CorrectFeedBackVo;
import com.huatu.tiku.springboot.basic.reward.RewardAction;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/9
 * @描述 学员人工批改意见反馈
 */
@Service
public class CorrectFeedBackServiceImpl implements CorrectFeedBackService {

    private static final Logger logger = LoggerFactory.getLogger(CorrectFeedBackServiceImpl.class);

    @Autowired
    EssayCorrectFeedBackRepository correctFeedBackRepository;

    @Autowired
    CorrectOrderRepository correctOrderRepository;
    @Autowired
    private TeacherManager teacherManager;
    @Autowired
    CorrectOrderSnapshotService correctOrderSnapshotService;

    @Autowired
    EssayTeacherService essayTeacherService;

    @Autowired
    EventPublisher eventPublisher;

    @Autowired
    RabbitTemplate rabbitTemplate;

    //异步方法
    public Object save(CorrectFeedBack correctFeedBack, UserSession userSession) {
        List<CorrectFeedBackVo> correctFeedBacks = findByAnswerId(correctFeedBack.getAnswerId(), correctFeedBack.getAnswerType());
        if (CollectionUtils.isNotEmpty(correctFeedBacks)) {
            throw new BizException(EssayErrors.HAVE_FEEDBACK_STATUS);
        }
        CorrectFeedBackVo correctFeedBackVo = new CorrectFeedBackVo();
        BeanUtils.copyProperties(correctFeedBack, correctFeedBackVo);
        CorrectOrder correctOrder = correctOrderRepository.findByAnswerCardIdAndAnswerCardTypeAndStatus(correctFeedBack.getAnswerId(),
                correctFeedBack.getAnswerType(), EssayStatusEnum.NORMAL.getCode());
        correctFeedBack.setOrderId(correctOrder.getId());
        correctFeedBack.setTeacherId(correctOrder.getReceiveOrderTeacher());
        correctFeedBack.setStatus(EssayStatusEnum.NORMAL.getCode());
        correctFeedBackRepository.save(correctFeedBack);
        //更新其他状态内容
        updateFeedBackStatus(correctOrder, correctFeedBackVo, userSession);
        return SuccessMessage.create("反馈成功!");
    }

    @Async
    public void updateFeedBackStatus(CorrectOrder order, CorrectFeedBackVo correctFeedBackVo, UserSession userSession) {
        //2.更新correctOrder是否评价字段,bizStatus为7
        correctOrderRepository.updateFeedBackStatus(correctFeedBackVo.getAnswerId(),
                correctFeedBackVo.getAnswerType(), OrderFeedBackEnum.FEED_BACK_FINISH.getValue(),
                CorrectOrderStatusEnum.FEEDBACK.getValue());

        //3.需要在工单流转表（CorrectOrderSnapshot）中添加一条评价记录
        if (null != order) {
            CorrectOrderSnapshot correctOrderSnapshot = CorrectOrderSnapshot.builder()
                    .orderId(order.getId())
                    .channel(CorrectOrderSnapshotChannelEnum.MEMBER.getValue())
                    .description(correctFeedBackVo.getContent())
                    .correctTeacherId(order.getReceiveOrderTeacher())
                    .build();
            correctOrderSnapshot.setCreator(userSession.getUname());
            correctOrderSnapshotService.save(correctOrderSnapshot, CorrectOrderStatusEnum.OperateEnum.FEED_BACK);
        }

        //4.计算老师批改平均分
        teacherScore(order.getReceiveOrderTeacher());
        //5.赠送金币(php需要添加名称)
        RewardMessage msg = RewardMessage.builder().gold(10).action(RewardAction.ActionType.ESSAY_CORRECT_FEEDBACK.name())
                .experience(1).bizId(System.currentTimeMillis() + "").uname(userSession.getUname())
                .timestamp(System.currentTimeMillis()).build();
        rabbitTemplate.convertAndSend("", RabbitConsts.QUEUE_REWARD_ACTION, msg);
    }


    /**
     * @description: 计算老师平均分
     * @author duanxiangchao
     * @date 2019/7/30 3:04 PM
     */
    private void teacherScore(Long teacherId) {
        FeedBackStatisticVO vo = correctFeedBackRepository.statisticTeacherScore(teacherId);
        if (vo.getStar() != null) {
            EssayTeacher teacher = teacherManager.getTeacherById(teacherId);
            teacher.setTeacherScore(new BigDecimal(vo.getStar()).divide(new BigDecimal(vo.getIds()), 1, BigDecimal.ROUND_HALF_UP));
            teacherManager.saveTeacher(teacher);
        }
    }

    /**
     * 根据答题卡ID
     *
     * @param answerId
     * @return
     */
    public List<CorrectFeedBackVo> findByAnswerId(long answerId, int answerType) {
        List<CorrectFeedBack> correctFeedBackList = correctFeedBackRepository.findByAnswerIdAndStatus(answerId, EssayStatusEnum.NORMAL.getCode());

        if (CollectionUtils.isEmpty(correctFeedBackList)) {
            return Lists.newArrayList();
        }
        List<CorrectFeedBackVo> list = new ArrayList<>();
        correctFeedBackList.forEach(feedBack -> {
            CorrectFeedBackVo correctFeedBackVo = CorrectFeedBackVo.builder().star(feedBack.getStar())
                    .content(feedBack.getContent())
                    .build();
            list.add(correctFeedBackVo);
        });
        return list.subList(0,1);
    }


}
