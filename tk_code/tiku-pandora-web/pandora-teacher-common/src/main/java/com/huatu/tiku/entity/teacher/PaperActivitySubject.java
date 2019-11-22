package com.huatu.tiku.entity.teacher;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "paper_activity_subject")
public class PaperActivitySubject extends BaseEntity{
    /**
     * 活动卷id
     */
    private Long paperId;
    /**
     * 科目id
     */
    private Long subjectId;
}

