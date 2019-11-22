package com.huatu.tiku.position.biz.domain;

import com.huatu.tiku.position.base.domain.BaseDomain;
import com.huatu.tiku.position.biz.enums.DepartmentType;
import com.huatu.tiku.position.biz.enums.Nature;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**职位部门
 * @author wangjian
 **/
@Data
@Entity
public class Department extends BaseDomain {

    private static final long serialVersionUID = -8423546291801399627L;

    private String name;//部门名称

    private String code;//部门代码

    private String phone;//电话 多个电话拼接

    private String url;//部门网址

    private DepartmentType type;//机构层级

    private String attribute;//机构性质

    // 类型
    private Nature nature;
}
