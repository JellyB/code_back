package com.huatu.ztk.backend.system.bean;

import com.huatu.ztk.backend.subject.bean.SubjectBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-25  19:28 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class RoleAction {
    private RoleMessage role;
    private List<Action> actionList;//角色对应能放访问的功能列表
    private List<Catgory> catgoryList;//考试类型
    private int lookup;//是否只查看该账号创建的试卷，0为是，1为不是
}
