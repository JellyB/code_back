package com.ht.base.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhengyi
 * @date 2018/11/1 5:58 PM
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageParameter {
    Object content;
    int pageNum;
    int number;
    int totalPages;
    long totalElements;
    int size;
    int page;
}