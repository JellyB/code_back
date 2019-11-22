package com.huatu.tiku.interview.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/11 14:45
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {

    private long id;
    //用户姓名
    private String name;
    //用户准考证号
    private String examId;
    //用户手机号
    private String phone;
}
