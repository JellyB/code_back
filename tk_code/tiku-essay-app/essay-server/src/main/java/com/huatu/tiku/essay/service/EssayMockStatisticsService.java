package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.vo.resp.EssayMockStatisticsVO;
import com.huatu.tiku.essay.util.PageUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/7 13:42
 * @Modefied By:
 */
public interface EssayMockStatisticsService {
    public EssayMockStatisticsVO firstGet(Long mockExamId);
    public PageUtil<Object> getPage(Pageable pageable, Long mockExamId);
    public ModelAndView getExcel(Long mockExamId,ArrayList<Long> areaId);
}
