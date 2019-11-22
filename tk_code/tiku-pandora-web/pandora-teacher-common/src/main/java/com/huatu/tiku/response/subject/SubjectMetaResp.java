package com.huatu.tiku.response.subject;

import com.huatu.tiku.response.BaseResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2018\7\12 0012.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectMetaResp extends BaseResp{
    /**
     * 科目id
     */
    private Long id;
    /**
     * 科目名称
     */
    private String name;
    /**
     * 下级科目id
     */
    private List<SubjectMetaResp> children;
    /**
     * 实体试卷量
     */
    private Integer paperEntityTotal;
    /**
     * 活动试卷量
     */
    private Integer paperActivityTotal;
    /**
     * 试卷总量
     */
    private Integer paperTotal;
    /**
     * 试题总量
     */
    private Integer questionTotal;
    /**
     * 层级
     */
    private Integer level;
}

