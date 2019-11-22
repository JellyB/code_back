package com.huatu.tiku.essay.vo.req;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/10
 */
@Data
public class CreateOrUpdateTeacherReq {

    private Long teacherId = 0L;

    @NotNull(message = "uCenterId不能为空")
    private Long uCenterId;

    @NotNull(message = "uCenterName不能为空")
    private String uCenterName;

    @NotNull(message = "realName不能为空")
    private String realName;

    @NotNull(message = "nickName不能为空")
    private String nickName;

    @NotNull(message = "phoneNum不能为空")
    private String phoneNum;

    private String email;

    @NotNull(message = "teacherLevel不能为空")
    private Integer teacherLevel;

    @NotNull(message = "teacherStatus不能为空")
    private Integer teacherStatus;

    @NotNull(message = "department不能为空")
    private Integer department;

    @NotNull(message = "areaId不能为空")
    private Long areaId;

    private Long entryDate;

    @NotEmpty(message = "请选择批改类型")
    private List<Integer> correctType;

//    @NotEmpty(message = "请选择接单类型")
    private List<Integer> orderType;

    private Integer questionLimit;

    private Integer argumentLimit;

    private Integer practicalLimit;

    private Integer setQuestionLimit;

    private String bankName;

    private String bankBranch;

    private String bankAddress;

    private String idCard;

    private String bankUserName;

    private String bankNum;

}
