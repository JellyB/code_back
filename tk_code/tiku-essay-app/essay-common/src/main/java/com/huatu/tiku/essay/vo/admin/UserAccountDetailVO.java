package com.huatu.tiku.essay.vo.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by x6 on 2017/12/18.
 */
@Data
public class UserAccountDetailVO implements Serializable {
    private  int UserMoney;
    private  int UserPoint;
}
