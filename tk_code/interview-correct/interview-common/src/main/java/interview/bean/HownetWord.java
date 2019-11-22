package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-05  11:19 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class HownetWord {
//    @Id
    private long id;//id
    private String item;//词语本身
    private int type;//虚词或实词
    private String pos;//词性
    private List<Long> wordId;//具体词
    private List<Long> primitives;//基本义原
    private Map<Long,List<String>> relationPrimitives;//关系义原，义原
    private Map<Long,List<String>> relationPrimitivesWord;//关系义原，具体词
    private Map<String,List<Long>> symbolPrimitives;//符号，义原
    private Map<String,List<Long>> symbolWords;//符号，具体词
}
