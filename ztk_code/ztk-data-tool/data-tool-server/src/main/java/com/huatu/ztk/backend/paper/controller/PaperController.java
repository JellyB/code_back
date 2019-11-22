package com.huatu.ztk.backend.paper.controller;

import com.google.common.primitives.Ints;
import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.bean.PaperBean;
import com.huatu.ztk.backend.paper.bean.PaperCheck;
import com.huatu.ztk.backend.paper.bean.PracticeModuleBean;
import com.huatu.ztk.backend.paper.constant.EssayConstant;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.backend.paper.service.CreatePaperPdfService;
import com.huatu.ztk.backend.paper.service.ExportType;
import com.huatu.ztk.backend.paper.service.PaperService;
import com.huatu.ztk.backend.paper.service.PracticeService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.backend.user.service.UserService;
import com.huatu.ztk.backend.util.ShortUrlHelper;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by ht on 2016/12/21.
 */
@RestController
@RequestMapping("/paper")
public class PaperController {

    private static final Logger logger = LoggerFactory.getLogger(PaperController.class);

    @Autowired
    private PaperService paperService;

    @Autowired
    private CreatePaperPdfService createPaperPdfService;
    @Autowired
    private PracticeService practiceService;
    @Autowired
    private MatchDao matchDao;


    /**
     * 获取真题试卷
     *
     * @param catgory 科目id，用,分开
     * @param area
     * @param year
     * @param type
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@RequestParam(required = false) String catgory,
                       @RequestParam(required = false) String area,
                       @RequestParam(required = false) int year,
                       @RequestParam(required = false) String name,
                       @RequestParam(defaultValue = PaperType.TRUE_PAPER + "") int type,
                       HttpServletRequest request) throws BizException {
        int uid = UserService.getUserId(request);
        List<PaperBean> paperList = paperService.findAll(catgory, area, year, name, type, uid);
        return paperList;
    }

    /**
     * 获取地区
     *
     * @param depth 区域深度,1:省级别 2:市级别
     * @return
     */
    @RequestMapping(value = "/areas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getAreas(@RequestParam(defaultValue = "2") int depth) {
        return AreaConstants.getAreas(depth);
    }

    /**
     * 创建试卷
     *
     * @param paperBean
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void createPaper(@RequestBody PaperBean paperBean, HttpServletRequest request) throws BizException {
        int userId = UserService.getUserId(request);
        paperBean.setCreatedBy(userId);

        logger.info("create paper bean={}", paperBean);
        paperService.createPaper(paperBean);
    }

    /**
     * 获取试卷详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaper(@PathVariable int id) throws BizException {
        return paperService.findById(id);
    }

    /**
     * 更新保存试卷
     *
     * @param paperBean
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updatePaper(@RequestBody PaperBean paperBean) throws BizException {
        paperService.update(paperBean);
        return SuccessMessage.create("修改更新试卷成功");
    }

    /**
     * 删除试卷
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deletePaper(@RequestParam int id) {
        paperService.delete(id);
        return SuccessMessage.create("删除试卷成功");
    }


    /**
     * 更新审核/上线状态
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object updateAudit(@RequestParam int id, HttpServletRequest request) throws BizException {
        long uid = UserService.getUserId(request);
        String msg = paperService.updateAudit(id, uid);
        if(StringUtils.isBlank(msg)){
            return ErrorResult.create(1110002, "已经结束的考试不能修改上下线状态");
        }
        return SuccessMessage.create(msg);
    }

    @RequestMapping(value = "reset", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object resetPaper(@RequestParam int id) throws BizException {
        paperService.resetPaper(id);
        return SuccessMessage.create("试卷重置成功");
    }


    /**
     * 添加推荐
     */
    @RequestMapping(value = "recommend", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object addRecommend(@RequestParam int id) throws BizException {
        paperService.addRecommend(id);
        return SuccessMessage.create("添加推荐成功");
    }

    /**
     * 取消推荐
     */
    @RequestMapping(value = "recommend", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object delRecommend(@RequestParam int id) throws BizException {
        paperService.delRecommend(id);
        return SuccessMessage.create("取消推荐成功");
    }

    /**
     * 添加模块
     */
    @RequestMapping(value = "module", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object addModule(@RequestParam int id, @RequestParam int mid, @RequestParam String name) throws BizException {
        paperService.addModule(id, mid, name);
        return SuccessMessage.create("添加成功");
    }

    /**
     * 删除模块
     */
    @RequestMapping(value = "module", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object delModule(@RequestParam int id, @RequestParam int mid) throws BizException {
        paperService.delModule(id, mid);
        return SuccessMessage.create("删除成功");
    }

    /**
     * 根据条件获取试卷审核列表
     *
     * @param catgory
     * @param areas
     * @param year
     * @param name
     * @param type
     * @param id
     * @return
     */
    @RequestMapping(value = "/check", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object queryPaperList(
            @RequestParam(required = false) String catgory,
            @RequestParam(required = false) String areas,
            @RequestParam(required = false) int year,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) int id,
            HttpServletRequest request) throws BizException {
        int uid = UserService.getUserId(request);
        PaperBean paperBean = PaperBean.builder()
                .areas(areas)
                .id(id)
                .year(year)
                .name(name)
                .type(Ints.tryParse(type))
                .createdBy(uid)
                .build();
        return paperService.queryPaperList(paperBean, catgory);
    }

    /**
     * 审核操作
     *
     * @param id         审核试卷pid
     * @param type       3审核拒绝 6审核通过 其他非法参数
     * @param suggestion 审核意见
     * @param request
     * @return
     */
    @RequestMapping(value = "/check", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object check(@RequestParam int id, @RequestParam int type, @RequestBody String suggestion, HttpServletRequest request) throws BizException {
        HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        if (user != null) {
            long uid = user.getId();
            PaperCheck paperCheck = PaperCheck.builder()
                    .paperId(id)
                    .checkTime(new Date().getTime())
                    .checkId(uid)
                    .build();
            if (type == BackendPaperStatus.AUDIT_REJECT) {
                paperCheck.setCheckStatus(BackendPaperStatus.AUDIT_REJECT);
            } else if (type == BackendPaperStatus.AUDIT_SUCCESS) {
                paperCheck.setCheckStatus(BackendPaperStatus.ONLINE);
                //审核通过，变成审核状态，需要手动上线 2018-01-10 18:03:35 zw
                paperCheck.setCheckStatus(BackendPaperStatus.AUDIT_SUCCESS);
            } else {
                return ErrorResult.create(1110002, "审核操作非法参数");
            }
            String suggest = (String) JsonUtil.toMap(suggestion).get("suggestion");
            paperCheck.setSuggestion(suggest);
            paperService.check(paperCheck);
            return SuccessMessage.create("审核操作完成");
        }
        return ErrorResult.create(1110002, "审核操作失败");
    }


    /**
     * 审核意见
     *
     * @return
     */
    @RequestMapping(value = "/check/{pid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findPaperCheckInfo(@PathVariable int pid) {
        List<PaperCheck> list = paperService.getPaperCheckByPids4Newest(Arrays.asList(pid));
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 组卷链接
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "createPaperLink/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createPaperLink(@PathVariable int id, @RequestParam int type) {
        String shortUrl = "";
        if (PaperType.TRUE_PAPER == type) {
            shortUrl = ShortUrlHelper.getShortUrl(ShortUrlHelper.TRUEPAPER_URL + id);
        } else if (PaperType.ESTIMATE_PAPER == type) {
            shortUrl = ShortUrlHelper.getShortUrl(ShortUrlHelper.ESTIMATE_URL + id);
        } else {
            shortUrl = ShortUrlHelper.getShortUrl(ShortUrlHelper.TIMING_URL + id);

        }
        return SuccessMessage.create(shortUrl);
    }

    /**
     * 下载链接
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/downLoadLink/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object downLoadLink(@PathVariable int id) throws Exception {
        Paper paper = paperService.findPaperById(id);
        String downUrl = createPaperPdfService.downFileUrl(paper, ExportType.PAPER_PDF_TYPE_All);
        if (StringUtils.isEmpty(downUrl)) {
            return ErrorResult.create(1110002, "服务器错误");
        }
        return SuccessMessage.create(downUrl);
    }

    /**
     * 生成id试卷的下载文档
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createPaperFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createPaperFile(@RequestParam String id) throws Exception {
        paperService.createFile(id);
        return SuccessMessage.create("生成文件成功");
    }

    /**
     * 生成startId~endId之间所有试卷的下载文件
     * @param startId
     * @param endId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createPaperFile", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createPaperFile1(@RequestParam int startId,@RequestParam int endId) throws Exception {
        String id =paperService.findPaperIdByRange(startId,endId);
        paperService.createFile(id);
        return SuccessMessage.create("生成文件成功");
    }


    /**
     * 下载试卷
     *
     * @param name
     * @param fileUrl
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/downLoad", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object downLoadFile(@RequestParam String name, @RequestParam String fileUrl, HttpServletResponse response) throws Exception {
        createPaperPdfService.downLoadFile(name, fileUrl, response);
        return SuccessMessage.create("下载成功");
    }

    /**
     * 真题下载列表
     *
     * @param catgory 科目
     * @param area    地区
     * @param sYear   开始年份
     * @param eYear   结束年份
     * @return
     */
    @RequestMapping(value = "/allDownList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object allDownList(@RequestParam(required = false) String catgory,
                              @RequestParam(required = false) String area,
                              @RequestParam(required = false) int sYear,
                              @RequestParam(required = false) int eYear) {
        List<PaperBean> paperList = paperService.allDownList(catgory, area, sYear, eYear);
        return paperList;
    }

    /**
     * 一体板下载
     *
     * @param ids
     * @return
     */
    @RequestMapping(value = "/allPaperDownLoad", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object allPaperDownLoad(@RequestParam String ids, HttpServletResponse response) throws Exception {
        String sUrl = paperService.allPaperDownLoad(ids, response);
        if (StringUtils.isNotEmpty(sUrl)) {
            return SuccessMessage.create(sUrl);
        }
        return ErrorResult.create(1110002, "获取数据失败");
    }

    @RequestMapping(value = "/allPaperDownLoad/cassandra", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object allPaperDownLoad1(@RequestParam int  startId, @RequestParam int endId,HttpServletResponse response) throws Exception {
        String sUrl = paperService.allPaperDownLoad(paperService.findPaperIdByRange(startId,endId), response);
        if (StringUtils.isNotEmpty(sUrl)) {
            return SuccessMessage.create(sUrl);
        }

        return ErrorResult.create(1110002, "获取数据失败");
    }
    /**
     * 分开版下载
     *
     * @param ids
     * @return
     */
    @RequestMapping(value = "/sidePaperDownLoad", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object sidePaperDownLoad(@RequestParam String ids, HttpServletResponse response) throws Exception {
        String sUrl = paperService.sidePaperDownLoad(ids, response);
        if (StringUtils.isNotEmpty(sUrl)) {
            return SuccessMessage.create(sUrl);
        }
        return ErrorResult.create(1110002, "获取数据失败");
    }

    /**
     * 保存模块顺序
     *
     * @param moduleBeanList
     * @param id
     * @return
     */
    @RequestMapping(value = "saveModulesSort", method = RequestMethod.PUT)
    public Object saveModulesSort(@RequestBody List<PracticeModuleBean> moduleBeanList, @RequestParam int id) {
        paperService.saveModulesSort(moduleBeanList, id);
        return SuccessMessage.create("保存模块顺序成功");
    }
}
