package com.huatu.ztk.paper.controller;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.huatu.ztk.commons.PageBean;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.bean.PaperSummary;
import com.huatu.ztk.paper.common.PaperType;
import com.huatu.ztk.paper.service.PaperService;
import com.huatu.ztk.paper.util.PersonalityAreaUtil;
import com.huatu.ztk.user.service.UserSessionService;
import com.huatu.ztk.user.util.UserTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 试卷v2
 */
@RestController
@RequestMapping(value = "/v2/papers")
public class PaperControllerV2 extends BaseController<Paper> {

    @Autowired
    private PaperService paperService;

    @Autowired
    private UserSessionService userSessionService;

    private static final Logger logger = LoggerFactory.getLogger(PaperControllerV2.class);


    /**
     * 试卷信息汇总
     * 如：北京 25套
     *
     * @param type 试卷类型 @PaperType
     * @return
     */
    @RequestMapping(value = "summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object summary(//默认真题
                          @RequestParam(required = false, defaultValue = PaperType.TRUE_PAPER + "") int type,
                          @RequestHeader(required = false) String token,
                          @RequestParam(defaultValue = "-1") int subject) throws BizException {
        userSessionService.assertSession(token);
        //取得用户ID
        final int area = userSessionService.getArea(token);

        if (subject < 0) {
            subject = userSessionService.getSubject(token);
        }
        List<Integer> subjects = Lists.newArrayList(subject);

        //如果用户设置的subject是事业单位行测,采用公务员行测和事业单位行测的真题试卷
        if (SubjectType.SYDW_XINGCE == subject) {
            subjects.add(SubjectType.GWY_XINGCE);
        }

        List<PaperSummary> paperSummaries = paperService.summaryNew(subjects, area, type);
        Integer subjectId = Optional.of(subjects).filter(i -> i.contains(1)).isPresent()?1:subjects.get(0);
        paperSummaries.forEach(i-> PersonalityAreaUtil.changeName(subjectId,i));
        return paperSummaries;
    }


    /**
     * 根据给定条件查询试卷列表v2
     * 与v1不同的是subject参数
     *
     * @param token
     * @param page      页码
     * @param area
     * @param size
     * @param year
     * @param subject   科目id,默认公务员行测
     * @param paperType 试卷类型,默认真题
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaperList(@RequestHeader(required = false) String token,
                               @RequestHeader(required = false) String cv,
                               @RequestHeader(required = false) int terminal,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "-1") int area,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "-1") int year,
                               @RequestParam(defaultValue = "-1") int subject,
                               @RequestParam(defaultValue = PaperType.TRUE_PAPER + "") int paperType) throws BizException {
        try {
            userSessionService.assertSession(token);
        } catch (BizException e) {
            if (subject > 0 && terminal == TerminalType.PC && paperType == PaperType.TRUE_PAPER) {
                final PageBean<Paper> pageBean = paperService.findForPageNoUser(Lists.newArrayList(subject), area, year, paperType, page, size);
                return pageBean;
            } else {
                throw e;
            }
        }
        //取得用户ID
        final long uid = userSessionService.getUid(token);

        int userSubject = userSessionService.getSubject(token);
        if (subject < 0) {
            subject = userSubject;
        }
        List<Integer> subjects = Lists.newArrayList(subject);
        int newSubject = userSessionService.getNewSubject(token);
        if (subject != newSubject) {
            subjects.add(newSubject);
        }
        //如果用户设置的subject是事业单位行测,采用公务员行测和事业单位行测的真题试卷
        if (SubjectType.SYDW_XINGCE == subject) {
            subjects.add(SubjectType.GWY_XINGCE);
        }
        /**
         * update by lijun 2018-05-23 矫正ios在6.1.3之前 subjectId bug 此处会被真题演练使用
         */
        logger.info("paper/list userInfo => userSubject = {},paramSubject = {},版本对比 {}", userSubject, subject, cv.compareTo("6.1.3"));
//        if (terminal == 2 && cv.compareTo("6.1.3") <= 0 && userSubject == 2 && subject != 2){//苹果、6.1.3 之前、用户属于事业单位、不是真题演练模块
//            ArrayList<Integer> subjectList = Lists.newArrayList(24);
//            final PageBean<Paper> pageBean = paperService.findForPage(subjectList, area, year, paperType, page, size, uid);
//            return pageBean;
//        }
        /**end*/
        final PageBean<Paper> pageBean = paperService.findForPage(subjects, area, year, paperType, page, size, uid);
        return pageBean;
    }

    /**
     * 模考估分列表
     *
     * @param token
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(value = "/estimateList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object getPaper(@RequestHeader(required = false) String token,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(defaultValue = "2,8,9") String types,
                           @RequestParam(defaultValue = "-1") int subjectId,
                           @RequestParam(defaultValue = "-1") int terminale,
                           @RequestHeader(defaultValue = "1") int terminal,
                           @RequestHeader(defaultValue = "-1") int subject
    ) throws BizException {

        List<Integer> typeList = Lists.newLinkedList();
        for (String str : types.split(",")) {
            Integer id = Ints.tryParse(str);
            if (id == null) {//id列表转换错误
                return CommonErrors.INVALID_ARGUMENTS;
            }
            typeList.add(id);
        }

        if (StringUtils.isNotEmpty(token)) {

            /**
             * update by lijun 2018-06-01 处理PC端没有用户登录的情况
             */

            try {
                userSessionService.assertSession(token);
            } catch (BizException e) {
                if (terminale == TerminalType.PC && subjectId != -1) {
                    List<EstimatePaper> newEstimatePapers = paperService.getNewEstimatePapers(subjectId, page, size, typeList);
                    return newEstimatePapers;
                } else {
                    throw e;
                }
            }
            page = page < 1 ? 1 : page;
            long userId = userSessionService.getUid(token);
            //事业单位,将（ABC非联考科目,转化为科目为职测）
            int transSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subjectId, subject);
            final int finalSubject = userSessionService.convertChildSubjectToParentSubject(transSubject);
            logger.info("估分列表,最终科目是:{}", finalSubject);
            List<EstimatePaper> papers = paperService.getNewEstimatePapers(finalSubject, page, size, typeList, userId, terminal);
            return papers;
        } else {
            /*if (subject < 0) {
                subject = SubjectType.GWY_XINGCE;
            }*/
            int transSubject = UserTokenUtil.getHeaderSubject(token, userSessionService::getSubject, subjectId, subject, terminal);
            final int finalSubject = userSessionService.convertChildSubjectToParentSubject(transSubject);
            return paperService.getNewEstimatePapers(finalSubject, page, size, typeList, -1, terminal);
        }
    }


    /**
     * 清除缓存数据  返回当前
     *
     * @throws BizException
     */
    @RequestMapping(value = "/clearEstimatePaperCache/{subjectId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object clearEstimatePaperCache(@PathVariable("subjectId") String subjectId) throws BizException {
        List<String> removeKeys = paperService.clearEstimatePaperCache(subjectId);
        return removeKeys;
    }
}
