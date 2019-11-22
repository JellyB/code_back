package com.huatu.hadoop.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayRecordDto {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy,MM,dd,HH,HHmm");

    /**
     * 视频ID
     */
    @NotEmpty(message = "视频ID不能为空")
    private Long videoId;

    /**
     * 百家云视频ID
     */
    @NotEmpty(message = "百家云视频ID不能为空")
    private Long yunVideoId;

    /**
     * 播放时间
     */
    @NotEmpty(message = "播放时间不能为空")
    @DateTimeFormat(pattern = "yyyy,MM,dd,HH,HHmm")
    private Date playTime;

    @Override
    public String toString() {
        return  videoId +
                "," + yunVideoId +
                "," + new BigDecimal(playTime.getTime()).divide(new BigDecimal(1000L),0,RoundingMode.HALF_DOWN) +
                ",";
    }
}
