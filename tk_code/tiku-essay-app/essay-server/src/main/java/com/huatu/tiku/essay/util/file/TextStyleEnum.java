package com.huatu.tiku.essay.util.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * @author huangqingpeng
 * @title: TextStyleEnum
 * @description: TODO
 * @date 2019-08-2711:34
 */
@Getter
@AllArgsConstructor
public enum TextStyleEnum{
    STRONG(1,"加粗", Pattern.compile("<strong>(((?!</strong>).)*)</strong>")),
    UNDERLINE(2,"下划线",Pattern.compile("<u>(((?!</u>).)*)</u>"));
    private int key;
    private String value;
    private Pattern pattern;
}