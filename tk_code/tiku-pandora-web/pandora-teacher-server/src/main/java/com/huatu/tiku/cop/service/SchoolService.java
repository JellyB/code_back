package com.huatu.tiku.cop.service;

import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

/**
 * @author zhaoxi
 * @Description: 公安招警-院校管理
 * @date 2018/8/17下午3:20
 */
public interface SchoolService {

    Object findAreaList();

    Object findSchoolList();

    ModelAndView importData(int paperId);

    ModelAndView importMockData(int subject);

    ModelAndView importLineData(int paperId);

    List<Map<String, Object>> getUserInfo(List<Long> matchUserIds);
}
