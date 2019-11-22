package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * Created by linkang on 17-7-14.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "ztk_match_user_meta")
public class MatchUserMeta implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id; //用户id_试卷id
    private long userId;  //用户id
    private int positionId; //职位id
    private String positionName; //职位名称
    private int paperId; //试卷id
    @Transient
    private int positionCount; //职位报名人数
    private long practiceId; //练习id
    /**
     * 学校名称
     */
    private String schoolName;
    /**
     * 学校id
     */
    private Long schoolId;
}
