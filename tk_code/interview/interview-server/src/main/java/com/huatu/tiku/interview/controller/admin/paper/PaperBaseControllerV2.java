package com.huatu.tiku.interview.controller.admin.paper;

import com.huatu.tiku.interview.constant.BaseInfo;
import com.huatu.tiku.interview.constant.ResultEnum;
import com.huatu.tiku.interview.constant.WXStatusEnum;
import com.huatu.tiku.interview.entity.po.PaperInfo;
import com.huatu.tiku.interview.entity.result.Result;
import com.huatu.tiku.interview.entity.vo.request.PaperAllInfoVo;
import com.huatu.tiku.interview.service.PaperInfoService;
import com.huatu.tiku.interview.userHandler.interceptor.UserInfoHolder;
import com.huatu.tiku.interview.util.LogPrint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 处理 试卷-试题 基础信息维护
 * Created by junli on 2018/4/11.
 */
@Slf4j
@RestController
@RequestMapping(value = "v2/end/paper", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PaperBaseControllerV2 {

    @Autowired
    private PaperInfoService paperInfoService;

    /**
     * 课程信息编辑 -- 课程信息-试题信息
     */
    @LogPrint
    @RequestMapping(value = "edit", method = RequestMethod.POST)
    public Result edit(@RequestBody PaperAllInfoVo paperAllInfoVo) {
        long adminId = UserInfoHolder.get();
        if (paperAllInfoVo.getId() == 0) {
            paperAllInfoVo.setCreator(adminId + "");
        } else {
            PaperInfo paperInfo = paperInfoService.findById(paperAllInfoVo.getId());
            if (null == paperInfo || paperInfo.getStatus() != WXStatusEnum.Status.NORMAL.getStatus()) {
                return Result.build(ResultEnum.ERROR.getCode(), "待修改数据不存在");
            }
            if (paperInfo.getBizStatus() != BaseInfo.PAPER_STATUS.UN_PUSHED.getState()) {
                return Result.build(ResultEnum.ERROR.getCode(), "已推送数据不能修改");
            }
            paperAllInfoVo.setCreator(paperInfo.getCreator());
            paperAllInfoVo.setGmtCreate(paperInfo.getGmtCreate());

            paperAllInfoVo.setModifier(adminId + "");

        }
        paperInfoService.save(paperAllInfoVo);
        return Result.ok();
    }


    /**
     * 推送信息
     */
    @LogPrint
    @RequestMapping(value = "push/{id}/{classId}", method = RequestMethod.GET)
    public Result push(@PathVariable("id") long id,
                        @PathVariable("classId") long classId) {
        long adminId = UserInfoHolder.get();
        paperInfoService.pushV2(id,classId,adminId);
        return Result.ok();
    }

    /**
     * 获取统计详情
     */
    @LogPrint
    @RequestMapping(value = "meta/{id}/{classId}",method = RequestMethod.GET)
    public Result meta(@PathVariable("id") long id,@PathVariable("classId") long classId){
        long adminId = UserInfoHolder.get();
        Map<String, Object> meta = paperInfoService.metaV2(id,adminId,classId,0);
        return Result.ok(meta);
    }

}
