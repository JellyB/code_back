package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by x6 on 2018/1/25.
 * 班级信息
 */

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="t_class_info")
public class ClassInfo   extends BaseEntity  implements Serializable {

    //班级名称
    private String name;
    //班级序号
    private Integer sort;
    //辅导老师
    private String teacher;
    //班级签到时间
    private String signInTime;

    //班级时长
    private Integer dayCount;

    private Date startTime;

    private Date endTime;

    //班级所属地区
    private Long areaId;

}
