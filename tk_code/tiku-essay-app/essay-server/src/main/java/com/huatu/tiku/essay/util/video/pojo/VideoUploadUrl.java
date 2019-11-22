package com.huatu.tiku.essay.util.video.pojo;

import lombok.Data;

/**
 * Created by duanxiangchao on 2019/4/11
 */
@Data
public class VideoUploadUrl {

    private Integer video_id;

    private String upload_url;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"video_id\":")
                .append(video_id);
        sb.append(",\"upload_url\":\"")
                .append(upload_url).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
