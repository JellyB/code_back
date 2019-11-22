package com.huatu.ztk.user.util;

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
