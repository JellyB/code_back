package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ansj.domain.Term;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2018-08-20  17:08 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class AnswerSentence {
    private String sentence;
    private String sentenceNoPunct;//无标点句子内容
    private int type;//段落中的位置，1为段首句，2为中间句，3为段尾句
    private List<Term> terms;//分词以后的词
    private List<AnswerPhrase> phrases;
    private int start;//该句子在答案中的起始位置
}
