package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/26 10:13
 * @Description 白名单
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="t_white_list")
public class WhiteList extends BaseEntity{

    private String name;
    private String phone;
}
