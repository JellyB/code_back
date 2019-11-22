package com.huatu.tiku.essay.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by x6 on 2017/11/24.
 * base试题表
 */
@Data
@Entity
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate(true)
@Table(name = "v_essay_question_base")
@EqualsAndHashCode(callSuper = false)
public class EssayQuestionBase   extends BaseEntity implements Serializable{
    //status 1表示正常
    @Id
    @GeneratedValue
    private long id;

    // 地区id */
    private long areaId;
    /* 地区名称 */
    private String areaName;
    /** 下级地区id **/
    private long subAreaId;
    /** 下级地区名称 **/
    private String subAreaName;




    //试题详情id
    private long detailId;

    //题序
    private int sort;

    /*  试题所属年份  */
    private String questionYear;

    /*  试题所属日期  */
    private String questionDate;

    /*  试题所属试卷  */
    private long paperId;

    /*  做题时长  */
    private int limitTime;
    //pdf路径
    private String pdfPath;


    //下载次数
    private int downloadCount;

    //文件大小
    private String pdfSize;

    private int areaSort;



    //是否是缺失题（0  不缺失   1 缺失）
    private int isLack;


    //视频id
    private Integer videoId;

}
