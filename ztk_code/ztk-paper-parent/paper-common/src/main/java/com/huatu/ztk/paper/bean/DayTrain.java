package com.huatu.ztk.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-05-20 21:44
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
@Document(collection = "day_train")
public class DayTrain implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;//每日特训id
    private int questionCount;//特训试题个数
    private int allCount;//所有需要练习次数
    private int finishCount;//训练点完成个数
    List<TrainPoint> points;//特训点列表
    @CreatedDate
    private Date createTime;//创建时间

}
