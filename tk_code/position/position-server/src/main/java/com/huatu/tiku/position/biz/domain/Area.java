package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import com.huatu.tiku.position.biz.enums.Status;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

/**面酷地区数据
 * @author wangjian
 **/
@Getter
@Setter
@Entity
public class Area extends BaseDomain {

    private static final long serialVersionUID = 1509670947743627884L;

//    @ManyToMany(mappedBy = "areas",fetch=FetchType.LAZY)
//    private Set<User> users;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     1. 省
     2. 市
     3. 县
     */
    private Integer type;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 父级地区
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId", insertable = false, updatable = false)
    private Area parentArea;

    /**
     * 父级id
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sorting;


    /**
     * 数据状态
     */
    private Status status;

    private Long provinceId;//所在省id
}
