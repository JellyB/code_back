package com.huatu.tiku.essay.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 地区表
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "v_essay_area")
@EqualsAndHashCode(callSuper = false)
@Builder
@DynamicUpdate
@DynamicInsert
public class EssayQuestionBelongPaperArea  implements Serializable {
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
