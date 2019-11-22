package com.huatu.tiku.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhouwei
 * @Description: 点击型菜单事件
 * @create 2018-01-03 上午11:05
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClickButton {
    private String type;
    private String name;
    private String key;
}
