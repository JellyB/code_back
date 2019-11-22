package com.huatu.tiku.interview.controller.admin.v1;

import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.entity.po.ModulePractice;
import com.huatu.tiku.interview.entity.po.PaperPractice;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.vo.request.PaperCommitVO;
import com.huatu.tiku.interview.service.LearningSituationService;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 学习情况 管理
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 14:34
 */
@Slf4j
@RestController
@RequestMapping("/end/ls")
public class LearningSituationController {

    @Autowired
    private LearningSituationService learningSituationService;

    /**
     * 新增学员学习情况(套题演练)
     * @param paperPractice
     * @return
     */
    @LogPrint
    @PostMapping(value="paper",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result paper(@RequestBody PaperPractice paperPractice){
        return learningSituationService.savePaperPractice(paperPractice)? Result.ok(): Result.build(ResultEnum.INSERT_FAIL);
    }

    /**
     * 新增学员学习情况(模块练习)
     * @param modulePractice
     * @return
     */
    @LogPrint
    @PostMapping(value="module",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result module(@RequestBody ModulePractice modulePractice){
        return learningSituationService.saveModulePractice(modulePractice)? Result.ok(): Result.build(ResultEnum.INSERT_FAIL);
    }


    /**
     * 新增学员学习情况(全真模考)
     * @param vo
     * @return
     */
    @LogPrint
    @PostMapping(value="mock",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result mock(@RequestBody PaperCommitVO vo ){
        return learningSituationService.saveMockPractice(vo)? Result.ok(): Result.build(ResultEnum.INSERT_FAIL);
    }

    /**
     * 查询学习情况详情
     */
    @LogPrint
    @PostMapping(value="detail",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result detail(@RequestParam String openId,@RequestParam String date){
        return Result.ok(learningSituationService.detail(openId,date));
    }







//
//    /**
//     * 删除学员学习情况
//     * @param id
//     * @return
//     */
//    @LogPrint
//    @DeleteMapping(value="/{id}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Result del(@PathVariable  Long id){
//
//        return learningSituationService.del(id) == 0 ?Result.build(ResultEnum.DELETE_FAIL):Result.ok();
//    }
//
//    /**
//     *  修改学员学习情况
//     * @param learningSituation
//     * @return
//     */
//    @LogPrint
//    @PutMapping(value="",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Result update(PaperPractice learningSituation){
//        learningSituation = learningSituationService.findOne(learningSituation.getId());
//        if (learningSituation == null){
//            return Result.build(ResultEnum.UPDATE_FAIL);
//        }else{
//            return learningSituationService.save(learningSituation)? Result.ok(): Result.build(ResultEnum.UPDATE_FAIL);
//        }
//    }


//    /**
//     * 查询某条学习情况记录
//     */
//    @LogPrint
//    @GetMapping(value="detail/{id}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Result detail(@PathVariable  Long id){
//        log.info("id：{}",id);
//        PaperPractice learningSituation = learningSituationService.findOne(id);
//        return learningSituation != null? Result.ok(learningSituation): Result.build(ResultEnum.FIND_FAIL);
//    }


//    /**
//     * 查询学习情况记录列表
//     */
//    @LogPrint
//    @GetMapping(value="list",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Result list(@RequestParam(name = "name") String name,
//                       @RequestParam(name = "page", defaultValue = "1") int page,
//                       @RequestParam(name = "pageSize", defaultValue = "10") int pageSize){
//
//        Pageable pageRequest = new PageRequest(page - 1, pageSize, Sort.Direction.DESC, "gmtCreate");
//        log.info("name: {},pageRequest: {}", name, pageRequest);
//        if(null == name){
//            name = "";
//        }
//        List<PaperPractice> list = learningSituationService.findList(name,pageRequest);
//        long c = learningSituationService.countByNameLikeStatus(name);
//
//        PageUtil p = PageUtil.builder()
//                .result(list)
//                .next(c > page * pageSize ? 1 : 0)
//                .total(c)
//                .totalPage((0 == c % pageSize) ? (c / pageSize) : (c / pageSize + 1))
//                .build();
//        return  Result.ok(p);
//    }




}
