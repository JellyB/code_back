package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.IntelligenceConvertManualRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/8/8
 * @描述 智能转人工记录表
 */
public interface IntelligenceConvertManualRecordRepository extends JpaRepository<IntelligenceConvertManualRecord, Long>, JpaSpecificationExecutor<IntelligenceConvertManualRecord> {

    List<IntelligenceConvertManualRecord> findByIntelligenceAnswerIdAndAnswerTypeAndStatus(long answerId, int answerType, int status);

    @Query("SELECT COUNT(id) FROM IntelligenceConvertManualRecord re where  re.intelligenceAnswerId=?1 and re.answerType=?2 and re.status=?3")
    Long countByIntelligenceAnswerIdAndAnswerTypeAndStatus(long answerId, int answerType, int status);


}
