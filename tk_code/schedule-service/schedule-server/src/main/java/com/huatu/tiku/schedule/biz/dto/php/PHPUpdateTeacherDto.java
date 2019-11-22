package com.huatu.tiku.schedule.biz.dto.php;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**PHP的修改教师参数
 * @author wangjian
 **/
@Data
public class PHPUpdateTeacherDto implements Serializable{
    private static final long serialVersionUID = -8994240311074294710L;

    @NotNull(message = "pid不能为空")
    private Long pid;

    @NotBlank(message = "教师名称不能为空")
    private String name;

    private Integer status;

    private String phone;

}
