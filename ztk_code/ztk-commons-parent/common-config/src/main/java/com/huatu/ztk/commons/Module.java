package com.huatu.ztk.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * Created by shaojieyue
 * Created time 2016-07-05 17:03
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class Module implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;//模块名称
    private String name;//所属模块的名字
}
