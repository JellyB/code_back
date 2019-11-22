package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 中心论点 对象
 * Created by x6 on 2017/12/14.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_center_thesis")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayCenterThesis  extends BaseEntity  implements Serializable {
    /*
      status 1正常  -1 删除
      bizStatus 0默认  1采纳 2 不采纳
    */


    private String content; //中心论点内容
    private int userId; //用户Id
    private long answerId; //答题卡Id
    private long questionBaseId;//试题id
    private long questionDetailId;//试题detailId

    private String areaName;//地区名称
    private int areaId;//地区名称
    private String year;//年份




}
