package com.huatu.tiku.essay.vo.admin;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 3:17 PM
 **/

@Data
@NoArgsConstructor
public class CommentTemplateVo {

    private int labelType;

    private String labelTypeName;

    private int sort;

    private List<CommentTemplateDetailVo> templateList;

    @Builder
    public CommentTemplateVo(int labelType, String labelTypeName, int sort, List<CommentTemplateDetailVo> templateList) {
        this.labelType = labelType;
        this.labelTypeName = labelTypeName;
        this.sort = sort;
        this.templateList = templateList;
    }
}
