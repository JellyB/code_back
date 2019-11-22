package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-12 下午7:32
 **/
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class SimpleUserDto implements Serializable {

    private long userId;

    private String userName;
}