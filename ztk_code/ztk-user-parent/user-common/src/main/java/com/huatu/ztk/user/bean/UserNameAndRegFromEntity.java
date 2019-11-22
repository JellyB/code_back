package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhengyi
 * @date 11/14/18 3:43 PM
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNameAndRegFromEntity {
    private String name;
    private int regFrom;
}