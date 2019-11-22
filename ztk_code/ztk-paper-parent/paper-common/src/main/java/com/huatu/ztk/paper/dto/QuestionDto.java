package com.huatu.ztk.paper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author shanjigang
 * @date 2019/3/5 21:01
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionDto {
    /**
     * 是否正确
     */
    private int[] corrects;

    /**
     * 试题Ids
     */
    private int[] questionIds;

    /**
     * 试题坐标
     */
    private int[] questionIndexs;
}
