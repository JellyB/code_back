package com.huatu.ztk.backend.paperModule.controller;

import com.huatu.ztk.backend.paperModule.bean.PaperModuleBean;
import com.huatu.ztk.backend.paperModule.service.PaperModuleService;
import com.huatu.ztk.backend.user.service.UserService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by ht on 2016/12/21.
 */
@RestController
@RequestMapping("/paperModule")
public class PaperModuleController {

    private static final Logger logger = LoggerFactory.getLogger(PaperModuleController.class);

    @Autowired
    private PaperModuleService paperModuleService;


    /**
     * 获取列表
     *
     * @param subjectId 0表示全部
     * @return
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(defaultValue = "0") int subjectId,HttpServletRequest request) throws BizException {
        long userId = UserService.getUserId(request);
        return paperModuleService.findList(subjectId, userId);
    }

    /**
     * 获取单个
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findById(@RequestParam int id) throws BizException {
        return paperModuleService.findById(id);
    }


    /**
     * 删除
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void delete(@RequestParam int id) throws BizException{
        paperModuleService.delete(id);
    }


    /**
     * 更新
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object update(@RequestBody PaperModuleBean paperModuleBean) throws BizException{
        logger.info("modify json={}",JsonUtil.toJson(paperModuleBean));
        paperModuleService.update(paperModuleBean);
        return SuccessMessage.create("修改成功");
    }


    /**
     * 新增
     *
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object add(@RequestBody PaperModuleBean paperModuleBean, HttpServletRequest request) throws BizException{
        logger.info("add json={}", JsonUtil.toJson(paperModuleBean));

        long uid = UserService.getUserId(request);
        paperModuleBean.setCreateBy(uid);
        paperModuleService.insert(paperModuleBean);
        return SuccessMessage.create("添加成功");
    }
}
