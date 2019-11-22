package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class RewardMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bizId; //业务id
    private String action; //动作
    private long uid;
    private String uname;
    private long timestamp; //时间戳，毫秒
    private int gold; //大于0则优先使用
    private int experience; //经验,大于0则优先使用
}
