package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: 添加议论文标注前的一些校验
 * @date 2018/7/9下午4:00
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayLabelCheckVO {

    /*
        是否存在标题批注
     */
    private String titleScore;
    /*
        是否存在结构批注
     */
    private String structScore;
}
