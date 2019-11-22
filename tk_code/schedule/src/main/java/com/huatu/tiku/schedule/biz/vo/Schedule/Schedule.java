package com.huatu.tiku.schedule.biz.vo.Schedule;

import lombok.Data;

import java.io.Serializable;

/**教师课表返回vo
 * @author wangjian
 **/
@Data
public class Schedule implements Serializable{

    private static final long serialVersionUID = -5725177990421993059L;

    private String time;

    private String teacherNames;

    private String assistantName;

    private String ctrlName;

    private String ltName;

    private String compereName;

    private String liveName;

    private String courseName;

    private String examtypeSubject;

    private String categoryName;


}
