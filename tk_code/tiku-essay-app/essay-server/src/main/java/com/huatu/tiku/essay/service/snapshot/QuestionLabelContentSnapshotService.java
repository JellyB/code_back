package com.huatu.tiku.essay.service.snapshot;

import com.huatu.tiku.essay.entity.EssayLabelDetail;

/**
 * @author huangqingpeng
 * @title: QuestionLabelContentSnapshotService
 * @description: TODO
 * @date 2019-07-1315:02
 */
public interface QuestionLabelContentSnapshotService {
    void saveSnapshot(EssayLabelDetail essayLabelDetail);
}
