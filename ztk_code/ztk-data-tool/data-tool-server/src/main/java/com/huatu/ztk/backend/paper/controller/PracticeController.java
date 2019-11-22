package com.huatu.ztk.backend.paper.controller;

import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.bean.PaperBean;
import com.huatu.ztk.backend.paper.bean.PracticePaperBean;
import com.huatu.ztk.backend.paper.service.PracticeService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.user.service.UserService;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.common.PaperType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * Created by aatrox on 2017/3/6.
 */
@RestController
@RequestMapping("/practice")
public class PracticeController {
    private Logger logger = LoggerFactory.getLogger(PracticeController.class);
    @Autowired
    private PracticeService practiceService;

    /**
     * 获取模考列表
     *
     * @param catgory    科目
     * @param area       地区
     * @param year       年份
     * @param name       名称
     * @param type       类型
     * @param onStatus   状态
     * @param createTime 创建时间
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(required = false) String catgory,
                       @RequestParam(required = false) String area,
                       @RequestParam(required = false) int year,
                       @RequestParam(required = false) String name,
                       @RequestParam(defaultValue = PaperType.CUSTOM_PAPER + "") int type,
                       @RequestParam(required = false) int onStatus,
                       @RequestParam(required = false) long createTime,
                       HttpServletRequest request) throws BizException {
        int userId = UserService.getUserId(request);
        List<PaperBean> paperList = practiceService.findAll(catgory, area, year, name, type, onStatus, createTime, userId);
        return paperList;
    }

    /**
     * 创建模拟试卷
     *
     * @param areas         多个地区，分割
     * @param practicePaper
     * @param request
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createPaper(@RequestParam String areas, @RequestBody PracticePaperBean practicePaper, HttpServletRequest request) throws BizException{
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            long uid = user.getId();
            practicePaper.setCreatedBy((int) uid);
            practicePaper.setCreateTime(new Date());
            //新建状态
            practicePaper.setStatus(BackendPaperStatus.CREATED);
            practiceService.createPaper(practicePaper, areas);
            return SuccessMessage.create("创建模拟试卷成功");
        }
        return ErrorResult.create(1110002, "创建模拟试卷失败");
    }

    /**
     * 删除试卷
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deletePaper(@RequestParam int id, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            long uid = user.getId();
            practiceService.deletePaper(id, uid);
            return SuccessMessage.create("删除模拟试卷成功");
        }
        return ErrorResult.create(1110002, "删除模拟试卷失败");
    }

    /**
     * 更新保存模拟试卷
     *
     * @param paperBean
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updatePaper(@RequestBody PracticePaperBean paperBean, HttpServletRequest request) throws BizException {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            long uid = user.getId();
            practiceService.updatePaper(uid, paperBean);
            return SuccessMessage.create("更新模拟试卷成功");
        }
        return ErrorResult.create(1110002, "更新模拟试卷失败");
    }

    /**
     * 查看参加该模考试卷人数
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object viewCountAnswer(@RequestParam int id) {
        return practiceService.viewCountAnswer(id);
    }

    /**
     * 根据试卷id,获取试卷详情
     *
     * @param id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaper(@PathVariable int id) throws BizException {
        return practiceService.findById(id);
    }

    /**
     * 根据试卷id,获取试卷中试题对象基本信息
     *
     * @param id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/questions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getQuestions(@RequestParam int id) throws BizException {
        return practiceService.getQuestionsByPid(id);
    }

    /**`
     * 根据知识点条件查找试题
     *
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/queryQuestionByKnowledge", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object queryQuestionByKnowledge(
            @RequestBody String paramsStr
    ) throws BizException {
        return practiceService.queryQuestionByKnowledge(paramsStr);
    }

    /**
     * 模拟试卷选题根据真题试卷
     *
     * @param paramsStr
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/queryQuestionByZhenTiPaper", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object queryQuestionByZhenTiPaper(
            @RequestBody String paramsStr
    ) throws BizException {
        return practiceService.queryQuestionByZhenTiPaper(paramsStr);
    }

    /**
     * 根据地区，年份，科目查找试卷
     *
     * @param area
     * @param year
     * @param catgory
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/queryPaper", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object queryPaper(
            @RequestParam int area,
            @RequestParam int year,
            @RequestParam int catgory
    ) throws BizException {
        return practiceService.queryPaper(area, year, catgory);
    }

    /**
     * 复制模拟试卷
     *
     * @param pid
     * @param pname
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/copyPaper", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object copyPaper(@RequestParam int pid, @RequestParam String pname, HttpServletRequest request) throws BizException {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            long uid = user.getId();
            return practiceService.copyPaper(pid, pname, uid);
        }
        return ErrorResult.create(1110002, "复制模拟试卷失败");
    }

    /**
     * 添加试题到模拟试卷
     *
     * @param pid
     * @param moduleId
     * @param qid
     * @param request
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/addQuestion2Paper", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object addQuestion2Paper(@RequestParam int pid, @RequestParam int moduleId, @RequestParam int qid, @RequestBody String moduleName, HttpServletRequest request) throws BizException {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            long uid = user.getId();
            practiceService.addQuestion2Paper(pid, moduleId, qid, moduleName);
            logger.info(String.format("用户:%s 添加试题:%s 到试卷:%s 模块:%s", uid, qid, pid, moduleId));
            return SuccessMessage.create("添加试题到模拟试卷成功");
        }
        return ErrorResult.create(1110002, "添加试题到模拟试卷失败");
    }

    /**
     * 从模拟试卷中撤出试题
     *
     * @param pid
     * @param moduleId
     * @param qid
     * @param request
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/deleteQuestion2Paper", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deleteQuestion2Paper(@RequestParam int pid, @RequestParam int moduleId, @RequestParam int qid, HttpServletRequest request) throws BizException {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            long uid = user.getId();
            practiceService.deleteQuestion2Paper(pid, moduleId, qid);
            logger.info(String.format("用户:%s 撤出试题:%s 到试卷:%s 模块:%s ", uid, qid, pid, moduleId));
            return SuccessMessage.create("模拟试卷撤出试题成功");
        }
        return ErrorResult.create(1110002, "模拟试卷撤出试题失败");
    }

    @RequestMapping(value = "/savePracticePaperSort", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object savePracticePaperSort(@RequestBody String practiceModuleBeansJson, @RequestParam int pid, HttpServletRequest request) throws BizException {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            long uid = user.getId();
            if (StringUtils.isNotEmpty(practiceModuleBeansJson)) {
                practiceService.savePracticePaperSort(practiceModuleBeansJson, pid);
                logger.info(String.format("用户:%s 调整模拟试卷题序", uid));
                return SuccessMessage.create("调整模拟试卷题序成功");
            }
            return ErrorResult.create(1110002, "参数无效");
        }
        return ErrorResult.create(1110002, "调整模拟试卷题序失败");
    }

}
