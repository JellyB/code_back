package com.huatu.tiku.match.util;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-07 下午7:00
 **/
@Getter
@Setter
@NoArgsConstructor
public class Page<E> implements Serializable {
    private long total;
    private Integer next;
    private long totalPage;
    private List<E> list;


    public Page(long total, Integer next, long totalPage) {
        this.total = total;
        this.next = next;
        this.totalPage = totalPage;
    }

    @Builder
    public Page(long total, Integer next, long totalPage, List<E> list) {
        this.total = total;
        this.next = next;
        this.totalPage = totalPage;
        this.list = list;
    }

    public static Page create(long total, int page, int size, List list){
        int totalPage = (int)Math.ceil((double)total/(double)size);
        Page pageInfo = new Page();
        pageInfo.setTotal(total);
        pageInfo.setList(list);
        pageInfo.setTotalPage(totalPage);
        pageInfo.setNext((page + 1) * size > total ? 0 : 1);
        return pageInfo;
    }
}
