package com.huatu.tiku.match.util;

import com.github.pagehelper.PageInfo;
import org.springframework.data.domain.PageImpl;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-11-07 下午7:02
 **/
public class PageUtil {

    public static Page parsePageInfo(PageInfo<?> pageInfo){
        Page page = new Page(pageInfo.getTotal(), pageInfo.isHasNextPage() ? 1 : 0, pageInfo.getPages());
        page.setList(pageInfo.getList());
        return page;
    }

    public static Page parsePageInfo(List list, long total, int page, int size ){
        long totalPage = total / size == 0 ? total % size : (total % size + 1);
        return Page.builder()
                .list(list)
                .next((page + 1) * size > total ? 0 : 1)
                .total(total)
                .totalPage(totalPage)
                .build();
    }

    public static Page parseMongoPageInfo(PageImpl pageImpl){
        return Page.builder()
                .list(pageImpl.getContent())
                .total(pageImpl.getTotalElements())
                .totalPage(pageImpl.getTotalPages())
                .next(pageImpl.hasNext() ? 1 : 0)
                .build();
    }
}
