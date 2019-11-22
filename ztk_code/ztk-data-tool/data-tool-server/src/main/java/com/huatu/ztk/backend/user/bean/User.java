package com.huatu.ztk.backend.user.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huatu.ztk.backend.system.bean.Catgory;
import lombok.*;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-11-04 16:07
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class User {
    private int id;
    private String name;//用户真实名称
    private String account;
    @Getter(onMethod = @__({ @JsonIgnore}))
    private String password;
    private int successLoginCount;//成功登陆次数
    private String lastLoginIp;//最后登录ip
    private long lastLoginTime;//最后登录时间
    private int status;
    private List<Catgory> catgoryList;
}
