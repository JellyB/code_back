package interview.dao.result;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Author: xuhuiqiang
 * Time: 2018-10-16  11:57 .
 */
@Repository
public interface ResultDao {
    void insertResult(@Param("score_point") String scorePoint,
                      @Param("score_content")String scoreContent,
                      @Param("question_answer_id")long answerCardId,
                      @Param("exam_score")double examScore,
                      @Param("actual_score")double actualScore,
                      @Param("similarity")int similarity,
                      @Param("question_record_type")int questionRecordType);
}
