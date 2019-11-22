package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2018/4/9.
 * 数据统计SQL返回vo
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EssayLabelTimesVO {

    /**
     * 答题卡id
     */
    private long answerId;

    /**
     * 批注次数
     */
    private int count;
    /**
     * 批注人
     */
    private List<String> adminList;


}
