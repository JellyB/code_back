package com.huatu.tiku.essay.util.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author huangqingpeng
 * @title: TextStyleElement
 * @description: TODO
 * @date 2019-08-2615:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextStyleElement {

    private Set<TextStyleEnum> textStyleEnums;

    private int headStartIndex;

    private int headEndIndex;

    private int tailStartIndex;

    private int tailEndIndex;

}
