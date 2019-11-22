package com.huatu.tiku.entity.material;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * 材料表
 * Created by huangqp on 2018\4\14 0014.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "material")
public class Material extends BaseEntity {
    /**
     * 材料内容
     */
    private String content;
    /**
     * 材料查重字段
     */
    private String contentFilter;
}
