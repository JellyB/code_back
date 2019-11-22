package com.huatu.ztk.knowledge.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 知识点总结
 * Created by shaojieyue
 * Created time 2016-05-27 09:44
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "user_point_summary")
public class PointSummary  implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private String name;
    private long uid;//用户id
    @Getter(onMethod = @__({ @JsonIgnore }))
    private int subject;
    @Getter(onMethod = @__({ @JsonIgnore }))
    private int pointId;//知识点id
    private int acount;//试题个数,不重复 类似mysql的distinct
    private int rcount;//正确个数,不重复 类似mysql的distinct
    private int wcount;//错误个数,不重复 类似mysql的distinct
    @Getter(onMethod = @__({ @JsonIgnore }))
    private long times;//答题时间和
    private long speed;//答题速度
    @Getter(onMethod = @__({ @JsonIgnore}))
    private int qsum;//所有做过的题次数(带重复的)
    private double accuracy;//正确率
}
