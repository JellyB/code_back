package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import com.huatu.tiku.position.biz.enums.Education;
import com.huatu.tiku.position.biz.enums.Status;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**专业
 * @author wangjian
 **/
@Entity
@Getter
@Setter
public class Specialty extends BaseDomain {

    private static final long serialVersionUID = -2802433927814876995L;

    private Education education;  //学历

    private String name;//专业名

    /**
     * 级别
     1. 一级专业
     2. 二级专业
     */
    private Integer type;

    private Long parentId;//父级id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId", insertable = false, updatable = false)
    private Specialty parentSpecialty;//父级专业

    private Integer sorting;//排序

    private Status status;//数据状态

//    @ManyToMany(mappedBy = "specialtys")
//    private Set<Position> position;
}
