package com.huatu.tiku.essay.entity.correct;

import com.huatu.tiku.essay.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author huangqingpeng
 * @title: QuestionLabelContentSnapshot
 * @description: 批注全量内容快照表
 * @date 2019-07-1216:49
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "v_essay_label_content_snapshot")
public class QuestionLabelContentSnapshot extends BaseEntity {

    /**
     * 总批注ID
     */
    private long totalId;

    /**
     * 详细批注ID（因为修改某个详细批注而生成的快照信息）
     */
    private long detailId;

    /**
     * 全量详细批注快照内容
     */
    private String labelContent;

    /**
     * 答题卡ID
     */
    private long questionAnswerId;


}
