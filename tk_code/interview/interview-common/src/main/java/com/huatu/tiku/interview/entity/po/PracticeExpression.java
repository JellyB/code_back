package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2018/4/12.
 * 表现方面相关数据（语音语调、流畅程度、仪态动作）
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Table(name = "t_practice_expression")
public class PracticeExpression  extends BaseEntity{
    //类型(1语音语调、2流畅程度、3仪态动作)
    private int type;

    //内容
    private String content;


}
