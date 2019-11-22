package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2018/1/25.
 * 学员-班级关系
 */
@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="t_user_class")
public class UserClassRelation   extends BaseEntity  implements Serializable {

    /*
       status   1绑定中   -1 解除绑定关系
     */
//    //用户id（PhpUserId）
//    private long userId;
//
//    //用户id（PhpUserId）
//    private long phpUserId;

    //用户openId
    private String openId;


    //班级id
    private long classId;

    //绑定时间（开始时间）
    private String startTime;
    //绑定时间（结束时间）
    private String endTime;


    //绑定类型(0  短期绑定   1长期绑定)
    private int  boundType;
    
    



}
