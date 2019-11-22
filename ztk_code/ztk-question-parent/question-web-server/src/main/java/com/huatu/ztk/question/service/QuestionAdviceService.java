package com.huatu.ztk.question.service;

import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionAdvice;
import com.huatu.ztk.question.bean.QuestionExtend;
import com.huatu.ztk.question.dao.QuestionAdviceDao;
import com.huatu.ztk.question.daoPandora.mapper.QuestionAdviceMapper;
import com.huatu.ztk.question.daoPandora.mapper.detail.QuestionAdviceDetail;
import com.huatu.ztk.user.bean.UserDto;
import com.huatu.ztk.user.dubbo.UserDubboService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户纠错提交代码
 * Created by linkang on 7/22/16.
 */
@Service
public class QuestionAdviceService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionAdviceService.class);

    @Autowired
    private QuestionAdviceDao questionAdviceDao;

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private QuestionExtendService questionExtendService;

    @Autowired
    private SubjectDubboService subjectDubboService;

    @Autowired
    private QuestionAdviceDetail questionAdviceDetail;

    @Autowired
    private UserDubboService userDubboService;


    /**
     * 用户纠错提交
     *
     * @param questionAdvice
     * @throws BizException
     */
    public void create(QuestionAdvice questionAdvice) throws BizException {
        String content = questionAdvice.getContent();

        //反馈内容为空，qid=0
        if (StringUtils.isBlank(content) || questionAdvice.getQid() == 0) {
            throw new BizException(CommonErrors.INVALID_ARGUMENTS);
        }

        Question question = questionDubboService.findById(questionAdvice.getQid());
        final QuestionExtend questionExtend = questionExtendService.findById(questionAdvice.getQid());
        if (question == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        if (!(question instanceof GenericQuestion)) {
            throw new BizException(ErrorResult.create(1000101, "该试类试题目前无法纠错"));
        }


        int catgory = subjectDubboService.getCatgoryBySubject(question.getSubject());

        //过滤掉表情符号
        content = removeFourChar(content);
        questionAdvice.setContent(content);
        if (questionExtend != null) {
            questionAdvice.setModuleId(questionExtend.getModuleId());
        }
        //TODO JBZM : I will change 'QuestionAdvice' Entity if we need update.
        questionAdvice.setQtype(question.getType());
        questionAdvice.setQuestionArea(question.getArea());
        questionAdvice.setCatgory(catgory);
        questionAdvice.setSubject(question.getSubject());

        if (questionAdvice.getErrorType() < 1) {//检查错误类型的合法性
            questionAdvice.setErrorType(4);
        }
        //get user nick name

        String contacts = StringUtils.trimToEmpty(questionAdvice.getContacts());
        questionAdvice.setContacts(contacts);
        com.huatu.ztk.question.daoPandora.entity.QuestionAdvice questionAdviceNew = new com.huatu.ztk.question.daoPandora.entity.QuestionAdvice();
        BeanUtils.copyProperties(questionAdvice, questionAdviceNew);
        questionAdviceNew.setSubject(questionAdvice.getSubject());
        questionAdviceNew.setQuestionType(questionAdvice.getQtype());
        questionAdviceNew.setQuestionId(questionAdvice.getQid());
        questionAdviceNew.setQuestionArea(questionAdvice.getQuestionArea());
        questionAdviceNew.setBlSubExam(questionAdvice.getCatgory());
        questionAdviceNew.setUserId(questionAdvice.getUid());
        //get user nickname
        UserDto byId = userDubboService.findById(questionAdvice.getUid());
        questionAdviceNew.setNickName(byId.getNick());
        questionAdviceNew.setUserArea(questionAdvice.getUserArea());
        questionAdviceNew.setUsername(byId.getName());
        questionAdviceDetail.insert(questionAdviceNew);
    }
    //questionAdviceDao.saveAdvice(questionAdvice);

    public static String removeFourChar(String content) {
        byte[] conbyte = content.getBytes();
        for (int i = 0; i < conbyte.length; i++) {
            if ((conbyte[i] & 0xF8) == 0xF0) {
                for (int j = 0; j < 4; j++) {
                    conbyte[i + j] = 0x30;
                }
                i += 3;
            }
        }
        content = new String(conbyte);
        return content.replaceAll("0000", "");
    }
}
