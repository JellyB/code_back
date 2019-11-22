package com.huatu.tiku.entity.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by lijun on 2018/8/8
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaperModuleInfo {
    private Integer id;
    private String name;
}
