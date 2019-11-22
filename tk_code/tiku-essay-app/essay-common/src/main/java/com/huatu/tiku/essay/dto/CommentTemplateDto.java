package com.huatu.tiku.essay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 4:13 PM
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentTemplateDto {

    private List<CommentTemplateNodeDto> comments;

    private String content;

    private long id;

    private int labelType;

    private int sort;

    private int type;

    private int bizType;
}
