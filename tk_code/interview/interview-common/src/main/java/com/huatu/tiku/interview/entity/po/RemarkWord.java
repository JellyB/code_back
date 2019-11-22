package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/11.
 * 评价文字 词库
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "t_remark_word")
public class RemarkWord extends BaseEntity{

    //内容
    private String content;

    //序号
    private int sort;
}
