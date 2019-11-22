package com.huatu.splider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by x6 on 2018/5/31.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FbLoginVO {
    private Long id;
    private String email;
    private String phone;
    private Long createdTime;
    private String identity;
}
