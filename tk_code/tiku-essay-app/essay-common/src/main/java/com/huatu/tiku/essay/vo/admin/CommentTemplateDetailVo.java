package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentTemplateDetailVo {

    private List<CommentTemplateNodeVo> comments;

    private String content;

    private long id;

    private int sort;

    //1论点 2论据3其他
    private int bizType;
}
