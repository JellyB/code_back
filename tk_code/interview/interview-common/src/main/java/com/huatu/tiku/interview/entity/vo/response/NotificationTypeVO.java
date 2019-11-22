package com.huatu.tiku.interview.entity.vo.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by x6 on 2018/5/18.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationTypeVO {
    private long id;
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
    private Long areaId ;
}
