package com.huatu.tiku.essay.util;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/16
 */
@Data
public class PageHandler<T> implements Serializable {

    List<T> result;
    int next;
    long total;
    long totalPage;

    public PageHandler(List<T> result, Page<T> page){
        this.result = result;
        this.next = page.hasNext()? 1 : 0;
        this.total = page.getTotalElements();
        this.totalPage = page.getTotalPages();
    }

}
