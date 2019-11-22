package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/24 11:04
 * @Description 模式串匹配结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mate {
    private Integer origin;
    private Integer destination;
    private String content;
    private Integer sort;

    public Mate(Integer origin, Integer destination, String content) {
        this.origin = origin;
        this.destination = destination;
        this.content = content;
    }
}
