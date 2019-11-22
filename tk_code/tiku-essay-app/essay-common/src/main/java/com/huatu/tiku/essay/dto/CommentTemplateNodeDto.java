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
 * Create time 2019-06-28 4:15 PM
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentTemplateNodeDto {

    private List<CommentTemplateNodeDto> children;

    private String content;

    private int sort;

    /**
     * 是否为删除， 1 是 0 为修改
     */
    private int isDelete;

    private long id;
}
