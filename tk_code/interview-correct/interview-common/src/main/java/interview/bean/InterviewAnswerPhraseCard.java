package interview.bean;

import lombok.*;

import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-11-01  16:29 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class InterviewAnswerPhraseCard {
    private String content;//内容
    private double sim;//相似度，包括关键词与语义相似之和
    private List<Integer> starts;//标记是在第几个位置开始
    private List<Integer> ends;//标记是在第几个位置结束
    private List<String> answerPhrases;//匹配的句子
    private String matchPhrase;//匹配到的句子，若有两个句子及以上，合并，中间以……分隔
    private double score;//分数
    private double actualScore;//实际得分
    private long scorePointId;
    private long descId;
    private double descFullMark;
}
