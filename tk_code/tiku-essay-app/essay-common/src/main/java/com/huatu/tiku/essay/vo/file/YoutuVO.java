package com.huatu.tiku.essay.vo.file;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/3/27.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class YoutuVO{

    //文件base64编码
    private String file;

    //类型（0  手写   1打印）
    private int type;

    //图片识别的文字内容
    private String content;


}
