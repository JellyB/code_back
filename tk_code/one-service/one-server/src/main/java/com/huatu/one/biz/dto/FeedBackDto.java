package com.huatu.one.biz.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

@Data
public class FeedBackDto {
    /**
     * 反馈意见
     */
    @NotEmpty(message = "反馈意见不能为空")
    private String content;
}
