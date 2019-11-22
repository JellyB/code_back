package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/11.
 * 点评数据
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "t_practice_remark")
public class PracticeRemark extends BaseEntity{
    //所属模块id（）
    private long practiceContentId;
    //内容
    private String content;
    //类型(1优点 2问题)
    private int type;

}
