package com.huatu.tiku.response.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by huangqp on 2018\5\15 0015.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeWithSubjectResp {
    /**
     * 考试类型
     */
    private Long category;
    /**
     * 学科
     */
    private Long subject;
    /**
     * 学段(教综学段可以为空)
     */
    private Long grades;
    /**
     * 知识点id
     */
    private List<Long> knowledgeIds;
}
