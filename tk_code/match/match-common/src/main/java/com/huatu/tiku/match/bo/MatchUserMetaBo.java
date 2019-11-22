package com.huatu.tiku.match.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 描述：用户统计信息封装
 *
 * @author biguodong
 * Create time 2018-10-18 下午3:25
 **/

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchUserMetaBo implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * 模考大赛ID
     */
    private Integer matchId;
    /**
     * 用户id
     */
    private Integer userId;
    /**
     * 职位id
     */
    private Integer positionId;
    /**
     * 职位名称
     */
    private String positionName;
    /**
     * 职位报名人数
     */
    private int positionCount;
    /**
     * 练习id
     */
    private Long practiceId;
    /**
     * 学校名称
     */
    private String schoolName;
    /**
     * 学校id
     */
    private Long schoolId;

    /**
     * 是否交卷
     */
    private boolean submitFlag;
}
