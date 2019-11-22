package com.huatu.tiku.interview.entity.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by x6 on 2018/4/13
 * 地区表
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "t_area")
public class Area {
    @Id
    protected long id;
    @Column(columnDefinition = "smallint default 0")
    protected int bizStatus;
    @Column(columnDefinition = "smallint default 1")
    protected int status;
    @Column(columnDefinition = "varchar(128) default ''")
    protected String creator;
    @Column(columnDefinition = "varchar(128) default ''")
    protected String modifier;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    protected Date gmtCreate;
    @org.hibernate.annotations.UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    protected Date gmtModify;



    //地区名称
    private String name;

    //上级城市id
    private long pId;
    //优先级（越小越靠前）
    private int sort;

}
