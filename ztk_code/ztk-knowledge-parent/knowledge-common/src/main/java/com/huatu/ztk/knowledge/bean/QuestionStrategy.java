package com.huatu.ztk.knowledge.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 组卷策略
 * Created by shaojieyue
 * Created time 2016-05-19 16:37
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionStrategy  implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Integer> questions;//试卷
    private List<Module> modules;//模块
    private double difficulty;//难度系数
}
