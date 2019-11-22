package com.huatu.tiku.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhouwei
 * @Description: 视图型菜单事件
 * @create 2018-01-03 上午11:04
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewButton {
    private String type;
    private String name;
    private String url;
}
