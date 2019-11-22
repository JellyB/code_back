package com.huatu.tiku.essay.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/2/6.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCorrectTimesOrderVO {
    /** 操作类型   1加批改次数  2 批改免费**/
    private int saveType;
    /** 课程id **/
    private Long courseId;

    /** 单题批改次数 **/
    private int queNum;
    /** 套题批改次数 **/
    private int mulNum;
    /** 单题批改次数 **/
    private int argNum;

    /* 白名单（无限次批改）失效时间 */
    private Long endTime;

}
