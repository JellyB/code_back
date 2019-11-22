package com.huatu.tiku.essay.vo.excel;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 11:49
 * @Modefied By:
 */
@Data
@Setter
@Getter
public class Param {
    private Long mockExamId;
    private ArrayList<Long> areaIds;
}
