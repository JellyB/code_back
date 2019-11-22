package com.huatu.ztk.paper.bean;


import lombok.*;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.List;

/**
 *
 * 整套题的答题卷
 * Created by shaojieyue
 * Created time 2016-04-29 17:43
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@ToString(callSuper=true)
public class CompositeCard extends StandardCard implements Serializable{
    private static final long serialVersionUID = 1L;
    private double lineTestScore;
    private double essayScore;
    //申论模考大赛试卷id
    private long essayPaperId;
}
