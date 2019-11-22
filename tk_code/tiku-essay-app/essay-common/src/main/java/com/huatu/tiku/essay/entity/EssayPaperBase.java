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
 * Created by x6 on 2017/11/26.
 * base试卷对象
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_paper_base")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayPaperBase extends BaseEntity implements Serializable {

    /* Status -1 删除状态  1未审核  2审核中  3审核未通过  4 审核通过  */
    /* BizStatus 0未上线  1上线中  2已下线 */

    /* 试卷名称 */
    private String name;
    /**
     * 试题年份
     **/
    private String paperYear;
    /**
     * 试题日期
     **/
    private String paperDate;
    /**
     * 地区id
     **/
    private long areaId;
    /**
     * 地区名称
     **/
    private String areaName;
    /**
     * 下级地区id
     **/
    private long subAreaId;
    /**
     * 下级地区名称
     **/
    private String subAreaName;

    /**
     * 真题 1  模考题 0
     **/
    private int type;
    /* 答题限时 */
    private int limitTime;
    /* 总分 */
    private double score;

    //pdf地址
    private String pdfPath;

    //下载次数
    private int downloadCount;
    //文件大小
    private String pdfSize;


    /**
     * 是否有存在视频解析
     */
    private Boolean videoAnalyzeFlag;


}
