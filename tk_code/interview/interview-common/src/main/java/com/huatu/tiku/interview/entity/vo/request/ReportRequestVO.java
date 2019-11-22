package com.huatu.tiku.interview.entity.vo.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/1/17.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportRequestVO {

//    //第几天(1-7  0代表总计  没有的话取当前日期)
//    private Long id;

    private Long userId;







}
