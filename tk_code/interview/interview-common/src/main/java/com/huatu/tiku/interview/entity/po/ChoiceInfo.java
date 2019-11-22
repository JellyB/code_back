package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/11.
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "t_choice_info")
public class ChoiceInfo extends BaseEntity{
    //试题id
    private long questionId;
    //序号
    private int sort;
    //选项内容
    private String content;
}
