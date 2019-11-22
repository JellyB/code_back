package com.huatu.tiku.essay.repository.snapshot;

import com.huatu.tiku.essay.entity.correct.QuestionLabelContentSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author huangqingpeng
 * @title: QuestionLabelContentSnapshotRepository
 * @description: 批注全量内容快照
 * @date 2019-07-1217:01
 */
public interface QuestionLabelContentSnapshotRepository extends JpaRepository<QuestionLabelContentSnapshot, Long>, JpaSpecificationExecutor<QuestionLabelContentSnapshot> {
}
