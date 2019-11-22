package com.huatu.ztk.knowledge.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-12  17:09 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionGeneticBean implements Serializable{
    private static final long serialVersionUID = 1L;

    private int id;
    private int year;
    private int moduleId;
    private int difficulty;
}
