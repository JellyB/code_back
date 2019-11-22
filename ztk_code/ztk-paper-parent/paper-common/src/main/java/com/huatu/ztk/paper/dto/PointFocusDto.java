package com.huatu.ztk.paper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 重点关注知识点
 * @author shanjigang
 * @date 2019/3/5 14:54
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PointFocusDto {
    /**
     * 试题下标
     */
    private List<QuestionDto> questions;

    /**
     * 关注类型值
     */
    private String typeText;

    /**
     * 关注类型
     */
    private String typeValue;

}
