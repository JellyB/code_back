package com.huatu.tiku.schedule.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


/**
 *  直播
 *
 *
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_schedule_live")
@EqualsAndHashCode(callSuper = false)
public class ScheduleLive extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 5107805263808846695L;
    /**
     * 课表id
     */
    private  Long scheduleId;/**

    /**
     * 上课时间
     */
    private Date date;

    /**
     * 地点id
     */
    private  Long placeId;



}
