package com.huatu.ztk.user.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhengyi
 * @date 11/14/18 3:28 PM
 **/
@Data
@NoArgsConstructor
public class UserSearchRequest {
    private Long begin;
    private Long end;
    private String regFrom;
    private int page;
    private int size;
}