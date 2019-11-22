package com.huatu.tiku.essay.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.Collections;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PageUtil<T> implements Serializable {
    T result;
    int next;
    long total;
    long totalPage;

    public PageUtil(T result){
        this.result = result;
        this.next = 0;
        this.total = 0;
        this.totalPage = 0;
    }

    public PageUtil(T result, Page page){
        this.result = result;
        this.next = page.hasNext()? 1 : 0;
        this.total = page.getTotalElements();
        this.totalPage = page.getTotalPages();
    }

}
