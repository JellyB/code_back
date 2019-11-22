package com.huatu.tiku.essay.web.controller.admin.edu;

import com.huatu.tiku.essay.service.EssayEduService;
import com.huatu.tiku.essay.service.EssayPaperService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.essay.util.PageUtil;
import com.huatu.tiku.essay.vo.admin.AdminPaperWithQuestionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("eduapi/paper")
public class EssayPaperThirdController {

    @Autowired
    private EssayPaperService essayPaperService;
    @Autowired
    EssayEduService essayEduService;

    @LogPrint
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findByConditions(@RequestParam(defaultValue = "") String name,
                                                               @RequestParam(defaultValue = "9998") long areaId) {
        return essayPaperService.findByAreaOrName(name, areaId);
    }


    @LogPrint
    @GetMapping(value = "{paperId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object paperInfo(@PathVariable long paperId){
        return essayPaperService.findInfoByIdForEdu(paperId);
    }

    /**
     * 模考大赛分页查询
     * @param startTime
     * @param endTime
     * @param name
     * @param status
     * @param page
     * @param size
     * @return
     */
    @LogPrint
    @GetMapping("mock/list")
    public Object mockList(@RequestParam(defaultValue = "-1") long startTime,
                           @RequestParam(defaultValue = "-1") long endTime,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "-1") int tagId,
                           @RequestParam(defaultValue = "") String paperId,
                           @RequestParam(defaultValue = "-1") int status,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = new PageRequest(page - 1, size, Sort.Direction.DESC, "gmtCreate");
        return essayPaperService.findByConditionsForEdu(name,status,startTime,endTime,tagId,paperId,pageable);
    }

    /**
     * 模考大赛用户数据分页查询
     * @param paperId
     * @param page
     * @param size
     * @return
     */
    @LogPrint
    @GetMapping("mock/scores")
    public Object mockList(@RequestParam long paperId,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = new PageRequest(page - 1, size, Sort.Direction.DESC, "gmtCreate");
        return essayEduService.findUserMetas(paperId,pageable);
    }

}
