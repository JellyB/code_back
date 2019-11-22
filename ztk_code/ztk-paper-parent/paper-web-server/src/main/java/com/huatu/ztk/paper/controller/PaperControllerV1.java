package com.huatu.ztk.paper.controller;

import com.google.common.collect.Lists;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.CatgoryType;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperSummary;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.service.PaperAnswerCardService;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.paper.util.PersonalityAreaUtil;
import com.huatu.ztk.user.service.UserSessionService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 试卷控制层
 * Created by shaojieyue
 * Created time 2016-04-23 15:01
 */

@RestController
@RequestMapping(value = "/v1/papers")
public class PaperControllerV1 extends BaseController<Paper> {
    private static final Logger logger = LoggerFactory.getLogger(PaperControllerV1.class);
    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperAnswerCardService answerCardService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private SubjectDubboService subjectDubboService;


    /**
     * 根据id查询试卷
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object get(@PathVariable int id) {
        final Paper paper = paperService.findById(id);
        if (paper == null) {
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        return paper;
    }

    /**
     * 试卷信息汇总
     * 如：北京 25套
     * @param type 试卷类型 @PaperType
     * @return
     */
    @RequestMapping(value = "summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object summary(//默认真题
                          @RequestParam(required = false,defaultValue = PaperType.TRUE_PAPER+"")  int type,
                          @RequestHeader(required = false) String token) throws BizException{
        userSessionService.assertSession(token);
        //取得用户ID
        final int area = userSessionService.getArea(token);
        final int category = userSessionService.getCatgory(token);
        List<PaperSummary> paperSummaries = paperService.summary(Lists.newArrayList(category),area,type);
        paperSummaries.forEach(i-> PersonalityAreaUtil.changeName(category,i));
        return paperSummaries;
    }

    /**
     * 试卷信息汇总（小程序使用）
     * @param type
     * @return
     */
    @RequestMapping(value = "summary/smallRoutine", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object summary(@RequestParam(required = false,defaultValue = PaperType.TRUE_PAPER+"") int type){
        List<PaperSummary> paperSummaries = paperService.summary(Lists.newArrayList(CatgoryType.GONG_WU_YUAN), AreaConstants.QUAN_GUO_ID,type);
        return paperSummaries;
    }

    /**
     * 根据给定条件查询试卷列表
     *
     * @param token     用户的token
     * @param page      页码
     * @param area    地区id
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaperList(@RequestHeader(required = false) String token,
                               @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "-1") int area,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "-1") int year,//年份
                               @RequestParam(defaultValue = CatgoryType.GONG_WU_YUAN+"") int catgory,//科目
                               //默认真题
                               @RequestParam(required = false,defaultValue = PaperType.TRUE_PAPER+"") int paperType ) throws BizException{

        if (StringUtils.isBlank(token)) {//如果不存在token,说明无需加载个人信息
            return paperService.findForPage(catgory, area,year, paperType, page, size);
        }

        userSessionService.assertSession(token);
        //取得用户ID
        final long uid = userSessionService.getUid(token);

        //用户设置的subject
        int subject = userSessionService.getSubject(token);

        //金融特殊处理
        subject = subjectDubboService.getBankSubject(subject);

        List<Integer> subjects = Lists.newArrayList(subject);

        //如果用户设置的subject是事业单位行测,采用公务员行测和事业单位行测的真题试卷
        if (SubjectType.SYDW_XINGCE == subject) {
            subjects.add(SubjectType.GWY_XINGCE);
        }
        int newSubject = userSessionService.getNewSubject(token);
        if(subject!=newSubject){
            subjects.add(newSubject);
        }
        final PageBean<Paper> pageBean = paperService.findForPage(subjects, area,year, paperType, page, size,uid);
        return pageBean;
    }



    /**
     * 废弃
     * @param token
     * @param terminal
     * @param cursor
     * @param pageSize
     * @param cardType
     * @param cardTime
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaperHistory(@RequestHeader(required = false) String token, @RequestHeader int terminal,
                                  @RequestParam(defaultValue = Long.MAX_VALUE+"") int cursor,
                                  @RequestParam int pageSize,
                                  @RequestParam int cardType,
                                  @RequestParam String cardTime) throws BizException{

        userSessionService.assertSession(token);
        //取得用户ID
        long userId = userSessionService.getUid(token);

        if (cursor < 0) cursor = 0;

        return answerCardService.getAnswerCards(userId, cursor, pageSize, cardType, cardTime);
    }

    /**
     * 模考估分列表
     * @param token
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "/estimateList", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaper(@RequestHeader(required = false) String token,
                           @RequestParam(defaultValue = "1", required = false) int page,
                           @RequestParam(defaultValue = "20", required = false) int size) throws BizException{
        userSessionService.assertSession(token);

        page = page < 1 ? 1 : page;
        long userId = userSessionService.getUid(token);

        int subject = userSessionService.getSubject(token);

        //金融特殊处理
        subject = subjectDubboService.getBankSubject(subject);

        final int area = userSessionService.getArea(token);
      //  logger.info("huang userId:"+userId+"subject:"+subject);
        List<EstimatePaper> papers = paperService.getEstimatePapers(subject,area, page, size, userId);
        return papers;
    }

}
