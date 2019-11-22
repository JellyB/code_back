package com.huatu.ztk.backend.question.controller;

import com.huatu.ztk.backend.question.service.QuestionOperateService;
import com.huatu.ztk.backend.question.service.QuestionService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.question.exception.IllegalQuestionException;
import com.itextpdf.text.BadElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Author: xuhuiqiang
 * Time: 2016-12-22  20:33 .
 */
@RestController
@RequestMapping(value = "/question", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);
    @Autowired
    private QuestionService questionService;
    @Autowired
    private QuestionOperateService questionOperateService;


    /**
     * 判读题序是否已经被占用
     * @param paperId
     * @param sequence
     * @throws BizException
     */
    @RequestMapping(value = "common/judgeDuplication", method = RequestMethod.GET)
    public void judgeDuplication(@RequestParam int paperId,
                                 @RequestParam int sequence) throws BizException {
        questionService.judgeDuplication(paperId,sequence);
    }

    /**
     * 根据传输的questionId，返回试题（支持各种类型）
     * @param questionId
     * @return
     */
    @RequestMapping(value = "common/findById", method = RequestMethod.GET)
    public Object questionAllTypeById(@RequestParam int questionId){
        Object object= questionService.findAllTypeById(questionId);
        if (object == null) {//试题不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        logger.info("obj={}",object);
        return object;
    }

    /**
     * 获得知识点树
     * @return
     */
    @RequestMapping(value = "common/pointTree", method = RequestMethod.GET)
    public Object pointTreeBySubject(@RequestParam int subject){
        return questionService.findPointTreeBySubject(subject);
    }

    /**
     * 编辑题（支持各种类型）
     * @param request
     * @throws IOException
     * @throws BizException
     */
    @RequestMapping(value = "common/edit", method = RequestMethod.PUT)
    public void questionEditAllType(@RequestBody String str,HttpServletRequest request) throws IOException, BizException, BadElementException, IllegalQuestionException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        int uid = 2;
        if(user!=null){
            account = user.getAccount();
            uid =(int) user.getId();
        }
        questionService.directUpdateQuestion(str,account,uid);
    }


    /**
     * 根据条件获得试题列表
     * @param subject
     * @param module
     * @param area
     * @param year
     * @param mode
     * @param databaseQuestionType
     * @param stem
     * @param questionId
     * @return
     */
    @RequestMapping(value = "common/list", method = RequestMethod.GET)
    public Object questionListByDetail(@RequestParam int subject,
                                       @RequestParam int module,
                                       @RequestParam String area,
                                       @RequestParam int year,
                                       @RequestParam int mode,
                                       @RequestParam int databaseQuestionType,
                                       @RequestParam String stem,
                                       @RequestParam int questionId){
        return questionService.findByDetail(subject,module,area,year,mode,databaseQuestionType,stem,questionId);
    }


    /**
     * 根据传输的questionId，返回试题
     * @param questionId
     * @return
     */
    @RequestMapping(value = "xingce/findById", method = RequestMethod.GET)
    public Object questionById(@RequestParam int questionId){
        Object object= questionService.findById(questionId);
        if (object == null) {//试题不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        return object;
    }

    /**
     * 获得知识点树
     * @return
     */
    @RequestMapping(value = "xingce/pointTree", method = RequestMethod.GET)
    public Object pointTree(){
        return questionService.findPointTree();
    }


    /**
     * 根据输入试题ID，删除该试题，即把该试题的状态设置为已删除状态
     * @param questionId
     * @return
     */
    @RequestMapping(value = "xingce/delete", method = RequestMethod.DELETE)
    public void questionDelete(@RequestParam int questionId) throws IOException, BizException {
        questionService.deleteQuestion(questionId);
    }

    /**
     * 过滤null
     * @return
     */
    @RequestMapping(value = "common/filterNull", method = RequestMethod.GET)
    public void filterNull() throws IOException, BizException {
        questionOperateService.operatesTemp();
    }

    /**
     * 添加单一客观题
     * @param
     */
    @RequestMapping(value = "xingce/add", method = RequestMethod.POST)
    public void questionAdd(@RequestBody String str,HttpServletRequest request) throws IOException, BizException, BadElementException, IllegalQuestionException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        long uid = 2;
        if(user!=null){
            account = user.getAccount();
            uid = user.getId();
        }
        questionOperateService.addGenericObjectQuestion(str,account ,uid);
    }

    /**
     * 添加复合客观题
     * @param str
     */
    @RequestMapping(value = "xingce/addComposite", method = RequestMethod.POST)
    public void questionAddComposite(@RequestBody String str,HttpServletRequest request) throws IOException, BizException, BadElementException, IllegalQuestionException {
        long start = System.currentTimeMillis();
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        long userId = 2;
        if(user!=null){
            account = user.getAccount();
            userId = user.getId();
        }
        questionOperateService.addCompositeObjectiveQuestion(str,account ,userId);
        long end = System.currentTimeMillis();
        logger.info("后端总用时={}",end-start);
    }

    /**
     * 添加单一主观题
     * @param
     */
    @RequestMapping(value = "subjective/add", method = RequestMethod.POST)
    public void questionAddGenericSubjective(@RequestBody String str,HttpServletRequest request) throws IOException, BizException, BadElementException, IllegalQuestionException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        long uid = 2;
        if(user!=null){
            account = user.getAccount();
            uid = user.getId();
        }
        questionOperateService.addGenericSubjectiveQuestion(str,account ,uid);
    }

    /**
     * 添加复合主观题
     * @param str
     */
    @RequestMapping(value = "subjective/addComposite", method = RequestMethod.POST)
    public void questionAddCompositeSubjective(@RequestBody String str,HttpServletRequest request) throws IOException, BizException, BadElementException, IllegalQuestionException {
        long start = System.currentTimeMillis();
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        long userId = 2;
        if(user!=null){
            account = user.getAccount();
            userId = user.getId();
        }
        questionOperateService.addCompositeSubjectiveQuestion(str,account ,userId);
        long end = System.currentTimeMillis();
        logger.info("插入复合主观题后端总用时={}",end-start);
    }

    /**
     * 申请编辑题
     */
    @RequestMapping(value = "common/editApply", method = RequestMethod.PUT)
    public void questionEditApply(@RequestBody String str,HttpServletRequest request) throws IOException, BizException, BadElementException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        int id = -1;
        if(user!=null){
            account = user.getAccount();
            id = (int) user.getId();
        }
        questionService.editApplyQuestion(str,account,id);
    }

    @RequestMapping(value = "xingce/review", method = RequestMethod.PUT)
    public void editReview(@RequestParam int qid,@RequestParam int opType,@RequestParam(required = false) String reason, HttpServletRequest request) throws IOException, BizException, BadElementException, IllegalQuestionException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        if(user!=null){
            account = user.getAccount();
        }
        questionService.reviewQuestion(qid,opType,reason,account);
    }
    /**
     * 编辑题
     * @param
     */
    @RequestMapping(value = "xingce/edit", method = RequestMethod.PUT)
    public void questionEdit(@RequestBody String str,HttpServletRequest request) throws IOException, BizException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        if(user!=null){
            account = user.getAccount();
        }
        //questionServiceV1.editQuestion(str,account);
    }

    /**
     * 获取试题审核状态
     * @param subject
     * @param module
     * @param type
     * @param startTime
     * @param endTime
     * @param questionId
     * @return
     */
    @RequestMapping(value = "xingce/allEditLogList", method = RequestMethod.GET)
    public Object allEditLogList(@RequestParam(required = false,defaultValue = "-1") int subject,
                                 @RequestParam(required = false,defaultValue = "-1") int module,
                                 @RequestParam(required = false,defaultValue = "-1") int type,
                                 @RequestParam(required = false,defaultValue = "-1") long startTime,
                                 @RequestParam(required = false,defaultValue = "-1") long endTime,
                                 @RequestParam(required = false,defaultValue = "-1") int questionId,
                                 @RequestParam(required = false,defaultValue = "-1") int reviewFlag,
                                 @RequestParam(required = false,defaultValue = "-2")int status,
                                 HttpServletRequest request){
        long userId = -1;
        if(reviewFlag>0){
            final HttpSession session = request.getSession(true);
            User user = (User) session.getAttribute("user");
            if(user!=null){
                userId = user.getId();
            }else{
                userId = 0;
            }
        }
        return questionService.allEditLogList(subject,module,type,startTime,endTime,questionId,userId,status);
    }

    /**
     * 获取审核试题的详情
     * @param id
     * @return
     */
    @RequestMapping(value = "queryReviewById", method = RequestMethod.GET)
    public Object queryReviewById(@RequestParam int id) throws BizException{
        Object object= questionService.queryReviewById(id);
        if (object == null) {//试题不存在
            return ErrorResult.create(1000107, "解析数据错误");
        }
        return object;
    }

    @RequestMapping(value = "findQuestionById", method = RequestMethod.GET)
    public Object findQuestionById(@RequestParam int id) throws BizException{
        Object object= questionService.findQuestionById(id);
        if (object == null) {//试题不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        return object;
    }

    @RequestMapping(value = "showReviewInfo/{id}", method = RequestMethod.GET)
    public Object showReviewInfo(@PathVariable int id) throws BizException{
        Object object= questionService.showReviewInfo(id);
        if (object == null) {//试题不存在
            return CommonErrors.RESOURCE_NOT_FOUND;
        }
        return object;
    }
    @RequestMapping(value = "update/child", method = RequestMethod.GET)
    public Object updateChildBySubject(@RequestParam int subjectId) throws BizException{
        try {
            questionService.updateBySubject(subjectId);
        } catch (IllegalQuestionException e) {
            e.printStackTrace();
        }
        return ErrorResult.create(100000,"成功");
    }

    @RequestMapping(value = "test" ,method = RequestMethod.GET)
    public Object test(@RequestParam int id){
        questionService.updateStyleMain(id);
        return null;
    }

}
