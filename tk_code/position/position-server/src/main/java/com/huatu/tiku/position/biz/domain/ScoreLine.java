package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import com.huatu.tiku.position.biz.enums.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**职位
 * @author wangjian
 **/
@Data
@Entity
public class ScoreLine extends BaseDomain {

    private static final long serialVersionUID = 5761385077585783338L;

    private String code;//职位代码

    private String nameStr;//职位名

    private String company;//用人司局

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentId", insertable = false, updatable = false)
    private Department department;//单位信息

    private Long departmentId;//单位信息

    private String proportion;//面试人员比例

    private Integer year;//年份

    private Double interviewScope;//面试分数

    private Integer number;//招录人数

    private Integer enrolment;//报名人数

}
