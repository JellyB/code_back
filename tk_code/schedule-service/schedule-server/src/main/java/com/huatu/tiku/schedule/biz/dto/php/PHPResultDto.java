package com.huatu.tiku.schedule.biz.dto.php;


import lombok.Data;

import java.io.Serializable;

/**PHP返回dto
 * @author wangjian
 **/
@Data
public class PHPResultDto implements Serializable{

    private static final long serialVersionUID = -5821079898373021233L;

    private Integer code;

    private String msg;

    private PHPTeacherDto data;

    @Data
    public class PHPTeacherDto implements  Serializable{

        private static final long serialVersionUID = -7290721600701290510L;

        private Long pid;

        private String name;

        private Integer status;

        private String phone;

        private String examType;

        private String subjectId;

    }
}
