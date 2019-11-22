package com.huatu.ztk.backend.paper.controller;

import com.huatu.ztk.backend.paper.bean.PaperBean;
import com.huatu.ztk.backend.paper.service.EstimateService;
import com.huatu.ztk.backend.paper.service.ExportType;
import com.huatu.ztk.backend.user.service.UserService;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by linkang on 3/6/17.
 */

@RestController
@RequestMapping("/paper")
public class EstimateController {

    @Autowired
    private EstimateService estimateService;

    private static final Logger logger = LoggerFactory.getLogger(EstimateController.class);

    /**
     * /**
     * 添加估分
     *
     * @param id
     * @param
     * @return
     */
    @RequestMapping(value = "estimate", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updateAudit(@RequestParam int id) throws BizException {
        estimateService.addEstimate(id);
        return SuccessMessage.create("添加到估分成功");
    }

    /**
     * 获取估分列表
     *
     * @param catgory 科目id，用,分开
     * @param area
     * @param year
     * @param name
     * @param status  0表示获取全部
     * @return
     */
    @RequestMapping(value = "estimate", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object estimateList(@RequestParam(required = false) String catgory,
                               @RequestParam(required = false) String area,
                               @RequestParam(required = false) int year,
                               @RequestParam(required = false) String name,
                               @RequestParam(defaultValue = "0") int status,
                               HttpServletRequest request) throws BizException {
        int uid = UserService.getUserId(request);
        List<PaperBean> paperList = estimateService.findEstimateList(catgory, area, year, name, status, uid);
        return paperList;
    }


    /**
     * 获取试卷考试结果
     *
     * @return
     */
    @RequestMapping(value = "result", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findResult(@RequestParam int id) throws BizException {
        Object result = estimateService.findResult(id);
        return result;
    }

    /**
     * 获取用户答题信息
     *
     * @return
     */
    @RequestMapping(value = "card", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findCard(@RequestParam long id) throws BizException {
        Object result = estimateService.findCard(id);
        return result;
    }


    /**
     * 错题统计
     *
     * @return
     */
    @RequestMapping(value = "errorCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findResultSet(@RequestParam int id) throws BizException {
        Object result = estimateService.findErrorCount(id);
        return result;
    }


    /**
     * 用户成绩导出
     *
     * @return
     */
    @RequestMapping(value = "resultFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findEstimateResultFileUrl(@RequestParam int id) throws BizException {
        String url = estimateService.findEstimateResultFileUrl(id);
        return url;
    }


    /**
     * 用户成绩导出
     *
     * @return
     */
    @RequestMapping(value = "userPhoneFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findUserPhoneCollect(@RequestParam int id, @RequestParam String area) throws BizException {
        String url = estimateService.findUserPhoneCollect(id, area);
        return SuccessMessage.create(url);
    }


    /**
     * 更新估分试卷状态
     *
     * @return
     */
    @RequestMapping(value = "estimateStatus", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updateEstimateStatus(@RequestParam int id) throws BizException {
        String msg = estimateService.updateEstimateStatus(id);
        return SuccessMessage.create(msg);
    }

    /**
     * 试题导出
     *
     * @param id
     * @param type
     * @return
     */
    @RequestMapping(value = "exportQuestion", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object exportQuestion(@RequestParam int id, @RequestParam int type) throws Exception {
        String downUrl = estimateService.exportQuestion(id, type);
        if (StringUtils.isEmpty(downUrl)) {
            return ErrorResult.create(1110002, "服务器错误");
        }
        return SuccessMessage.create(downUrl);
    }
}
