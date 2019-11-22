package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-08-20  17:45 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AnswerPhrase {
    private String content;
    private double score;//分数
    private List<String> words;//语句所有词语
    private int status;//状态
    private int start;//该短句在答案中的起始位置
}
