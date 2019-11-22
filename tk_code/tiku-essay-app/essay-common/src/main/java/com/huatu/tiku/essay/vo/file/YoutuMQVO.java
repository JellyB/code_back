package com.huatu.tiku.essay.vo.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhaoxi
 * @Description: 拍照识别MQ传输对象
 * @date 2018/11/1下午1:33
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YoutuMQVO {
    //图片识别的文字内容
    private String content;


    private String url;
    //用户id
    private int userId;

    //terminal
    private int terminal;
    //文件后缀
    private String suffix;
}
