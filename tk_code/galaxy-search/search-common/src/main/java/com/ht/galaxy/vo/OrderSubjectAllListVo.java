package com.ht.galaxy.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author jbzm
 * @Date Create on 2018/4/18 15:46
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSubjectAllListVo {
    private String course;
    private Long count;
}
