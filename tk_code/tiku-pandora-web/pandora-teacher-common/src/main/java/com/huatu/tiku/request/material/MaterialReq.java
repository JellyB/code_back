package com.huatu.tiku.request.material;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by huangqp on 2018\6\28 0028.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialReq {
    /**
     * 材料id
     */
    private Long materialId;
    /**
     * 材料内容
     */
    private String content;
}

