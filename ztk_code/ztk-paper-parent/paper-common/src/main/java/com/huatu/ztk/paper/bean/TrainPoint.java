package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 推荐用户训练的知识点
 * Created by shaojieyue
 * Created time 2016-05-20 21:59
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class TrainPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private int questionPointId;//知识点id
    private int status;
    private long practiceId;//练习id
    private String name;
}
