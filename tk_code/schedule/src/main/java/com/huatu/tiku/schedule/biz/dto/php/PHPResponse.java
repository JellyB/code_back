package com.huatu.tiku.schedule.biz.dto.php;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * php返回数据
 * @author wangjian
 **/
@Data
public class PHPResponse implements Serializable{
    private static final long serialVersionUID = -4919879204213210175L;

    private Integer code;

    private String msg;

    private List<PHPUpdateTeacherDto> data;
}
