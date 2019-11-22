package com.huatu.tiku.essay.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

/**
 * Created by x6 on 2017/11/26.
 * 拍照识别信息存储
 */
@Builder
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name="v_essay_photo_answer")
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
public class EssayPhotoAnswer extends BaseEntity implements Serializable {

    /**
     * 图片地址
     */
    private String url;

    /**
     * 用户id
     */
    private int userId;

    /**
     * 终端
     */
    private int terminal;

    /**
     * 识别结果
     */
    private String content;

}
