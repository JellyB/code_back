package com.huatu.ztk.backend.system.bean;

import com.huatu.ztk.backend.subject.bean.SubjectBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-04-25  10:40 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class OperateAction {
    private OperateMessage operate;//操作信息
    private List<Action> actionList;
}
