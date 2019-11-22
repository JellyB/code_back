package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/27 18:45
 * @Description 推送记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "t_push_log")
@Entity
public class PushLog {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private String openId;
    private Integer pushType;
    private Boolean status;
    private Date pushTime;
    private Long classId;

}
