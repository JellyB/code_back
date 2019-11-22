package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.PaperPushLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 课堂互动推送日志
 */
public interface PaperPushLogRepository  extends JpaRepository<PaperPushLog, Long> {

    //根据试卷id和推送人id查询
    List<PaperPushLog> findByPaperIdAndCreatorOrderByIdDesc(Long paperId, String creator);

    //根据试卷id和班级id查询
    List<PaperPushLog> findByPaperIdAndClassId(Long paperId, Long classId);
}