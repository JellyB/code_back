package com.huatu.tiku.teacher.service.paper;


import org.springframework.web.servlet.ModelAndView;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/5/24
 * @描述 导出模考大赛, 精准估分, 专项模考学员考试数据
 */

public interface PaperActivityImportDataService {

    ModelAndView importUserExamData(int paperId);

}
