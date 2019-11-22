package com.huatu.ztk.backend.question.bean;

import com.huatu.ztk.commons.Area;
import com.huatu.ztk.user.bean.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ht on 2017/2/22.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AdviceBean {
    private int id;//id
    private int qid;//要纠错的试题id
    private String stem; //试题题干
    private int qArea;//试题地区
    private String areaName;  //地区名称
    private int mode;// 试题类型(真题、模拟题)
    private long uid;//纠错人
    private String contacts;//联系方式
    private String content;//纠错内容
    private int errorType; //错误类型
    private int catgory; //科目
    private int qType; //试题类型
    private int subject; //考试科目
    private int moduleId; //模块
    private String uname; //纠错人名称
    private long createTime; //创建时间
    private int status; //试题纠错状态 1：已采纳，2：未采纳，3：未处理,4:不使用
    private String acceptContent; //回复内容
    private long acceptTime; //回复时间
    private long isMine; //是否为当前用户
    private int orderTime;//时间排序类型
    private String handler; //处理人

    private int type; //题库题型


}
