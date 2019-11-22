package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-05-20 16:34
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "user_day_train_settings")
public class DayTrainSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private long id;
    private long userId;
    private int subject;  //科目id
    private int number; //每日特训次数
    private int questionCount; //特训试题个数
    private List<Integer> selects; //选择的特训点
    @Transient
    private List<KnowledgeModule> points; //特训点列表
    @CreatedDate
    private Date createTime;
}
