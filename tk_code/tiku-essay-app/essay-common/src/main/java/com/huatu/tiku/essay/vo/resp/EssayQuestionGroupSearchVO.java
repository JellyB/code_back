package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author zhaoxi
 * @Description: 单题组查询VO
 * @date 2018/9/18下午8:53
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayQuestionGroupSearchVO {
    private Long id;

    //id(题组id)
    private Long groupId;
    //展示内容
    private String showMsg;
    //题组类型
    private Integer type;
    //题目列表
    private List<EssayQuestionSearchVO> questionList;

}
