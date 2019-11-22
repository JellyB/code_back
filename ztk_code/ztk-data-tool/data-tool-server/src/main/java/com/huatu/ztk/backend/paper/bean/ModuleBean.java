package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


/**
 * Created by linkang on 2/16/17.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ModuleBean {
    private String name;
    private int id;
    private Map<Integer,Integer> questions;  //题序，题目id map
}
