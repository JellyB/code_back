package com.huatu.tiku.course.web.controller.v3;

import com.huatu.common.utils.collection.HashMapBuilder;
import com.huatu.tiku.course.netschool.api.v3.RefundServiceV3;
import com.huatu.tiku.course.util.RequestUtil;
import com.huatu.tiku.course.util.ResponseUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/28 13:11
 */
@RestController
@RequestMapping("/v3/refund")
public class RefundControllerV3 {
    @Autowired
    private RefundServiceV3 refundServiceV3;

    /**
     * 提交退款申请
     * @param ordernum
     * @param remark
     * @param userSession
     * @return
     */
    @PostMapping
    public Object submit(@RequestParam String ordernum,
                       @RequestParam String remark,
                       @Token UserSession userSession) {
        Map<String,Object> params = HashMapBuilder.newBuilder()
                .put("ordernum",ordernum)
                .put("remark",remark)
                .put("username",userSession.getUname())
                .buildUnsafe();
        return ResponseUtil.build(refundServiceV3.submitRefund(RequestUtil.encrypt(params)));
    }


    /**
     * 查看退款详情
     * @param ordernum
     * @param userSession
     * @return
     */
    @GetMapping
    public Object getRefundDetail(@RequestParam String ordernum,
                         @Token UserSession userSession) {
        Map<String,Object> params = HashMapBuilder.newBuilder()
                .put("ordernum",ordernum)
                .put("username",userSession.getUname())
                .buildUnsafe();
        return ResponseUtil.build(refundServiceV3.getRefundDetail(params));
    }
}
