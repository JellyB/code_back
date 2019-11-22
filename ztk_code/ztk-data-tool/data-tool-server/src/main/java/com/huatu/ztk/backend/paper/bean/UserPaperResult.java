package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by linkang on 3/5/17.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class UserPaperResult {
    private long uid;
    private String username;
    private String phone;
    private String province;
    private String city;
    private double score;
    private int rcount;
    private String utime;
    private Date createTime;
    private String cardId; //使用long，前端有问题
    private String email;
    private String nick;//用户昵称
}
