package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-13  16:02 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class InterviewScoreDescription {
//    @Id
    private long scoreDescId;//描述id
    private String scoreDescContent;//描述内容
    private double score;//分数
    private List<InterviewScorePoint> scorePoints;//得分要点
}
