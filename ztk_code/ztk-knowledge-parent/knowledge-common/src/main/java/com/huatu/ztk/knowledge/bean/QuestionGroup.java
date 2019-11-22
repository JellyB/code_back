package com.huatu.ztk.knowledge.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-12  17:20 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private int year;
    private int moduleId;
    private int qNum;
    List<QuestionGeneticBean> questions;
}
