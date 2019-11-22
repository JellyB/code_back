package com.huatu.tiku.essay.entity.vo.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by linkang on 17-7-14.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class MatchUserMeta implements Serializable {

//    private String id; //用户id_试卷id
    private int userId;  //用户id
    private Long positionId; //地区id
    private String positionName; //地区名称
    private int paperId; //试卷id
    private int positionCount; //职位报名人数
    //答题卡id
    private long practiceId; //练习id
}
