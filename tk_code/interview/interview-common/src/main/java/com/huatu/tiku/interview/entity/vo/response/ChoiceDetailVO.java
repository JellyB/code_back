package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/4/11.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChoiceDetailVO {
    //选项id
    private long id;
    //试题id
    private long questionId;
    //序号
    private int sort;
    //选项内容
    private String content;
    //选中标识
    private boolean flag = false;
}
