package com.huatu.tiku.essay.service.impl.snapshot;

import com.huatu.tiku.essay.entity.EssayLabelDetail;
import com.huatu.tiku.essay.entity.correct.QuestionLabelContentSnapshot;
import com.huatu.tiku.essay.repository.snapshot.QuestionLabelContentSnapshotRepository;
import com.huatu.tiku.essay.service.snapshot.QuestionLabelContentSnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author huangqingpeng
 * @title: QuestionLabelContentSnapshotServiceImpl
 * @description: TODO
 * @date 2019-07-1315:03
 */
@Service
public class QuestionLabelContentSnapshotServiceImpl implements QuestionLabelContentSnapshotService {

    @Autowired
    private QuestionLabelContentSnapshotRepository questionLabelContentSnapshotRepository;

    @Override
    @Async
    public void saveSnapshot(EssayLabelDetail essayLabelDetail) {
        QuestionLabelContentSnapshot build = QuestionLabelContentSnapshot.builder().detailId(essayLabelDetail.getId())
                .labelContent(essayLabelDetail.getLabeledContent())
                .questionAnswerId(null == essayLabelDetail.getAnswerId() ? 0 : essayLabelDetail.getAnswerId())
                .totalId(null == essayLabelDetail.getTotalId() ? 0 : essayLabelDetail.getTotalId())
                .build();
        questionLabelContentSnapshotRepository.save(build);

    }
}
