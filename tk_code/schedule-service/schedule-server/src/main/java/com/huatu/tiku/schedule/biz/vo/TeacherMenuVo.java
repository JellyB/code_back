package com.huatu.tiku.schedule.biz.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**教师菜单
 * @author wangjian
 **/
@Data
public class TeacherMenuVo implements Serializable{
    private static final long serialVersionUID = 759330263498292800L;

    //标题
    private String title;
    //菜单集合
    private List list;
}
