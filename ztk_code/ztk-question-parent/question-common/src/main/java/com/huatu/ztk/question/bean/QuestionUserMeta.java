package com.huatu.ztk.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionUserMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;//id
    //用户id
    private long userId;
    //试题ID
    private int questionId;
    //一级知识点ID
    private int firstPointId;
    //二级知识点ID
    private int secondPointId;
    //三级知识点
    private int thirdPointId;
    /**
     * 知识点相关信息
     */
    private List<Integer> pointsList;
    private List<String> pointsName;
    /**
     * 状态 试题是否有效 0无效1有效
     */
    private int status;
    /**
     * 是否是抽题池试题 0 不是1是
     */
    private int poolFlag;
    /**
     * 用户做题次数
     */
    private int total;
    /**
     * 用户错题次数
     */
    private int errorCount;
    /**
     * 用户最后一次做题对错表示（0对1错）
     */
    private int errorFlag;
}
