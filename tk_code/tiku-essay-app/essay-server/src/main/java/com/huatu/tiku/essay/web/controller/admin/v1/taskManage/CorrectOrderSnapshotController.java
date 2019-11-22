package com.huatu.tiku.essay.web.controller.admin.v1.taskManage;

import com.huatu.tiku.essay.service.v2.correctOrder.CorrectOrderSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/17
 * @描述 订单批注行为记录
 */
@RestController
@RequestMapping("/end/v1/snapshot")
public class CorrectOrderSnapshotController {


    @Autowired
    CorrectOrderSnapshotService correctOrderSnapshotService;

    /**
     * 任务ID
     *
     * @param taskId
     * @return
     */
    @GetMapping("list")
    public Object getCorrectOrderSnapshot(@RequestParam long taskId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int pageSize) {
        return correctOrderSnapshotService.getOrderSnapshot(taskId, page, pageSize);
    }


}
