package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/11/29.
 * 交卷答案对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaperCommitAnswerVO {

    //答题卡id
    private Long answerId;
    //题目baseId
    private Long questionBaseId;
    //题目detailId
    private Long questionDetailId;
    //答案内容
    private String content;
    //答案字数
    private Integer inputWordNum;
    // 答题用时
    private Integer spendTime;
    //文件名字（拍照答题图片名称）
    private String fileName;





}
