package com.huatu.tiku.essay.entity;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by x6 on 2018/1/30.
 * 学员申论题目收藏关系表（试题，试卷）
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_user_paper_collection")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayUserPaperCollection extends BaseEntity implements Serializable {

    //用户id
    private int userId;

    //题目id
    private long paperBaseId;







}
