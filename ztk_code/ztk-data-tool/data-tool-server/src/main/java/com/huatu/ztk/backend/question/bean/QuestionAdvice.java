package com.huatu.ztk.backend.question.bean;

import com.huatu.ztk.commons.Area;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.user.bean.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ht on 2017/1/4.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionAdvice {

    private int id;//id
    private int qid;//要纠错的试题id
    private int qArea;//试题地区
    private long uid;//纠错人
    private String content;//纠错内容
    private long createTime; //创建时间
    private int status; //试题纠错状态 1：已采纳，2：未采纳，3：未处理,4:不使用
    private QuestionDetail questionDetail;
}
