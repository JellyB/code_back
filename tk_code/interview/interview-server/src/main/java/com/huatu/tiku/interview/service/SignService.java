package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.util.common.PageUtil;
import org.springframework.web.servlet.ModelAndView;

/**
 * 学员签到管理
 */
public interface SignService {
    PageUtil findByConditions(int page, int pageSize, String uname, long classId);

    ModelAndView export(String uname, long classId);
}
