package com.huatu.tiku.dto.request;

import lombok.Data;

import java.util.List;

/**
 * @author zhengyi
 * @date 11/13/18 6:09 PM
 **/
@Data
public class BatchDealAdoption {
    private List<Long> ids;
    private Integer checker;
    private Integer gold = 0;
    String resultContent;
}