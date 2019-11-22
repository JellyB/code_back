package com.huatu.tiku.essay.web.controller.admin;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.vo.admin.EssayMockExamVO;
import com.huatu.tiku.essay.service.EssayMockExamService;
import com.huatu.tiku.essay.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Created by x6 on 2017/12/28.
 */
@RestController
@Slf4j
@RequestMapping(value="/end/mock")
public class EssayMockController {

    @Autowired
    EssayMockExamService essayMockExamService;
    /**
     *  申论模考
     */
    @LogPrint
    @PostMapping(value="mock",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object mock(@RequestBody EssayMockExamVO mockExam) throws BizException {
        return essayMockExamService.saveMockPaper( mockExam);
    }


    /**
     *  查询申论模考信息
     */
    @LogPrint
    @GetMapping(value="mock",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayMockExamVO mockDetail(@RequestParam long id) throws BizException {
        return essayMockExamService.queryMockPaper( id);
    }


    /**
     *  是否可查看模考數據統計
     */
    @LogPrint
    @GetMapping(value="calculateButton/{mockId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Boolean calculateButton(@PathVariable long mockId) {
        return essayMockExamService.calculateButton( mockId);
    }


}
