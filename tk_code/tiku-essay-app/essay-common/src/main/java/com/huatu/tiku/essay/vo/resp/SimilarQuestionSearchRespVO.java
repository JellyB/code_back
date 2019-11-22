package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * @author zhaoxi
 * @Description: 搜索结果响应体
 * @date 2018/12/145:24 PM
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimilarQuestionSearchRespVO {

    private long groupId;
    private int type;
    private String showMsg;
    private List<Object> questionList;
    private List<Object> materialList;


}
