package com.huatu.tiku.interview.entity.po;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@MappedSuperclass
@DynamicInsert
@DynamicUpdate(true)
public class BaseEntity {
    @Id
    @GeneratedValue
    private long id;
    @Column(columnDefinition = "smallint default 0")
    private int bizStatus;
    @Column(columnDefinition = "smallint default 1")
    private int status;
    @Column(columnDefinition = "varchar(128) default 'admin'")
    private String creator;
    @Column(columnDefinition = "varchar(128) default 'admin'")
    private String modifier;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    @org.hibernate.annotations.CreationTimestamp
    private Date gmtCreate;
    @org.hibernate.annotations.UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date gmtModify;
}
