package com.huatu.hadoop.bean;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class ZtkAnswerCardCTO {


    private Integer userId;
    private int[] corrects;
    private int[] questions;
    private int[] times;
    private Long createTime;
    private Integer subject;

    @Override
    public String toString() {

        return JSON.toJSONString(this);
    }

    public static void main(String[] args) {

        System.out.println(233122560 % 32);
    }

    public String getCorrects2Str() {
        StringBuffer sb = new StringBuffer();

        for (int i : corrects) {
            sb.append(i).append(",");
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public String getQuestions2Str() {
        StringBuffer sb = new StringBuffer();

        for (int i : questions) {
            sb.append(i).append(",");
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public String getTimes2Str() {
        StringBuffer sb = new StringBuffer();

        for (int i : times) {
            sb.append(i).append(",");
        }

        return sb.deleteCharAt(sb.length() - 1).toString();
    }
}
