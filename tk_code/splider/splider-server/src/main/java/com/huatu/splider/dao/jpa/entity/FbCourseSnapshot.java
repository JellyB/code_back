package com.huatu.splider.dao.jpa.entity;
// Generated 2018-2-27 15:41:54 by Hibernate Tools 5.2.1.Final

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * FbCourseSnapshot generated by hbm2java
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "fb_course_snapshot")
public class FbCourseSnapshot implements java.io.Serializable {

    private Integer id;
    private Integer courseId;
    private String title;
    private BigDecimal price;
    private Integer studentCount;
    private Integer studentLimit;
    private Integer previousCount;
    private BigDecimal previoursPrice;
    private BigDecimal currentCycleIncome;
    private Date createTime;
    private Integer version;


    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "course_id")
    public Integer getCourseId() {
        return this.courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    @Column(name = "title")
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "price", precision = 10)
    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Column(name = "student_count")
    public Integer getStudentCount() {
        return this.studentCount;
    }

    public void setStudentCount(Integer studentCount) {
        this.studentCount = studentCount;
    }

    @Column(name = "student_limit")
    public Integer getStudentLimit() {
        return this.studentLimit;
    }

    public void setStudentLimit(Integer studentLimit) {
        this.studentLimit = studentLimit;
    }

    @Column(name = "previous_count")
    public Integer getPreviousCount() {
        return this.previousCount;
    }

    public void setPreviousCount(Integer previousCount) {
        this.previousCount = previousCount;
    }

    @Column(name = "previours_price", precision = 10)
    public BigDecimal getPrevioursPrice() {
        return this.previoursPrice;
    }

    public void setPrevioursPrice(BigDecimal previoursPrice) {
        this.previoursPrice = previoursPrice;
    }

    @Column(name = "current_cycle_income", precision = 10, scale = 0)
    public BigDecimal getCurrentCycleIncome() {
        return this.currentCycleIncome;
    }

    public void setCurrentCycleIncome(BigDecimal currentCycleIncome) {
        this.currentCycleIncome = currentCycleIncome;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", length = 19)
    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Column(name = "version")
    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}