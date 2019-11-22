package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by x6 on 2017/12/28.
 * 申论模考用户报名信息
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name="v_essay_mock_user_meta")
@DynamicUpdate
@DynamicInsert
public class EssayMockUserMeta extends BaseEntity{

    private int userId;  //用户id
    // private int positionId; //职位id
//   private String positionName; //职位名称
    private long paperId; //试卷id
    private int positionCount; //职位报名人数
    //答题卡id
    private long practiceId; //练习id
}
