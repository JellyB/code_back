package com.huatu.tiku.schedule.biz.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wangjian
 **/
@Data
public class DeleteVideoRoomDto implements Serializable{

    private static final long serialVersionUID = -2758384701339106568L;

    @NotNull(message = "录影棚ID不能为空")
    private Long roomId;

    @NotBlank(message = "验证码不能为空")
    private String code;

    @NotBlank(message = "删除原因不能为空")
    private String reason;
}
