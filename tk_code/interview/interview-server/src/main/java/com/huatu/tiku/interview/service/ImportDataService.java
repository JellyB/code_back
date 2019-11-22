package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.template.TemplateMsgResult;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zhaoxi
 * @Description: 导出学员每日练习之后的测评报告
 * @date 2018/7/25下午7:55
 */
public interface ImportDataService {
    ModelAndView importData(Long type);
}
