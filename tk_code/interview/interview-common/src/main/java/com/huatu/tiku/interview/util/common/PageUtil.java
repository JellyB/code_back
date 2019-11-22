package com.huatu.tiku.interview.util.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PageUtil<T> {
    T result;
    int next;
    long total;
    long totalPage;
}
