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
import com.huatu.tiku.interview.util.common.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 处理 试卷-试题 基础信息维护
 * Created by junli on 2018/4/11.
 */
@Slf4j
@RestController
@RequestMapping(value = "end/paper", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class PaperBaseController {

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
            //TODO 目前只有一个班级，id是1（以后改掉）
            if (paperAllInfoVo.getClassId() == 0){
                log.warn("参数中缺少班级信息");
                paperAllInfoVo.setClassId(1L);
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
     * 课程信息 - 列表查询
     */
    @LogPrint
    @RequestMapping(value = "list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Result list(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "type", defaultValue = "0") int type,
            @RequestParam(value = "paperName") String paperName

    ) {
        PageUtil<PaperInfo> pageInfo = paperInfoService.list(page, pageSize, type, paperName);
        return Result.ok(pageInfo);
    }

    /**
     * 根据ID删除课程信息
     */
    @LogPrint
    @RequestMapping(value = "delete/{id}", method = RequestMethod.GET)
    public Result delete(
            @PathVariable("id") long id
    ) {
        paperInfoService.delete(id);
        return Result.ok();
    }

    @LogPrint
    @RequestMapping(value = "detail/{id}", method = RequestMethod.GET)
    public Result detail(
            @PathVariable("id") long id
    ) {
        PaperAllInfoVo detail = paperInfoService.detail(id);
        return Result.ok(detail);
    }

    /**
     * 推送信息
     */
    @LogPrint
    @RequestMapping(value = "push/{id}", method = RequestMethod.GET)
    public Result push(
            @PathVariable("id") long id
    ) {
        paperInfoService.push(id);
        return Result.ok();
    }

    /**
     * 获取统计详情
     */
    @LogPrint
    @RequestMapping(value = "meta/{id}",method = RequestMethod.GET)
    public Result meta(
            @PathVariable("id") long id
    ){
        Map<String, Object> meta = paperInfoService.meta(id);
        return Result.ok(meta);
    }

    /**
     * 获取互动课堂类型
     */
    @LogPrint
    @RequestMapping(value = "getPaperType", method = RequestMethod.GET)
    public Result getPaperType() {
        List<HashMap<String, Object>> collect = Stream.of(BaseInfo.PAPER_TYPE.values())
                .map(paperType -> new HashMap<String, Object>() {{
                    put("id", paperType.getType());
                    put("name", paperType.getName());
                }})
                .collect(Collectors.toList());
        return Result.ok(collect);
    }
}
