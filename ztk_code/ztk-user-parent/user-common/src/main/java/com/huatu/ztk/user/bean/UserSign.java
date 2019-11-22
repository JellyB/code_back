package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by linkang on 2017/10/16 下午2:35
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UserSign implements Serializable {
    private static final long serialVersionUID = 1L;

    private long uid;
    private Date signTime;
    private int type;
    private int number;
}
