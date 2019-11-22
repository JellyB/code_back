package com.huatu.tiku.entity.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * vhuatu库中存储的知识点表的备份数据表
 * Created by huangqingpeng on 2018/8/24.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "v_knowledge_point")
public class OldKnowledge {
    /**
     * ID
     */
    private Integer pukey;
    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String descrp;
    /**
     * 难度系数
     */
    private Double difficultGrade;
    /**
     * 高频系数
     */
    private Integer appearGrade;
    /**
     * 重点系数
     */
    private Integer importantGrade;
    /**
     * 易错系数
     */
    private Integer mistakeGrade;
    /**
     * 区分度
     */
    private Double distinctionGrade;
    /**
     * 猜测值
     */
    private Double guessGrade;
    /**
     * 上一级知识点
     */
    private Integer prevKp;
    /**
     * 节点所在层级
     */
    private Integer nodeRank;
    /**
     * 是否可以参与抽题
     */
    private Integer isLeafnode;
    /**
     * 是否可以参与抽题
     */
    private Integer isExtQuestion;
    /**
     * 现有题量
     */
    private Integer questionNum;
    /**
     * 所属科目
     */
    private Integer blSub;
    @Column(name = "FB1Z1")
    private String FB1Z1;
    @Column(name = "FB1Z2")
    private String FB1Z2;
    @Column(name = "FB1Z3")
    private String FB1Z3;
    @Column(name = "FB1Z4")
    private String FB1Z4;
    @Column(name = "FB1Z5")
    private String FB1Z5;
    @Column(name = "EB1B1")
    private String EB1B1;
    @Column(name = "EB102")
    private String EB102;
    @Column(name = "EB103")
    private String EB103;
    @Column(name = "EB104")
    private String EB104;
    @Column(name = "EB105")
    private String EB105;
    @Column(name = "BB1B1")
    private String BB1B1;
    /**
     * 状态
     */
    @Column(name = "BB102")
    private String BB102;
    @Column(name = "BB103")
    private String BB103;
    @Column(name = "BB104")
    private String BB104;
    @Column(name = "BB105")
    private String BB105;
    @Column(name = "BB106")
    private String BB106;
    @Column(name = "BB107")
    private String BB107;
    @Column(name = "BB108")
    private String BB108;

}
