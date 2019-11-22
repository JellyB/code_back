package com.huatu.tiku.course.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author hanchao
 * @date 2017/12/13 9:34
 */
@Data
public class HighEndCsFormDTO {
    @JsonProperty("SN")
    private String SN;
    @JsonProperty("admission_student")
    private String admission_student;
    private String identifyID;
    private String nickName;
    private String phone;
    private String rid;
    private String sex;
    private String studentScore;

}
