package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试题的简单信息（试题id，题序，题干，试卷id）
 * Created by huangqp on 2017\12\11 0011.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssaySimpleQuestionVO {
    //试题id
    private long questionId;
    //试题详情
    private long questionDetailId;
    //试卷id
    private long paperId;
    //题序
    private int sort;
    //题干
    private String stem;
    private int bizStatus;
    private int status;
    /**
     * 视频ID
     */
    private Integer videoId;
    /**
     * 视频播放地址
     */
    private String videoUrl;
}
