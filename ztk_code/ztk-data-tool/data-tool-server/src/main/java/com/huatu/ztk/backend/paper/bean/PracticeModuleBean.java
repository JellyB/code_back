package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * Created by linkang on 2/16/17.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PracticeModuleBean {
    private String name;
    private int id;
    private List<PracticeSort> practiceSorts;
}
