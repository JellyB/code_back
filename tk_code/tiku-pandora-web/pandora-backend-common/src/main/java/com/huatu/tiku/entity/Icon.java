package com.huatu.tiku.entity;

import com.huatu.common.bean.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-10-25 1:32 PM
 **/

@Data
@NoArgsConstructor
@Table(name = "icon")
public class Icon extends BaseEntity {

    private Integer subject;

    private String type;

    private String name;

    private String url;

    private String info;

    private Integer sort;

    public Icon(String type, String name, String url, Integer sort) {
        this.type = type;
        this.name = name;
        this.url = url;
        this.sort = sort;
    }

    public Icon(Integer subject, String type, String name, String url, Integer sort) {
        this.subject = subject;
        this.type = type;
        this.name = name;
        this.url = url;
        this.sort = sort;
    }
}
