package com.huatu.tiku.interview.entity.dto;

import lombok.Data;

import java.util.Date;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/19 11:09
 * @Description
 */
@Data
public class NotificationVO {
    private Integer type;
    private Long id;
    private String creator;
    private Date gmtCreate;
    private String title;
    private Date pushTime;

    //是否编辑
    private Boolean canEdit;
}
