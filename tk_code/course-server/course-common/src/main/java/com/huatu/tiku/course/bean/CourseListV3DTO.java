package com.huatu.tiku.course.bean;

import com.huatu.common.bean.CacheableValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author hanchao
 * @date 2017/9/14 14:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseListV3DTO extends CacheableValue {

    public static final String KEY_SALE_START = "saleStart";
    public static final String KEY_SALE_END = "saleEnd";


    private int next;
    private List<Map> result;
    private boolean degrade;
}
