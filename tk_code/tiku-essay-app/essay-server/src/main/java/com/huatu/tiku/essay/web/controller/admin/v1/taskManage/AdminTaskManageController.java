package com.huatu.tiku.essay.web.controller.admin.v1.taskManage;

import com.huatu.common.SuccessMessage;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.service.v2.CorrectFeedBackService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderBaseVo;
import com.huatu.tiku.essay.vo.req.CorrectOperateRep;
import com.huatu.tiku.essay.vo.req.CorrectOrderRep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/17
 * @描述 管理员任务管理
 */

@RestController
@Slf4j
@RequestMapping("/end/v1/task")
public class AdminTaskManageController {


    @Autowired
    CorrectOrderService correctOrderService;

    @Autowired
    CorrectFeedBackService correctFeedBackService;


    /**
     * 管理员～任务列表
     *
     * @param
     * @return
     */
    @LogPrint
    @GetMapping("list")
    public Object list(CorrectOrderRep correctOrderRep) {
        PageUtil<CorrectOrderBaseVo> list = correctOrderService.taskList(correctOrderRep);
        return list;
    }


    /**
     * 管理员～查看任务详情
     *
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping("lookTask")
    public Object lookTaskDetail(@RequestParam long id) {
        return correctOrderService.lookTaskDetail(id);
    }

    /**
     * 管理员～分配任务给(老师)
     *
     * @param taskId    任务ID
     * @param teacherId 老师ID
     * @return
     */
    @PostMapping("distribute")
    public Object distributeTask(@RequestParam long taskId,
                                 @RequestParam long teacherId,
                                 @RequestHeader String admin) {

        correctOrderService.distributeTask(taskId, teacherId, admin);
        return SuccessMessage.create("分配成功!");
    }

    /**
     * 查看评价
     *
     * @param answerId   答题卡ID
     * @param answerType 答题卡类型
     * @return
     */
    @GetMapping("feedBack")
    public Object lookFeedBack(@RequestParam long answerId,
                               @RequestParam int answerType) {
        return correctFeedBackService.findByAnswerId(answerId, answerType);
    }


    /**
     * 管理员～撤回任务（ 跟产品确认,是否需要撤回原因）
     *
     * @return
     */
    @PostMapping("cancel")
    public Object cancelTask(@RequestBody CorrectOperateRep correctOrderRep,
                             @RequestHeader String admin) {
        return correctOrderService.cancelTask(correctOrderRep, admin);
    }

    /**
     * 管理员～退回学员( 同样需要添加退回原因,客户端展示是以管理员的为准)
     *
     * @return
     */
    @LogPrint
    @PostMapping("return/user")
    public Object returnUser(@RequestBody CorrectOperateRep correctOrderRep,
                             @RequestHeader String admin) {
        correctOrderService.returnUser(correctOrderRep, admin);
        return SuccessMessage.create("操作成功");
    }


    /**
     * 管理员～再次批改
     *
     * @param orderId
     * @param teacherId 再次批改新老师的ID
     * @return
     */
    @PostMapping("reCorrect/{orderId}/{teacherId}")
    public Object reCorrect(@PathVariable long orderId,
                            @PathVariable long teacherId,
                            @RequestHeader String admin) {

        return correctOrderService.reCorrect(orderId, admin, teacherId);
    }

    /**
     * 管理员～驳回（老师退回）请求 (添加驳回原因)
     *
     * @param
     * @return
     */
    @PostMapping("reject/request")
    public Object rejectRequest(@RequestBody CorrectOperateRep correctOrderRep,
                                @RequestHeader String admin) {
        correctOrderService.rejectRequest(correctOrderRep, admin);
        return SuccessMessage.create("驳回成功!");
    }


}
