package com.huatu.tiku.essay.web.controller.admin;

/**
 * 中心论点 相关模块
 * Created by x6 on 2017/12/14.
 */

import com.huatu.tiku.essay.vo.resp.EssayCenterThesisVO;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;
import com.huatu.tiku.essay.service.EssayQuestionService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/end/thesis")
public class EssayCenterThesisController {


    @Autowired
    EssayQuestionService essayQuestionService;
    /**
     * 查询中心论点
     * @param page
     * @param pageSize
     * @return
     */
    @LogPrint
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public PageUtil<EssayCenterThesisVO> thesisList(@RequestParam(name = "page", defaultValue = "1") int page,
                                                    @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
                                                    @RequestParam(name = "questionName", defaultValue = "") String questionName ,
                                                    @RequestParam(name = "areaId", defaultValue = "-1") long areaId,
                                                    @RequestParam(name = "year", defaultValue = "") String year) {

        if("0".equals(year.trim())){
            year = "";
        }
       return  essayQuestionService.findThesisByCondition(page,pageSize,questionName,areaId,year);
    }


    /**
     * 编辑是否采纳答案
     * @param id
     * @param type(1 采纳  2不采纳)
     * @return
     */
    @LogPrint
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayUpdateVO adopt(@RequestParam(name = "id", defaultValue = "1") long id,
                               @RequestParam(name = "type", defaultValue = "1") int type) {
        return  essayQuestionService.adopt(id,type);
    }



}
