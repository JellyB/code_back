package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminOptionVO {

    /**
     * 显示文字
     */
    private String text;

    /**
     * 值
     */
    private Object value;
}
