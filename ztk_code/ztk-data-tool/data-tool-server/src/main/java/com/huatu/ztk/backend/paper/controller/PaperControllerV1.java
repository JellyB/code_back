package com.huatu.ztk.backend.paper.controller;

import com.huatu.ztk.backend.paper.bean.PaperBean;
import com.huatu.ztk.backend.paper.service.PaperServiceV1;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by huangqp on 2018\4\2 0002.
 *行测下载，做校验使用，不给定试题ID（没有主观题数据）
 */
@RestController
@RequestMapping(value = "/paper/v1")
public class PaperControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(PaperControllerV1.class);

    @Autowired
    private PaperServiceV1 PaperServiceV1;

    /**
     *  所有试卷下载列表
     *
     * @param catgory 科目
     * @param area    地区
     * @param sYear   开始年份
     * @param eYear   结束年份
     * @return
     */
    @RequestMapping(value = "/allDownList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object allDownList(@RequestParam(required = false) String catgory,
                              @RequestParam(defaultValue = "1")int paperType,
                              @RequestParam(defaultValue = "-1") int id,
                              @RequestParam(defaultValue = "") String name,
                              @RequestParam(required = false) String area,
                              @RequestParam(required = false) int sYear,
                              @RequestParam(required = false) int eYear) {
        List<PaperBean> paperList = PaperServiceV1.allDownListV1(catgory, area, sYear, eYear,paperType,id,name);
        return paperList;
    }
    /**
     * 生成id试卷的下载文档
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createPaperFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createPaperFile(@RequestParam String id) throws Exception {
        Object object =PaperServiceV1.createFile(id);

        return SuccessMessage.create("生成文件成功"+ JsonUtil.toJson(object));
    }

    @RequestMapping(value = "/createPaperFiles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createPaperFiles(@RequestParam String ids) throws Exception {
        Object file = PaperServiceV1.createFile(ids);
        return SuccessMessage.create("生成文件成功"+ JsonUtil.toJson(file));
    }

    /**
     * 查询所有的模考真题演练和模考大赛,同步到mysql（开发专用）
     * @param subjects
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/sync", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deleteCollect( @RequestParam String subjects) throws BizException {
        String[] split = subjects.split(",");
        for (String s : split) {
            PaperServiceV1.syncQuestionsBySubject(Integer.parseInt(s));
        }
        return SuccessMessage.create("更新任务已提交");
    }
}
