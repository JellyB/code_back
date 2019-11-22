package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2018/1/17.
 *  提升建议
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="t_learning_advice")
public class LearningAdvice  extends BaseEntity  implements Serializable {

    //类型
    private Integer type;
    //级别
    private Integer level;
    //建议内容
    private String content;


}
