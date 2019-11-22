package com.huatu.ztk.backend.paper.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class EssayVO {
    String endTime;
    String startTime;
}
