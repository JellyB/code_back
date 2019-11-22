package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by x6 on 2018/2/6.
 * 批改免费的用户白名单
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name="v_essay_correct_free_user")
@DynamicUpdate
@DynamicInsert
public class EssayCorrectFreeUser  extends BaseEntity implements Serializable {
    /**
     * status 1正常  -1删除
     * bizStatus  1 上线   0 下线（暂时不用）
     */

    //用户名称
    private String userName;
    //用户id
    private int userId;
    //订单id
    private long orderId;

    //开始时间
    private Date startTime;
    //结束时间
    private Date endTime;

}
