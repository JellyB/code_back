package com.huatu.ztk.paper.bean;


import lombok.*;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 *
 * 整套题的答题卷
 * Created by shaojieyue
 * Created time 2016-04-29 17:43
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@ToString(callSuper=true)
public class StandardCard extends AnswerCard implements Serializable{
    private static final long serialVersionUID = 1L;

    private Paper paper;
    private CardUserMeta cardUserMeta;//用户做题统计

    @Transient
    private MatchCardUserMeta matchMeta;

    /**
     * pc端模考解决精确度问题
     */
    private String idStr;

    @Transient
    private Long currentTime;

}
