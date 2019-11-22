package com.huatu.tiku.essay.entity.correct;

import com.huatu.tiku.essay.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author huangqingpeng
 * 人工批改所用图片表
 * TODO 在label_detail表中添加imageId，imageAxis(单个坐标),imageAllAxis（整体坐标）三个字段
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@DynamicInsert(true)//动态插入
@DynamicUpdate(true)//动态更新
@Table(name = "v_essay_correct_image")
public class CorrectImage extends BaseEntity {

    /**
     * 源图片地址
     */
    private String imageUrl;

    /**
     * 答题卡ID
     */
    private long questionAnswerId;

    /**
     * 图片顺序
     */
    private int sort;

    /**
     * 反转角度（0正常、1.90度、2.180度、3.270度）
     */
    private int roll;

    /**
     * 批注完成后带批注截图的url
     */
    private String finalUrl;
    
    /**
     * 翻译内容
     */
    private String content;
}
