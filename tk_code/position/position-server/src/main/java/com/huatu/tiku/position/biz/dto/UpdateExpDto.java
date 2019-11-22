package com.huatu.tiku.position.biz.dto;

import com.huatu.tiku.position.biz.enums.Exp;
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
public class UpdateExpDto implements Serializable{

    private static final long serialVersionUID = 4190878447380547551L;

    @NotNull(message = "工作经验不能为空")
    private Exp exp;//工作经验

}
