package com.huatu.tiku.position.biz.vo;

import com.google.common.collect.Lists;
import com.huatu.tiku.position.biz.domain.Department;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**部门vo
 * @author wangjian
 **/
@Getter
@Setter
public class DepartmentVo implements Serializable{

    private static final long serialVersionUID = 2512980908962286370L;

    private Long id;//id

    private String name;//部门名称

    private String code;//部门代码

    private List<String> phone;//电话 多个电话拼接

    private String url;//部门网址

    private String type;

    private String attribute;//机构性质

    public DepartmentVo(Department department) {
        this.id = department.getId();
        this.name = department.getName();
        this.code = department.getCode();
        String  departmentPhone= department.getPhone();//部门电话
        if(StringUtils.isNotBlank(departmentPhone)){

            String[] split = departmentPhone.split(",");
            this.phone= Arrays.asList(split);
        }else {
            this.phone=null;
        }
        this.url = department.getUrl();
        this.type = null==department.getType()?null:department.getType().getText();
        this.attribute=department.getAttribute();
    }
}
