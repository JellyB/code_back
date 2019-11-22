package com.huatu.tiku.schedule.biz.vo.Statistics;

import com.huatu.tiku.schedule.biz.enums.CourseCategory;
import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** body vo
 * @author wangjian
 **/
@Data
public class StatisticsVo  implements Serializable{

    private static final long serialVersionUID = 5593970769878150819L;

    private String date;//日期

    private String count;//实际时长

    private String time;//时间

    private String liveName;

    private String coefficient;

    private String categoryName;//类型

    private String courseName;//课程名

    private BigDecimal bigDecimal;//计算课时数

    private CourseCategory courseCategory;

    private CourseLiveCategory courseLiveCategory;

    private Integer type;

    private Double reallyExam;//真题题数

    private BigDecimal reallyHour;//真题课时

    private Double simulationExam;//模拟题数

    private BigDecimal simulationHour;//模拟题课时

    private BigDecimal articleHour;

    private BigDecimal audioHour;

    private String videoHour;//录播题课时
}
