package com.huatu.tiku.dto.request;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-09-20 下午6:55
 **/

@Getter
@Setter
@NoArgsConstructor
public class AppVersionDto implements Serializable{

    private Integer id;

    /**
     * 灰度发布 1 全部用户 2 白名单 3 取模随机
     */
    private Integer releaseType;

    /**
     * 取模随机值
     */
    private  Integer updateMode;

    @Builder
    public AppVersionDto(Integer id, Integer releaseType, Integer updateMode) {
        this.id = id;
        this.releaseType = releaseType;
        this.updateMode = updateMode;
    }
}
