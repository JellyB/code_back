package com.huatu.morphling.dao.jpa.entity;
// Generated 2017-11-10 17:53:57 by Hibernate Tools 5.2.1.Final

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * ShellLog generated by hbm2java
 */
@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "shell_log")
public class ShellLog implements java.io.Serializable {

    private long id;
    private String status;
    private String content;
    private Date createTime;

    @Id

    @Column(name = "id", unique = true, nullable = false)
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "status", nullable = false)
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "content", nullable = false, length = 65535)
    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_time", nullable = false, length = 19)
    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
