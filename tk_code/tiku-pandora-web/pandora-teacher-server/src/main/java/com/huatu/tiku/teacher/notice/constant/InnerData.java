package com.huatu.tiku.teacher.notice.constant;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-12-12 下午3:15
 **/
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class InnerData<T> {
    private int total;
    private int next;
    private int totalPage;
    private List<T> list;

    public static InnerData newInstance() {
        InnerData innerData = new InnerData(0, 0, 0, new ArrayList());
        return innerData;
    }
}
