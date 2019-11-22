package interview.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-05  11:20 .
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class HownetWordRelation {
    private long hownetWordId;
    private int type;//1为具体词，2为基本义原，3为关系义原--义原对，4为关系义原--具体词对，5为符号--义原对，6为符号--具体词对
    private long pairId;//关系对id
    private String pairStr;//关系对字符串
}
