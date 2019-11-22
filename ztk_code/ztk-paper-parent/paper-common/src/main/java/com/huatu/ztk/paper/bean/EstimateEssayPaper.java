package com.huatu.ztk.paper.bean;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * 模考估分试卷
 * Created by shaojieyue
 * Created time 2016-07-23 16:54
 */
@Data
public class EstimateEssayPaper extends EssayPaper implements Serializable {
    private static final long serialVersionUID = 1L;

    private long startTime;         //开始时间,毫秒
    private long endTime;           //结束时间，毫秒
    private long onlineTime;        //上线时间,毫秒
    private long offlineTime;       //下线时间,毫秒
}
