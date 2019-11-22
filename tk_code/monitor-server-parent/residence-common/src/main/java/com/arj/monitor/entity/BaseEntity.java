package com.arj.monitor.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author zhouwei
 * @Description: 基础实体类
 * @create 2018-10-15 上午10:49
 **/
@Data
@NoArgsConstructor
/**
 * 标识父类,标识该类不映射到数据库表，所以不能再有@Entity或@Table注解
 * */
@MappedSuperclass
/**
 * 即在插入和修改数据的时候,语句中只包括要插入或者修改的字段,默认为true，所以可不写，写出来是为了让读了这段代码你的了解下（哈哈）
 */
@DynamicInsert
@DynamicUpdate
public class BaseEntity implements Serializable {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    @Column(columnDefinition = "smallint default 0")
    protected Integer bizStatus = 0;
    @Column(columnDefinition = "smallint default 1")
    protected Integer status = 1;
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

}
