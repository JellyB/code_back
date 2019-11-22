package com.huatu.tiku.essay.web.controller.admin.v1.taskManage;

import com.ht.base.user.module.security.UserInfo;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.essay.entity.EssayTeacher;
import com.huatu.tiku.essay.manager.TeacherManager;
import com.huatu.tiku.essay.service.EssayTeacherService;
import com.huatu.tiku.essay.service.correct.CorrectOrderService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.correct.CorrectOrderBaseVo;
import com.huatu.tiku.essay.vo.req.CorrectOperateRep;
import com.huatu.tiku.essay.vo.req.CorrectOrderRep;
import com.huatu.tiku.essay.vo.req.TeacherOrderTypeReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/17
 * @描述 老师个人任务相关
 */
@RestController
@RequestMapping("/end/v1/task/teacher")
@Slf4j
public class TeacherTaskController {

    @Autowired
    private CorrectOrderService correctOrderService;

    @Autowired
    private TeacherManager teacherManager;

    @Autowired
    private EssayTeacherService essayTeacherService;

    /**
     * 老师接单
     *
     * @param
     * @return
     */
    @LogPrint
    @PutMapping("acceptOrder/{orderId}")
    public Object acceptOrder(@PathVariable long orderId, @RequestHeader String admin) {
        correctOrderService.acceptOrder(orderId, admin);
        return SuccessMessage.create("接单成功！");
    }

    /**
     * 拒绝接单
     *
     * @param correctOrderRep
     * @return
     */
    @LogPrint
    @PutMapping("refuseOrder")
    public Object refuseOrder(@RequestBody CorrectOperateRep correctOrderRep, @RequestHeader String admin) {
        correctOrderService.refuseOrder(correctOrderRep, admin, correctOrderRep.getOrderType());
        return SuccessMessage.create("拒绝成功！");
    }

    /**
     * 老师个人设置
     *
     * @param admin
     * @return
     */
    @LogPrint
    @GetMapping("settings")
    public Object settings(@RequestHeader String admin) {
        UserInfo userInfo = essayTeacherService.getUserInfo();
        String uCentername = userInfo.getUsername();
        log.info("uCentername:{}", uCentername);
        return essayTeacherService.getSettings(uCentername);
    }


    /**
     * 老师～退回订单
     *
     * @param
     * @return
     */
    @LogPrint
    @PutMapping("return/user")
    public Object returnUser(@RequestBody CorrectOperateRep correctOperateRep, @RequestHeader String admin) {
        UserInfo userInfo = essayTeacherService.getUserInfo();
        String uCentername = userInfo.getUsername();
        correctOrderService.returnOrder(correctOperateRep, uCentername);
        return SuccessMessage.create("退回成功！");
    }

    /**
     * 薪资列表
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return
     */
    @LogPrint
    @GetMapping("salary/list")
    public Object list(@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate) {
        UserInfo userInfo = essayTeacherService.getUserInfo();

        EssayTeacher teacher = essayTeacherService.getTeacherByUCenterId(userInfo.getId());

        return essayTeacherService.getSalaryList(teacher.getId(), startDate, endDate);
    }

    /**
     * 修改个人接单设置
     *
     * @param
     * @param
     * @param
     * @return
     */
    @LogPrint
    @PostMapping("settings")
    public Object updateSettings(@RequestBody List<TeacherOrderTypeReq> teacherOrderTypeReq) {
        return essayTeacherService.updateTeacherSettings(teacherOrderTypeReq);
    }


    /**
     * 老师～任务列表
     *
     * @param
     * @return
     */
    @LogPrint
    @GetMapping("list")
    public Object list(CorrectOrderRep correctOrderRep,
                       @RequestHeader String admin) {
        PageUtil<CorrectOrderBaseVo> list = correctOrderService.teacherTaskList(correctOrderRep, admin);
        return list;
    }
}
