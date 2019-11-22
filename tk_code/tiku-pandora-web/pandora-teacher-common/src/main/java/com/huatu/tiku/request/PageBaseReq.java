package com.huatu.tiku.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by duanxiangchao on 2018/5/8
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageBaseReq extends BaseReq {

    private Integer page = 1;
    private Integer size = 10;

}
