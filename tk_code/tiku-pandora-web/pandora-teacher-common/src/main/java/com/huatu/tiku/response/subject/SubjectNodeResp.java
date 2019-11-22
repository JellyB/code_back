package com.huatu.tiku.response.subject;

import com.huatu.tiku.response.BaseResp;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by huangqp on 2018\5\11 0011.
 */
@Data
@Builder
public class SubjectNodeResp extends BaseResp{
    /**
     * 考试类型ID
     */
    private List<Long> categoryIds;
    /**
     * 学科ID
     */
    private Long subject;
    /**
     * 学段ID
     */
    private List<Long> grades;
    /**
     * 学段名称
     */
    private List<String> gradeList;
}
