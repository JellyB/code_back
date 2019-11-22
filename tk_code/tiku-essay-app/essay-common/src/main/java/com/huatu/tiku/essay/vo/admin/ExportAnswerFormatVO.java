package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/30
 * @描述
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportAnswerFormatVO {

    private String titleScore;
    private String keyWord;
}
