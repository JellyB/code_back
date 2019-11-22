package com.huatu.tiku.position.biz.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.huatu.tiku.position.biz.domain.Position;
import com.huatu.tiku.position.biz.enums.Nature;
import com.huatu.tiku.position.biz.enums.OrganizationType;
import com.huatu.tiku.position.biz.enums.SchoolType;
import com.huatu.tiku.position.biz.util.DateformatUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
@Getter
@Setter
@NoArgsConstructor
public class PositionVo implements Serializable {

    private static final long serialVersionUID = 6057056265690229651L;

    private Long id;

    private String code;//职位代码

    private String name;//职位名

    @JsonIgnore
    private String nameStr;//职位名

    private String attribute;//职位属性

    private String introduce;//职位介绍

    private String distribution;//职位分布

    private Integer number;//招录人数

    private Date beginDate;//报名开始日期

    private Date endDate;//报名结束日期

    private Integer year;//年份

    private String type;//类型

    private String area;

    private String registerArea;//落户地点

    private String domicileArea;//户籍地

//    private String specialtys;//专业要求

    private String specialtyString;//专业字符串

    private Boolean recent;//是否应届 0为否 1是

    private String education;//学历

    private String degree;//学位

    private String degreeString;//学位展示字符串

    private String political;//政治面貌

    private String exp;//工作经验要求

    private String baseExp;//基层工作经验

    private String sex;//性别

    private DepartmentVo department;//单位信息

    private String requirementMark;//要求备注

    private String mark;//公告备注

    //    private String status;//状态
    private Integer status;//状态

    private Double similarityScope;//相似度评分

    private String proportion;//面试人员比例

    private Boolean extraFlag;//专业能力测试

    private String company;//用人司局

    private Integer enrolment;//报名人数

    private Double start;//推荐星级

    private Nature nature;//性质 国考 省考

    private OrganizationType organization;//编制

    private String salary;//薪资

    private SchoolType schoolType;//公办民办

    private Integer browseCount;//浏览量

    private Integer enrollCount;//意向报名人数

    private String nation;//民族

    private Integer birthYear;//出生年份

    private List<String> label;//标签

    private List<String> certificates;//证书

    private String englishType;//英语水平类型

    private String enrolmentEndDate;//报名结束日期

    private Boolean isMark;

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

    public PositionVo(Position position) {
        if (null != position) {
            BeanUtils.copyProperties(position, this);
            this.enrolmentEndDate = position.getEnrolmentEndDateString();
            this.type = null == position.getType() ? null : position.getType().getText();
            this.degree = null == position.getDegree() ? null : position.getDegree().getText();
            this.exp = null == position.getExp() ? null : position.getExp().getText();
            this.sex = null == position.getSex() ? null : position.getSex().getText();
            Date nowDate = new Date();
            if (nowDate.before(position.getBeginDate())) {
                status = 0;//未开始
            } else if (nowDate.after(position.getEndDate())) {
                status = 2;//已结束
                String positionEndDate = DateformatUtil.format0(position.getEndDate());
                String nowEndDate = DateformatUtil.format0(nowDate);
                if (nowEndDate.equals(positionEndDate)) {
                    status = 1;
                }
            } else {
                status = 1;
            }
            this.similarityScope = (double) 0;
            String certificate = position.getCertificate();//证书
            if (StringUtils.isNotBlank(certificate)) {
                String[] split = certificate.split(",");
                List<String> certificateList = new ArrayList<>();
                Collections.addAll(certificateList, split);
                this.certificates = certificateList;
            }
        }
    }

    public PositionVo(Position position, Boolean flag) {
        this(position);
        if (null != position && flag) {
            this.area = null == position.getArea() ? null : position.getArea().getName();
            this.registerArea = null == position.getRegisterArea() ? null : position.getRegisterArea().getName();
            this.department = null == position.getDepartment() ? null : new DepartmentVo(position.getDepartment());
            if (this.department != null && StringUtils.isNotBlank(position.getDepartmentAttribute())) {
                department.setAttribute(position.getDepartmentAttribute());
            }
            if (null != position.getBrowseRecords()) {
                this.browseCount = position.getBrowseRecords().size();
            } else {
                this.browseCount = 0;
            }
        }
    }
}
