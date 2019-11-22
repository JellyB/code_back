package com.huatu.tiku.teacher.service.download;

import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.ztk.commons.exception.BizException;

import java.util.List;

/**
 * 表格数据处理
 * Created by huangqingpeng on 2019/3/19.
 */
public interface ExcelHandleService {

    /**
     * 批量下载试卷下试题的基础属性
     * @param ids
     * @param typeInfo
     * @return
     */
    String downloads(List<Long> ids, PaperInfoEnum.TypeInfo typeInfo) throws BizException;
}
