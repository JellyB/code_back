package com.huatu.bigdataanalyzecommon.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayRecordDtoWrapper {


    /**
     * 播放记录
     */
    private List<PlayRecordDto> playRecordDtos;

    /**
     * 终端类型
     */
    private Integer terminal;

    /**
     * 客户端版本
     */
    private String cv;

    /**
     * 客户端型号
     */
    private String pm;

    /**
     * 用户ID
     */
    private Long userId;


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        if (playRecordDtos != null && playRecordDtos.size() > 0) {

            for (int i = 0; i < playRecordDtos.size(); i++) {

                sb.append(playRecordDtos.get(i).toString()).append(terminal).append(",")
                        .append(cv).append(",")
                        .append(pm).append(",")
                        .append(userId);

                if (i < playRecordDtos.size()-1){
                    sb.append("\r\n");
                }

            }
        }


        return sb.toString();
    }
}
