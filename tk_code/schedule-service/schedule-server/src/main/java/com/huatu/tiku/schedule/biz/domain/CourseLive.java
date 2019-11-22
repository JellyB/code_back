package com.huatu.tiku.schedule.biz.domain;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.huatu.tiku.schedule.base.domain.BaseDomain;

import com.huatu.tiku.schedule.biz.enums.CourseLiveCategory;
import lombok.Getter;
import lombok.Setter;

/**
 * 课程直播
 *
 * @author Geek-S
 */
@Entity
@Getter
@Setter
public class CourseLive extends BaseDomain {

    private static final long serialVersionUID = 5730966574014090038L;

    /**
     * 所属课程
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courseId", insertable = false, updatable = false)
    private Course course;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 课程内容
     */
    private String name;

    /**
     * 日期
     */
    private Integer dateInt;

    /**
     * 日期
     */
    @Temporal(TemporalType.DATE)
    private Date date;

    /**
     * 开始时间
     */
    private Integer timeBegin;

    /**
     * 结束时间
     */
    private Integer timeEnd;

    /**
     * 录播间
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videoRoomId", insertable = false, updatable = false)
    private VideoRoom videoRoom;

    private Long videoRoomId;

    /**
     * 关联课程直播教师
     */
    @OneToMany(mappedBy = "courseLive", cascade = CascadeType.ALL)
    private List<CourseLiveTeacher> courseLiveTeachers;

    /**
     * 滚动排课关联
     */
    private Long sourceId;

    /**
     * 授课类型
     */
    private CourseLiveCategory courseLiveCategory;
}
