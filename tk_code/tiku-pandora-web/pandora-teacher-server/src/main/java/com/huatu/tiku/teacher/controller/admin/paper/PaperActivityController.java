package com.huatu.tiku.teacher.controller.admin.paper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.ErrorResult;
import com.huatu.common.SuccessMessage;
import com.huatu.common.SuccessResponse;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.request.paper.InsertActivityPaperReq;
import com.huatu.tiku.request.paper.SelectActivityReq;
import com.huatu.tiku.request.paper.UpdateActivityPaperReq;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.util.log.LogPrint;

/**
 * Created by huangqp on 2018\7\4 0004.
 */
@RestController
@RequestMapping("paper/activity")
public class PaperActivityController {
    @Autowired
    PaperActivityService paperActivityService;


    /**
     * 创建活动试卷
     *
     * @return
     */
    @LogPrint
    @PostMapping("")
    public Object createPaper(@Valid @RequestBody InsertActivityPaperReq insertActivityPaperReq,
                              BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BizException(ErrorResult.create(1000001, bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        if (insertActivityPaperReq.getPaperId() != null && insertActivityPaperReq.getPaperId() > 0) {
            return paperActivityService.insertPaper(insertActivityPaperReq, insertActivityPaperReq.getPaperId());
        } else {
            return paperActivityService.insertPaper(insertActivityPaperReq, -1L);
        }
    }


    /**
     * 删除活动试卷
     *
     * @param paperId
     * @return
     */
    @LogPrint
    @DeleteMapping("")
    public Object deletePaper(@RequestParam Long paperId) {
        paperActivityService.deletePaper(paperId);
        return SuccessMessage.create("试卷删除成功");
    }

    /**
     * 修改活动卷信息
     *
     * @param updateActivityPaperReq
     * @param bindingResult
     * @return
     */
    @LogPrint
    @PutMapping("")
    public Object updatePaper(@Valid @RequestBody UpdateActivityPaperReq updateActivityPaperReq,
                              BindingResult bindingResult) {
        // 校验参数是否合法
        if (bindingResult.hasErrors()) {
            throw new BizException(ErrorResult.create(1000001, bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }
        paperActivityService.updatePaper(updateActivityPaperReq);
        return SuccessMessage.create("试卷修改成功");
    }


    /**
     * 活动列表-编辑，回显数据
     */
    @LogPrint
    @GetMapping("/{id}")
    public Object findPaper(@PathVariable Long id) {
        SelectActivityReq paper = paperActivityService.findById(id);
        Integer scoreFlag = paper.getScoreFlag();
        if(null != scoreFlag && scoreFlag.intValue() != 0){
            paper.setTotalScore(paper.getQuestionTotalScore());
        }
        return paper;
    }


    /**
     * 统计试卷或者活动的参考人数
     *
     * @param paperId   试卷id或者活动id
     * @param paperType 试卷类型1实体卷2活动卷
     * @return
     */
    @LogPrint
    @GetMapping("exam/count")
    public Object countActivityInfo(@RequestParam Long paperId,
                                    @RequestParam(defaultValue = "2") Integer paperType) {

        //活动卷统计
        if (PaperInfoEnum.TypeInfo.SIMULATION.getCode() == paperType.intValue()) {//只有活动卷自身统计数据
            Map mapData = paperActivityService.countExamInfo(paperId);
            if (mapData.size() > 0) {
                return mapData;
            }
            throw new BizException(ErrorResult.create(1000213, "没有考试数据"));
        } else {      //统计所有关联试卷的活动数据
            //根据实体卷id查询关联的活动卷id
            List<Long> paperIds = paperActivityService.findByPaperId(paperId);
            Double totalScore = 0D; //考试总分累加值
            Integer count = 0;  //考试人数累加值
            Double difficult = 0D; //难度
            if (CollectionUtils.isNotEmpty(paperIds)) {
                for (Long tempId : paperIds) {
                    Map map = paperActivityService.countExamInfo(tempId);
                    if (map.size() != 0) {
                        count += Integer.parseInt(map.get("count").toString());
                        totalScore += Integer.parseInt(map.get("count").toString()) * Double.parseDouble(map.get("average").toString());
                        difficult = Double.parseDouble(map.get("difficult").toString());
                    }
                }
            }
            if (difficult > 0D) {   //是否有关联的活动，且活动有数据
                Map mapData = Maps.newHashMap();
                mapData.put("count", count);
                mapData.put("average", totalScore / count);
                mapData.put("difficult", difficult);
                return mapData;
            }
            throw new BizException(ErrorResult.create(1000213, "没有考试数据"));
        }
    }

    /**
     * 根据实体卷ID生成活动卷ID-用实体卷ID替换活动卷ID
     */
    @LogPrint
    @GetMapping("createActivityByPaperId")
    public Object createActivityByPaperId() {
        paperActivityService.createActivityByPaperId();
        return SuccessMessage.create("处理成功");

    }

    /**
     * 根据试卷ID查询试卷信息
     */
    @LogPrint
    @GetMapping("list")
    public Object list(@RequestParam String ids) {
        ArrayList<Long> paperIds = Lists.newArrayList();
        for (String s : ids.split(",")) {
            if (!StringUtils.isNumber(s)) {
                throw new BizException(ErrorResult.create(1000101, "参数非法"));
            }
            paperIds.add(Long.parseLong(s));
        }
        List<PaperActivity> activities = paperActivityService.selectByIds(paperIds);
        if (CollectionUtils.isEmpty(activities)) {
            return Lists.newArrayList();
        }
        return activities;

    }

    /**
     * 完成活动卷信息修改
     *
     * @return
     */
    @LogPrint
    @PutMapping("finish")
    public Object finished(@RequestBody UpdateActivityPaperReq updateActivityPaperReq) {
        // 校验参数是否合法
        SelectActivityReq paperActivity = paperActivityService.findById(updateActivityPaperReq.getId());
        BeanUtils.copyProperties(paperActivity,updateActivityPaperReq);
        paperActivityService.updatePaper(updateActivityPaperReq);
        return SuccessMessage.create("试卷修改成功");
    }
    
    /**
     * 获取试卷小程序二维码
     * @param id
     * @param subjectId
     * @return
     */
    @LogPrint
    @GetMapping("/QRCode/{id}")
	public Object getQRCode(@PathVariable Long id, Long subjectId) {
		Object qrCode = paperActivityService.getQRCode(id, subjectId);
		
		return new SuccessResponse(qrCode);
	}
    
}

