package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/4/26
 * @描述 存储活动赠送用户金币信息
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "ztk_estimate_user")
public class SendCoinUsers implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    private Long userId;//用户ID
    private String userName;//用户名
    private Long paperId;//试卷ID
    private int courseId; //课程ID

}
