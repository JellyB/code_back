package com.huatu.tiku.common.bean.reward;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hanchao
 * @date 2017/10/12 20:15
 */
@Data
@Builder
public class RewardMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bizId; //业务id
    private String action; //动作
    private int gold; //大于0则优先使用
    private int experience; //经验,大于0则优先使用
    private int uid;
    private String uname;
    private long timestamp;
}
