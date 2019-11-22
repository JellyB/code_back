package com.huatu.tiku.response.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/5/9.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResp {
    private String url;

    private String content;
}
