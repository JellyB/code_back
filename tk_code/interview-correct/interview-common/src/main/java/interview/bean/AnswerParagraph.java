package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-08-20  17:07 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AnswerParagraph {
    private String content;//段落内容
    private List<AnswerSentence> sentences;//段落语句
    private int start;//该段落是在答案中的起始位置
}
