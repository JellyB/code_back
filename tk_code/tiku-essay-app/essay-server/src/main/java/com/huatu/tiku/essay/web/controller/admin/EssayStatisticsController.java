package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssaySimilarQuestionGroupVO;
import com.huatu.tiku.essay.service.EssayStatisticsService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * @author create by jbzm on 2018年1月4日17:49:16
 */
@RestController
@Slf4j
@RequestMapping("end/statistic")
public class EssayStatisticsController {
    @Autowired
    EssayStatisticsService essayStatisticsService;

    /**
     *  数据统计 - 单题列表
     * @return 分页之后的结果
     */
    @LogPrint
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil<List<EssaySimilarQuestionGroupVO>> list(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int pageSize,
                                                            @RequestParam(name = "title", defaultValue = "") String title,
                                                            @RequestParam(name = "type", defaultValue = "0") int type) {
        //利用SpringDate jpa 提供的方法来完成分页:  第三个参数是用来排序的(通过id降序排列返回数据)
        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "id");
        return essayStatisticsService.findAllGroup(pageable, title, type);
    }


    /**
     * 查询批改单/套题总数
     *
     * @return 查询总数
     */
    @LogPrint
    @GetMapping(value = "sum/{type}/{paperType}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public long correctSum(@PathVariable int type,@PathVariable int paperType) {
        return essayStatisticsService.countCorrectSum(type,paperType);
    }

    /**
     * 数据统计 - 套题列表
     *
     * @param title
     * @param year
     * @param areaId
     * @return
     */
    @LogPrint
    @GetMapping(value = "paperList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object paperList(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int pageSize,
                            @RequestParam(name = "title", defaultValue = "") String title,
                            @RequestParam(name = "year", defaultValue = "") String year,
                            @RequestParam(name = "areaId", defaultValue = "-1") int areaId,
                            @RequestParam(name = "paperType") int paperType) {
        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "id");
        return essayStatisticsService.findAllPaper(pageable, title, year, areaId,paperType);
    }

    /**
     * 查询单题结果信息
     *
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping(value = "detailSingle/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object detailSingle(@PathVariable Long id) {
        return essayStatisticsService.findBySingleGroupId(id,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }

    /**
     * 成绩列表
     *
     * @param areaId
     * @param questionId
     * @return
     */
    @LogPrint
    @PostMapping(value = "detailSingle/{areaId}/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object detailSingleUser(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int pageSize, @PathVariable Long areaId, @PathVariable Long questionId) {
        PageRequest pageable = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "id");
        return essayStatisticsService.findBySingleGroupIdAndPage(pageable, areaId, questionId);
    }

    /**
     * 添加套题详细信息
     *
     * @param id
     * @return
     */
    @LogPrint
    @GetMapping(value = "detailPaper/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object detailPaper(@PathVariable Long id) {
        return essayStatisticsService.findByPaperId(id,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }
    @LogPrint
    @GetMapping(value = "getPageExcel")
    public ModelAndView getPageExcel(Long pageId){

        return essayStatisticsService.getPageExcel(pageId, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }
    @LogPrint
    @GetMapping(value = "getQuestionExcel")
    public ModelAndView getQuestionExcel(Long groupId){

        return essayStatisticsService.getQuestionExcel(groupId,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
    }


}

