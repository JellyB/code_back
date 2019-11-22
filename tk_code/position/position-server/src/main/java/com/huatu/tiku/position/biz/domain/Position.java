package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import com.huatu.tiku.position.biz.enums.*;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**职位
 * @author wangjian
 **/
@Data
@Entity
public class Position extends BaseDomain {

    private static final long serialVersionUID = 5761385077585783338L;

    private String name;//拼接后职位名 完整名称

    private String nameStr;//职位名

    private String code;//职位代码

    private String attribute;//职位属性

    private String introduce;//职位介绍

    private String distribution;//职位分布

    private Integer number;//招录人数

    @Temporal(TemporalType.DATE)
    private Date beginDate;//报名开始日期

    @Temporal(TemporalType.DATE)
    private Date endDate;//报名结束日期

    private Integer year;//年份

    private PositionType type;//类型

    private String examType;//考试类别

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "areaId", insertable = false, updatable = false)
    private Area area;
    private Long areaId;//工作地点

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registerAreaId", insertable = false, updatable = false)
    private Area registerArea;//落户地点
    private Long registerAreaId;//落户地点

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "domicileAreaId", insertable = false, updatable = false)
//    private Area domicileArea;//户籍地
    private String domicileAreaIds;//户籍地

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "birthAreaId", insertable = false, updatable = false)
//    private Area birthArea;//出生地  生源
    private String birthAreaIds;//出生地

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(joinColumns = @JoinColumn(name = "positionId"), inverseJoinColumns = @JoinColumn(name = "specialtyId"))
    private List<Specialty> specialtys;//专业要求

    private String specialtyString;

    private Boolean recent;//是否应届 0为否 1是

    private Integer graduationYear;//毕业年份

    private String education;//学历

    private Education minEducation;//最小学历

    private Education maxEducation;//最大学历

    private Degree degree;//学位

    private String degreeString;//学位展示字符串

    private String political;//政治面貌

    private Exp exp;//工作经验要求

    private String baseExp;//基层工作经验

    private Sex sex;//性别

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentId", insertable = false, updatable = false)
    private Department department;//单位信息

    private Long departmentId;//单位信息

    private String requirementMark;//要求备注

    private String mark;//公告备注

    private Integer interviewScope;//面试分数

    private String proportion;//面试人员比例

    private Boolean extraFlag;//是否专业能力测试

    private String company;//用人司局

    private Integer enrolment;//报名人数

    private Nature nature;//性质 国考 省考

    private OrganizationType organization;//编制

    private String salary;//薪资

    private SchoolType schoolType;//公办民办

    @OneToMany(mappedBy = "positionId",cascade = CascadeType.REMOVE)
    private List<BrowseRecord> browseRecords;  //浏览记录 备注

    @OneToMany(mappedBy = "positionId")
    private List<Enroll> enrolls;  //意向报名

    private String nation;//民族

    private Integer birthYear;//出生年份

    private String certificate;//证书

    private String englishType;//英语水平类型

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "noticeId", insertable = false, updatable = false)
    private Notice notice;//对应公告

    private Long noticeId;//对应公告

    private Boolean isMark;//是否有额外备注要求

    @Temporal(TemporalType.DATE)
    private Date enrolmentEndDate;//报名结束日期

    private String enrolmentEndDateString;//报名结束日期

    private String departmentAttribute;//机构性质

    /**
     * 招收对象
     */
    private String requireTarget;

    /**
     * 年龄
     */
    private String ageStr;

    /**
     * 性别
     */
    private String sexStr;

    /**
     * 考试类别
     */
    private String examTypeStr;

    /**
     * 公共考试类别
     */
    private String commonExamTypeStr;

    /**
     * 专业考试类别
     */
    private String specialtyExamTypeStr;
}
