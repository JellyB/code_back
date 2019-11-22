package com.huatu.ztk.question.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 映射去重表
 * 用作存储去重过程中删除的试题的ID和替代ID的对应关系
 * 如果查询被删除的试题ID，则使用对应的替代ID的试题数据作为试题内容返回（试题内容中ID改为被删除的ID）
 * Created by huangqp on 2018\6\25 0025.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "reflect_question")
public class ReflectQuestion implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 旧题库id
     */
    @Id
    private Integer oldId;
    /**
     * 新题库id
     */
    private Integer newId;

    private Integer status;
}
