package com.huatu.one.biz.model;

import com.huatu.one.base.model.BaseModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 课程排名考试类型
 *
 * @author geek-s
 * @date 2019-09-18
 */
@Getter
@Setter
@ToString
public class UserCourseRanking extends BaseModel {

    /**
     * 微信ID
     */
    private String openid;

    /**
     * 考试类型ID
     */
    private Long examTypeId;
}
