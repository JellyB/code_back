package com.huatu.ztk.user.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 用户关于考试的基础配置
 * Created by shaojieyue
 * Created time 2016-12-20 10:29
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "ztk_user_config")
public class UserConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;//用户id+catgoryId
    private long uid;//用户id
    /**
     * @see {@link com.huatu.ztk.commons.SubjectType}
     */
    private int subject;//知识点所属类目
    private int area;//考试区域,可以精确到市
    private int category;//考试类型
    private int qcount;//抽题的数量
    private int errorQcount;//错题练习的数量
}
