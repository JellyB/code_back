package com.huatu.tiku.essay.web.controller.admin.v1;

import com.google.common.collect.Lists;
import com.huatu.common.SuccessMessage;
import com.huatu.tiku.essay.constant.status.EssayGoodsOrderConstant;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderRefundAuditDto;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderRefundDto;
import com.huatu.tiku.essay.dto.AdminEssayGoodsOrderRefundListDto;
import com.huatu.tiku.essay.essayEnum.EssayGoodsOrderRefundStatusEnum;
import com.huatu.tiku.essay.service.EssayGoodsOrderRefundService;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderRefundListVO;
import com.huatu.tiku.essay.vo.admin.AdminEssayGoodsOrderRefundRecordVO;
import com.huatu.tiku.essay.vo.admin.AdminOptionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品订单退款记录
 *
 * @author geek-s
 * @date 2019-07-08
 */
@RestController
@RequestMapping("end/v1/goodsOrderRefund")
@Slf4j
public class EssayGoodsOrderRefundControllerV1 {

    @Autowired
    private EssayGoodsOrderRefundService goodsOrderRefundService;

    /**
     * 申请退款
     *
     * @param goodsOrderRefundDto 退款信息
     * @return 订单详情
     */
    @PostMapping
    public SuccessMessage refund(@RequestBody @Validated AdminEssayGoodsOrderRefundDto goodsOrderRefundDto) {
        goodsOrderRefundService.refund(goodsOrderRefundDto);

        return SuccessMessage.create();
    }

    /**
     * 申请退款列表
     *
     * @param query    查询条件
     * @param page     页数
     * @param pageSize 条数
     * @return 退款列表
     */
    @GetMapping
    public PageUtil<AdminEssayGoodsOrderRefundListVO> list(@Validated AdminEssayGoodsOrderRefundListDto query,
                                                           @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                           @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        return goodsOrderRefundService.list(query, page, pageSize);
    }

    /**
     * 通过
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PutMapping("pass1/{id}")
    public SuccessMessage pass1(@PathVariable Long id, @RequestBody AdminEssayGoodsOrderRefundAuditDto auditDto) {
        Assert.notNull(id, "ID不能为空");

        goodsOrderRefundService.audit(id, auditDto == null ? null : auditDto.getRemark(), EssayGoodsOrderRefundStatusEnum.TODO, EssayGoodsOrderRefundStatusEnum.PASS1);

        return SuccessMessage.create();
    }

    /**
     * 驳回
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PutMapping("deny1/{id}")
    public SuccessMessage deny1(@PathVariable Long id, @RequestBody AdminEssayGoodsOrderRefundAuditDto auditDto) {
        Assert.notNull(id, "ID不能为空");
        Assert.isTrue(!StringUtils.isEmpty(auditDto.getRemark()), "备注不能为空");

        goodsOrderRefundService.audit(id, auditDto.getRemark(), EssayGoodsOrderRefundStatusEnum.TODO, EssayGoodsOrderRefundStatusEnum.DENY1);

        return SuccessMessage.create();
    }

    /**
     * 通过
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PutMapping("pass2/{id}")
    public SuccessMessage pass2(@PathVariable Long id, @RequestBody AdminEssayGoodsOrderRefundAuditDto auditDto) {
        Assert.notNull(id, "ID不能为空");

        goodsOrderRefundService.audit(id, auditDto == null ? null : auditDto.getRemark(), EssayGoodsOrderRefundStatusEnum.PASS1, EssayGoodsOrderRefundStatusEnum.PASS2);

        return SuccessMessage.create();
    }

    /**
     * 驳回
     *
     * @param id 记录ID
     * @return 操作结果
     */
    @PutMapping("deny2/{id}")
    public SuccessMessage deny2(@PathVariable Long id, @RequestBody AdminEssayGoodsOrderRefundAuditDto auditDto) {
        Assert.notNull(id, "ID不能为空");
        Assert.isTrue(!StringUtils.isEmpty(auditDto.getRemark()), "备注不能为空");

        goodsOrderRefundService.audit(id, auditDto.getRemark(), EssayGoodsOrderRefundStatusEnum.PASS1, EssayGoodsOrderRefundStatusEnum.DENY2);

        return SuccessMessage.create();
    }

    /**
     * 订单退款状态字典
     *
     * @return 订单退款状态字典
     */
    @GetMapping("bizStatusDic")
    public List<AdminOptionVO> bizStatusDic() {
        List<AdminOptionVO> optionVOS = Lists.newArrayListWithExpectedSize(EssayGoodsOrderConstant.EssayGoodsOrderStatusEnum.values().length);

        for (EssayGoodsOrderRefundStatusEnum goodsOrderRefundStatusEnum : EssayGoodsOrderRefundStatusEnum.values()) {
            optionVOS.add(AdminOptionVO.builder()
                    .text(goodsOrderRefundStatusEnum.getValue())
                    .value(goodsOrderRefundStatusEnum.ordinal())
                    .build());
        }

        return optionVOS;
    }

    /**
     * 申请退款列表
     *
     * @param orderId 订单ID
     * @return 退款列表
     */
    @GetMapping("listByOrderId")
    public List<AdminEssayGoodsOrderRefundRecordVO> listlistByOrderId(Long orderId) {
        return goodsOrderRefundService.listByOrderId(orderId);
    }

    /**
     * PHP课程赠送订单申请退款
     *
     * @param orderId 订单ID
     * @return 扣除金额
     */
    @PostMapping("preRefundPHP")
    public Object preRefundPHP(Long orderId, String userName) {
        Assert.notNull(orderId, "订单ID不能为空");
        Assert.isTrue(!StringUtils.isEmpty(userName), "用户名不能为空");

        goodsOrderRefundService.preRefundByCourseOrderId(orderId, userName);

        return SuccessMessage.create();
    }

    /**
     * PHP课程赠送订单确认退款
     *
     * @param orderId 订单ID
     * @return 扣除金额
     */
    @PostMapping("refundPHP")
    public Object refundPHP(Long orderId, String userName) {
        Assert.notNull(orderId, "订单ID不能为空");
        Assert.isTrue(!StringUtils.isEmpty(userName), "用户名不能为空");

        goodsOrderRefundService.refundByCourseOrderId(orderId, userName);

        return SuccessMessage.create();
    }

    /**
     * PHP课程赠送订单申请退款
     *
     * @param orderId 订单ID
     * @return 扣除金额
     */
    @PostMapping("cancelRefundPHP")
    public Object cancelRefundPHP(Long orderId, String userName) {
        Assert.notNull(orderId, "订单ID不能为空");
        Assert.isTrue(!StringUtils.isEmpty(userName), "用户名不能为空");

        goodsOrderRefundService.cancelRefundByCourseOrderId(orderId, userName);

        return SuccessMessage.create();
    }
}