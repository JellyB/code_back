package com.huatu.tiku.essay.vo.resp;

/**
 * Created by x6 on 2018/3/9.
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/5.
 * 用户意见反馈回复VO
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReplyVO {

    //学员反馈内容
    private String suggestContent;
    //学员id
    private long userId;
    //回复内容
    private String content;
    //回复对应的意见反馈id
    private int id;
    //回复的标题
    private String title;

}
