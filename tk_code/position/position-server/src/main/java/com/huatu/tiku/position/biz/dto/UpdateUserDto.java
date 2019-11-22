package com.huatu.tiku.position.biz.dto;

import com.huatu.tiku.position.biz.enums.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
public class UpdateUserDto implements Serializable{

    private static final long serialVersionUID = -8205768979211945937L;

    private Sex sex;//性别

    @NotNull(message = "专业不能为空")
    private Long specialtyId;//专业
    private Boolean recent;//是否应届 0为否 1是
    private Integer graduationYear;//毕业年份
    private Integer birthdayYear;//出生年
    private String nation;//民族

    @NotNull(message = "学历不能为空")
    private Education education;//学历
    private Degree degree;//学位
    private List<String> englishTypes;//最高英语水平类型
    private BaseExp baseExp;//基层工作经验

    @NotNull(message = "工作经验不能为空")
    private Exp exp;//工作经验
    private Political political;//政治面貌

    @NotEmpty(message = "报考地点不能为空")
    private List<Long> areaIds;//报考地点

    private Long registerAreaId;//户籍

    private Long birthAreaId;//出生地

    private List<String> certificates;//证书

}
