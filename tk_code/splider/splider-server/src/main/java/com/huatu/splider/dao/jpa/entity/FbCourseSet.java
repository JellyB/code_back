package com.huatu.splider.dao.jpa.entity;
// Generated 2018-2-27 10:59:47 by Hibernate Tools 5.2.1.Final

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * FbCourseSet generated by hbm2java
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "fb_course_set")
public class FbCourseSet implements java.io.Serializable {

    private int id;
    private String title;
    private String tag;
    private String courseType;
    private Integer studentLimit;
    private Integer studentCount;
    private BigDecimal floorPrice;
    private BigDecimal topPrice;
    private Integer saleStatus;
    private Date classStartTime;
    private Date classStopTime;
    private Date startSaleTime;
    private Date stopSaleTime;
    private Integer teachChannel;
    private String courses;
    private String teachers;
    private Integer availableOunt;
    private Integer tradeUnit;
    private String rawData;
    private Date updateTime;
    private Boolean state;


    @Id

    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "title")
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "tag")
    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Column(name = "course_type")
    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    @Column(name = "student_limit")
    public Integer getStudentLimit() {
        return this.studentLimit;
    }

    public void setStudentLimit(Integer studentLimit) {
        this.studentLimit = studentLimit;
    }

    @Column(name = "student_count")
    public Integer getStudentCount() {
        return this.studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    @Column(name = "floor_price", precision = 10)
    public BigDecimal getFloorPrice() {
        return this.floorPrice;
    }

    public void setFloorPrice(BigDecimal floorPrice) {
        this.floorPrice = floorPrice;
    }

    @Column(name = "top_price", precision = 10)
    public BigDecimal getTopPrice() {
        return this.topPrice;
    }

    public void setTopPrice(BigDecimal topPrice) {
        this.topPrice = topPrice;
    }

    @Column(name = "sale_status")
    public Integer getSaleStatus() {
        return this.saleStatus;
    }

    public void setSaleStatus(Integer saleStatus) {
        this.saleStatus = saleStatus;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "class_start_time", length = 19)
    public Date getClassStartTime() {
        return this.classStartTime;
    }

    public void setClassStartTime(Date classStartTime) {
        this.classStartTime = classStartTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "class_stop_time", length = 19)
    public Date getClassStopTime() {
        return this.classStopTime;
    }

    public void setClassStopTime(Date classStopTime) {
        this.classStopTime = classStopTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_sale_time", length = 19)
    public Date getStartSaleTime() {
        return this.startSaleTime;
    }

    public void setStartSaleTime(Date startSaleTime) {
        this.startSaleTime = startSaleTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "stop_sale_time", length = 19)
    public Date getStopSaleTime() {
        return this.stopSaleTime;
    }

    public void setStopSaleTime(Date stopSaleTime) {
        this.stopSaleTime = stopSaleTime;
    }

    @Column(name = "teach_channel")
    public Integer getTeachChannel() {
        return this.teachChannel;
    }

    public void setTeachChannel(Integer teachChannel) {
        this.teachChannel = teachChannel;
    }

    @Column(name = "courses")
    public String getCourses() {
        return this.courses;
    }

    public void setCourses(String courses) {
        this.courses = courses;
    }

    @Column(name = "teachers")
    public String getTeachers() {
        return this.teachers;
    }

    public void setTeachers(String teachers) {
        this.teachers = teachers;
    }

    @Column(name = "available_ount")
    public Integer getAvailableOunt() {
        return this.availableOunt;
    }

    public void setAvailableOunt(Integer availableOunt) {
        this.availableOunt = availableOunt;
    }

    @Column(name = "trade_unit")
    public Integer getTradeUnit() {
        return this.tradeUnit;
    }

    public void setTradeUnit(Integer tradeUnit) {
        this.tradeUnit = tradeUnit;
    }

    @Column(name = "raw_data")
    public String getRawData() {
        return this.rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time", nullable = false, length = 19)
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Column(name = "state")
    public Boolean getState() {
        return this.state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

}