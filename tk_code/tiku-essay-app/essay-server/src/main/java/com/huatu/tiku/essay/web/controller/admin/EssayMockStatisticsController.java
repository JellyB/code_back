package com.huatu.tiku.essay.web.controller.admin;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Lists;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.constant.error.EssayMockErrors;
import com.huatu.tiku.essay.service.EssayMockStatisticsService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.excel.Param;
import com.huatu.tiku.essay.vo.resp.EssayMockStatisticsVO;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/7 13:14
 * @Modefied By:
 */
@Slf4j
@RestController
@RequestMapping("end/mockStatistic")
public class EssayMockStatisticsController {


    @Autowired
    private EssayMockStatisticsService service;

    @LogPrint
    @GetMapping("firstGet")
    public EssayMockStatisticsVO test1(Long mockExamId){
        log.info("mockExamId :{}",mockExamId);
        return service.firstGet(mockExamId);
    }

    @LogPrint
    @GetMapping("getPage")
    public PageUtil<Object> page(@RequestParam(name = "size", defaultValue = "10") Integer size,@RequestParam(name = "page", defaultValue = "1") Integer page, @RequestParam(name = "mockExamId", defaultValue = "") Long mockExamId){
        // 这个是获取分页的。。。！！！
        PageRequest pageable = new PageRequest(page-1,size,new Sort("id"));
        return service.getPage(pageable,mockExamId);
    }

    @LogPrint
    @PostMapping("getExcel")
    public Object getExcel(@RequestBody Param param){
        Long mockExamId = param.getMockExamId();
        ArrayList<Long> areaIds = param.getAreaIds();
        if(mockExamId == null && areaIds == null){
            return null;
        }
        try {
            return service.getExcel(mockExamId,areaIds);
        }catch (Exception e){
            e.printStackTrace();
            throw new BizException(EssayMockErrors.EXCEL_ID_NULL);
        }
    }
//    @GetMapping("getSessionExcel")
//    public ModelAndView getSessionExcel(HttpServletRequest request){
//        Long mockExamId = (Long) request.getSession().getAttribute("excel-mockExamId");
//        ArrayList<Long> areaIds = (ArrayList<Long>) request.getSession().getAttribute("excel-areaIds");
//        if(mockExamId == null && areaIds == null){
//            return null;
//        }
//        return service.getExcel(mockExamId,areaIds);
//    }
    
    /**
     * 模考数据导出
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping("exportMockInfo")
	public ModelAndView getExcel(Long id) {
		Assert.notNull(id, "试卷id不能为空");
		ArrayList<Long> areaIds = Lists.newArrayList();
		return service.getExcel(id, areaIds);
	}
}
