package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-13  15:48 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class InterviewScorePoint {
//    @Id
    private long scorePointId;//id
    private String scorePointContent;//要点本身
    private double score;//分数
    private List<InterviewKeyWord> keyWords;//关键词
}
