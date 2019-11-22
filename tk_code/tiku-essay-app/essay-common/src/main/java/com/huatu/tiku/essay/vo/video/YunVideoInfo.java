package com.huatu.tiku.essay.vo.video;

import lombok.Builder;
import lombok.Data;

/**
 * 百家云视频信息返回对象
 */
@Data
public class YunVideoInfo {

    /**
     * 视频名称
     */
    private String name;
    /**
     * 视频时长 秒
     */
    private Integer length;
    /**
     * 视频封面
     */
    private String prefaceUrl;
    
    /**
     * 视频转码状态 100 为转码成功 20为上传完成 30为转码失败
     */
    private Integer videoStatus;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"name\":\"")
                .append(name).append('\"');
        sb.append(",\"length\":")
                .append(length);
        sb.append(",\"prefaceUrl\":\"")
                .append(prefaceUrl).append('\"');
        sb.append('}');
        return sb.toString();
    }
}
