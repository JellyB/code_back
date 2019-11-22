package com.huatu.tiku.essay.vo.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 后台导入算法相关VO
 * @date 2018/12/275:23 PM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyWithDescVO {
    //描述
    private String desc;
    //关键词or关键句
    private List<String> keyParaList;
}
