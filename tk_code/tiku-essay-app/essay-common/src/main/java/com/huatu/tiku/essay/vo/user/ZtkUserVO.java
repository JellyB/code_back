package com.huatu.tiku.essay.vo.user;

import lombok.Data;

/**
 * 用户信息
 *
 * @author geek-s
 * @date 2019-07-09
 */
@Data
public class ZtkUserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户手机
     */
    private String mobile;

    /**
     * 用户名
     */
    private String name;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 昵称
     */
    private String nick;
}
