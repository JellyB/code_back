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
@AllArgsConstructor
@NoArgsConstructor
public class KeySentenceExportVo {

    //关键词(关键词的近义词)
    private List<String> keyWordList;

    //关键句的近似句
    private List<KeySentenceExportVo> similarKeySentenceList;


}
