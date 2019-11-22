package com.arj.monitor.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author zhouwei
 * @Description: 报警记录
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monitor_inform_record")
public class InformRecord extends BaseEntity {

    /**
     * 报警服务
     */
    @Column(columnDefinition = "bigint default 0")
    private long serverInfoId;
    /**
     * 报警原因
     */
    private String reason;
    /**
     * 报警类型 0 钉钉报警 1 短信报警
     */
    @Column(columnDefinition = "smallint default 0")
    private int alarmType;
    /**
     * yyyy
     */
    private int year;
    /**
     * yyyymm
     */
    private int month;
    /**
     * yyyymmdd
     */
    private int day;
    /**
     * yyyymmddhh
     */
    private int hour;
    /**
     * yyyymmddhhff
     */
    private long minute;
    /**
     * yyyymmddhhffss
     */
    private long second;

    private String url;

}
