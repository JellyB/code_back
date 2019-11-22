package com.huatu.tiku.essay.essayEnum;

import com.huatu.tiku.essay.util.enu.EnumUtils;
import com.huatu.tiku.essay.util.enu.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by duanxiangchao on 2019/7/15
 */
public enum CorrectOrderStatusEnum implements IEnum<Integer> {
    INIT(0, "待分配"),
    WAIT_RECEIPT(1, "待接单"),
    WAIT_CORRECT(2, "待批改"),
    ON_GOING(3, "批改中"),
    CORRECTED(4, "已批改"),
    WAIT_BACK(5, "待退回"),
    BACKED(6, "已退回"),
    FEEDBACK(7, "已评价");

    private int value;
    private String title;

    public static CorrectOrderStatusEnum create(Integer value) {
        return (CorrectOrderStatusEnum) EnumUtils.getEnum(values(), value);
    }

    public static List<Integer> getUnFinishedCorrectStatus() {
        CorrectOrderStatusEnum[] values = CorrectOrderStatusEnum.values();
        List<Integer> collect = Arrays.stream(values).filter(statusEnum -> statusEnum.getValue() != CorrectOrderStatusEnum.CORRECTED.getValue() &&
                statusEnum.getValue() != CorrectOrderStatusEnum.FEEDBACK.getValue())
                .map(CorrectOrderStatusEnum::getValue)
                .collect(Collectors.toList());
        return collect;
    }

    private CorrectOrderStatusEnum(int value, String title) {
        this.value = value;
        this.title = title;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getTitle() {
        return this.title;
    }

    /**
     * 订单行为枚举（日志表记录操作行为使用）
     */
    @AllArgsConstructor
    @Getter
    public enum OperateEnum {
        INIT(0, "初始化"),
        DISPATCH_AUTO(1, "自动派单"),
        DISPATCH_MANUAL(2, "人工派单"),
        RECEIPT(3, "接单"),
        START_CORRECT(4, "开始批改"),
        END_CORRECT(5, "结束批改"),
        APPLY_BACK(6, "申请退回订单"),
        REJECT_BACK(7, "拒绝订单退回申请"),
        ALLOW_BACK(8, "通过订单退回申请"),  //TODO  该操作和11是同样的操作，不做区分
        RECALL(9, "撤回订单到待分配"),
        FEED_BACK(10, "学员订单评价"),
        RETURN_USER(11, "管理员退回学员"),
        RECALL_AUTO(12, "系统自动撤回订单"),;

        private int code;
        private String name;
    }

    /**
     * 批改订单是否顺延状态
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    public enum DelayStatusEnum {
        YES(1, "顺延"),
        NO(0, "非顺延");
        private int code;
        private String name;
    }
}
