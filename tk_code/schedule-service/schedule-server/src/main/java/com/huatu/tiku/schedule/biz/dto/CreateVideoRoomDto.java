package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;

/**
 * 添加录播间
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class CreateVideoRoomDto implements Serializable {

    private static final long serialVersionUID = 6716444989419518761L;

    /**
     * 姓名
     */
    @NotEmpty(message = "名称不能为空")
    private String name;

    /**
     * 备注
     */
    private String mark;

}
