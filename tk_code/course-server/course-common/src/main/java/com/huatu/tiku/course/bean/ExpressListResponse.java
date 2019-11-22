package com.huatu.tiku.course.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * 用于物流列表返回值的bean
 * Created by linkang on 12/6/16.
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ExpressListResponse {
    private int code;
    private String msg;
    private ArrayList<ExpressListItem> data;
}
