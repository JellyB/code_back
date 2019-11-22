package com.huatu.tiku.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.util.List;

/**
 * @author 周威
 * @date 2018/9/13 1:25 PM
 **/
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AdviceBean extends BaseEntity {
    private int qid;//要纠错的试题id
    private String stem; //试题题干
    private int qArea;//试题地区
    private String areaName;  //地区名称
    private int mode;// 试题类型(真题、模拟题)
    private long uid;//纠错人`
    private String contacts;//联系方式
    private String content;//纠错内容
    private int errorType; //错误类型
    private int catgory; //科目
    private long questionId;
    /**
     * question type
     */
    private int questionType;
    private int subject; //考试科目
    private int moduleId; //模块
    /**
     * area id
     */
    private int areaId;
    /**
     * committer
     */
    private String committer;
    /**
     * start time
     */
    private String startTime;
    /**
     * end time
     */
    private String endTime;
    /**
     * order by
     */
    private String orderby;

    /**
     * 检查业务状态
     * 1 已采纳 2未采纳 3未处理
     */
    private Integer checker;
    /**
     * question area
     */
    private String questionArea;
    private String uname; //纠错人名称
    private long createTime; //创建时间
    /**
     * 试题纠错状态 1：已采纳，2：未采纳，3：未处理,4:不使用
     */
    private Integer bizStatus;
    private String acceptContent; //回复内容
    private long acceptTime; //回复时间
    private long isMine; //是否为当前用户
    private int orderTime;//时间排序类型
    private String handler; //处理人
    private String knowledgeId;//知识点id
    private int type; //题库题型


}