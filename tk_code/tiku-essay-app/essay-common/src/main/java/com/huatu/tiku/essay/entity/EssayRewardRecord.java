package com.huatu.tiku.essay.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * 赠送批改操作记录
 */
@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_reward_record")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayRewardRecord extends BaseEntity  implements Serializable {

    //备注信息
    private String remark;
    //单题批改次数
    private int queNum;
    //套题批改次数
    private int mulNum;
    //议论文批改次数
    private int argNum;
    //账号信息列表
    private String errorList;
    //原始数据(excel地址 或者 录入信息)
    private String source;



}
