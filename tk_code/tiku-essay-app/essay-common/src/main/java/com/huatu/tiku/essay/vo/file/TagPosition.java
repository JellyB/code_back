package com.huatu.tiku.essay.vo.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangqingpeng
 * @title: TagPosition
 * @description: CorrectLabelUtil用
 * @date 2019-07-1812:00
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TagPosition {
    private int start;
    private int end;
    private String tagName;
    private String description;
    private double score;
    private int contentStart;       //文字区间
    private int contentEnd;
    private String contentRegion;
    private String endTagRegion;        //结束标签的区间
    private boolean underLine;      //下划线展示
    private boolean highLight;      //是否高亮展示

    private String content;

    private long detailId;  //详细批注
}