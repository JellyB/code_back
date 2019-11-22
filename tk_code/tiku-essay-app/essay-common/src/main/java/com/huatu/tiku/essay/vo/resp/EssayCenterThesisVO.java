package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/3/13.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EssayCenterThesisVO {


    private long id; //用户Id
    private String content; //中心论点内容
    private int userId; //用户Id
    private long answerId; //答题卡Id
    private long questionBaseId;//试题id
    private long questionDetailId;//试题detailId
    private String questionName;//试题名称
    private String areaName;//地区名称
    private int areaId;//地区名称
    private String year;//年份
    private int bizStatus;
    private int status;


}
