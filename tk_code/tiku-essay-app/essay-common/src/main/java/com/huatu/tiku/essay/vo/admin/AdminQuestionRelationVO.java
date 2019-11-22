package com.huatu.tiku.essay.vo.admin;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 试题关联的试卷信息和材料信息
 * Created by huangqp on 2017\12\25 0025.
 */
@Builder
@AllArgsConstructor
@Data
public class AdminQuestionRelationVO {
    private long questionBaseId;
    private long paperId;
    private String paperName;
    private List<EssayMaterialVO> materials;
    public AdminQuestionRelationVO(){
        paperName = "";
        materials = Lists.newLinkedList();
    }
}
