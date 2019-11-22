package com.huatu.tiku.essay.vo.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2017/12/22.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayCreatePdfVO {

    private long id;
    private int type;
}
