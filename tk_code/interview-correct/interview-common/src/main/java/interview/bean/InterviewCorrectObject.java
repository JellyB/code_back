package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-08-20  10:34 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class InterviewCorrectObject {
    private String answerContent;//要点本身
    private Long questionRecordId;//答题卡id
    private int questionRecordType;//答题卡类型
    private List<InterviewScoreDescription> scoreDescs;//分数
}
