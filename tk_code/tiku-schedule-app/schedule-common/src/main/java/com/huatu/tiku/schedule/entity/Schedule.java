package com.huatu.tiku.schedule.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 *  课表
 *
 *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_schedule")
@EqualsAndHashCode(callSuper = false)
public class Schedule extends BaseEntity implements Serializable {

    private static final long serialVersionUID = -4946021081754486129L;

    /**
     *文件路径
     */
    private String excelUrl;

    /**
     *状态
     */
    private String Status;

    /**
     *名称
     */
    private String name;

    /**
     * 录入人ID
     */
    private Long teacherId;
}
