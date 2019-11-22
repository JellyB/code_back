package com.huatu.tiku.essay.entity;

import com.huatu.tiku.essay.essayEnum.DepartmentEnum;
import com.huatu.tiku.essay.essayEnum.TeacherLevelEnum;
import com.huatu.tiku.essay.essayEnum.TeacherStatusEnum;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by duanxiangchao on 2019/7/9
 */
@Entity
@Data
@Builder
@Table(name="v_essay_teacher")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class EssayTeacher extends BaseEntity implements Serializable {

    private String realName;

    private String nickName;

    private String phoneNum;

    private String email;

    /**
     *  1.名师 2.VIP总监 3.总监 4.高级
     */
    private Integer teacherLevel;

    /**
     *  1.使用 2.考核 3.正式 4.离职
     */
    private Integer teacherStatus;

    /**
     *  1.人大 2.党群 3.政协 4.综合
     */
    private Integer department;

    private Long areaId;

    private Date entryDate;

    private String correctType;

    /**
     * 教师评分
     */
    private BigDecimal teacherScore;

    private String bankName;

    private String bankBranch;

    private String bankAddress;

    private String idCard;

    private String bankUserName;

    private String bankNum;
    /**
     * 权限中心username
     */
    private String uCenterName;
    /**
     * 权限中心用户id
     */
    private Long  uCenterId; 

}
