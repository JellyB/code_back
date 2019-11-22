package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-13  15:46 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class InterviewKeyWord {
    private String keyWord;//词语本身
    private double score;//分数
    private List<String> splitWords;//拆分词
}
