package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2017/12/11.
 * 试题材料 对象
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionMaterialVO {
    //材料列表
    private List<EssayMaterialVO> materialVOList;

    //顶部展示信息
    private String title;

    //试题id
    private long questionDetailId;
    private long questionBaseId;

}
