package com.huatu.ztk.search.bean;


import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by shaojieyue
 * Created time 2016-05-04 16:40
 */

@Data
@Builder
public class SearchResult {
    private int currentPage;//搜索当前页数
    private long total;//搜索到的总记录数
    List<QuestoinSearchBean> results;//搜索出来的结果
}
