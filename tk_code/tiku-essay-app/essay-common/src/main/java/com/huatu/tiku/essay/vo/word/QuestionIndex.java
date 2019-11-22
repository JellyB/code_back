package com.huatu.tiku.essay.vo.word;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ZhenYang
 * @Date Created in 2018/3/6 16:07
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionIndex {
    private String content;
    private Integer index;
    private Integer sort;
}
