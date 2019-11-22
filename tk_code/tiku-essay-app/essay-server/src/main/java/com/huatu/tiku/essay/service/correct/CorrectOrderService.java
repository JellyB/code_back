package com.huatu.tiku.essay.service.correct;

import java.util.Date;

import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderBaseVo;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderDetailVo;
import com.huatu.tiku.essay.vo.req.CorrectOperateRep;
import com.huatu.tiku.essay.vo.req.CorrectOrderRep;

public interface CorrectOrderService {

    CorrectOrder findByAnswerId(long answerCardId, EssayAnswerCardEnum.TypeEnum paper);

    /**
     * 完成订单批注
     *
     * @param answerId
     * @param question
     */
    void finished(Long answerId, EssayAnswerCardEnum.TypeEnum question);

    /**
     * 开始订单批改
     *
     * @param questionAnswerCardId
     * @param question
     */
    void startLabel(long questionAnswerCardId, EssayAnswerCardEnum.TypeEnum question);

    /**
     * 创建订单
     *
     * @param correctOrder
     */
    void createOrder(CorrectOrder correctOrder);


    /**
     * 任务管理,查看任务列表
     */
    PageUtil<CorrectOrderBaseVo> taskList(CorrectOrderRep correctOrderRep);

    /**
     * 任务管理,查看任务
     *
     * @param id
     * @return
     */
    CorrectOrderDetailVo lookTaskDetail(long id);

    /**
     * @param taskId    任务ID
     * @param teacherId 老师ID
     * @return
     */
    Object distributeTask(long taskId, long teacherId, String admin);

    /**
     * 老师点击接单
     *
     * @param orderId 订单id
     * @param admin   老师名称
     * @return
     */
    Object acceptOrder(long orderId, String admin);

    /**
     * 老师有点不接
     *
     * @param correctOrderRep
     * @param admin
     * @return
     */
    Object refuseOrder(CorrectOperateRep correctOrderRep, String admin, Integer orderType);

    /**
     * 管理员-退回学员
     *
     * @return
     */
    Object returnUser(CorrectOperateRep correctOrderRep, String admin);

    /**
     * 管理员～撤回任务
     */
    Object cancelTask(CorrectOperateRep correctOperateRep, String admin);

    /**
     * 再次批改
     *
     * @param taskId
     * @return
     */
    Object reCorrect(long taskId, String admin,long teacherId);

    /**
     * 管理员～驳回（老师退回）请求
     *
     * @param correctOrderRep
     * @return
     */
    void rejectRequest(CorrectOperateRep correctOrderRep, String admin);

    /**
     * 下一个未完成订单
     *
     * @param answerCardId
     * @param typeEnum
     * @return
     */
    CorrectOrder getNext(long answerCardId, EssayAnswerCardEnum.TypeEnum typeEnum);


    /**
     * 老师～查看任务列表
     */
    PageUtil<CorrectOrderBaseVo> teacherTaskList(CorrectOrderRep correctOrderRep,String admin);

    /**
     * 计算订单超时时间
     * @param correctOrderType
     * @param delayStatus
     * @return
     */
	Date calculateDeadLine(int correctOrderType, int delayStatus);

	/**
	 * 老师退回学员
	 * @param correctOperateRep
	 * @return
	 */
	int returnOrder(CorrectOperateRep correctOperateRep, String ucName);

    /**
     * 人工批改完成消息处理
     * @param answerId
     * @param typeEnum
     */
    void sendManualCorrectMessage(long answerId, EssayAnswerCardEnum.TypeEnum typeEnum);

    String getQuestionName(long answerCardId, int answerCardType);
}
