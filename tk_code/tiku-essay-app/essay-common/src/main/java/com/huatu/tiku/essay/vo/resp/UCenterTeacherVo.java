package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by duanxiangchao on 2019/7/19
 */
@Data
@JsonInclude
public class UCenterTeacherVo {

    private Long uCenterId;

    private String uCenterName;

    private String realName;

    private String phoneNum;

}
