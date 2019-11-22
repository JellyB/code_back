package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/26
 * @描述
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeySentenceExportWithDescVo {

    //关键句描述
    private String phrase;

    //关键句
    private List<KeySentenceExportVo> keySentenceExportVoList;


}
