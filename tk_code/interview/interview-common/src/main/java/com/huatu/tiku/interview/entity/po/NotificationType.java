package com.huatu.tiku.interview.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
//@DynamicInsert
@DynamicUpdate(true)
@Entity
@Table(name = "t_notification_type")
public class NotificationType extends BaseEntity implements Serializable {

    /**
     * 通知类型  1线上课程安排  2晨读鸡汤  3 报道通知
     */
    private Integer type;
    /**
     * 图片url
     */
    private String imageUrl;
    /**
     * 微信认证图片id
     */
    private String wxImageId;
    /**
     * 推送时间
     */
    private Date pushTime;
    /**
     * 标题
     */
    private String title;

    /**
     * 推送内容
     */
    private String content;


    //关联班级id（全部班级：0   多选班级：id1,id2,id3）
    private Long classId ;


}
