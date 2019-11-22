package com.huatu.tiku.entity.subject;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "subject")
public class Subject extends BaseEntity {

    private String name;

    private Long parent;

    private Integer level;

}
