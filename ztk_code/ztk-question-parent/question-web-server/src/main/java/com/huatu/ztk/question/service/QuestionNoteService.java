package com.huatu.ztk.question.service;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.question.bean.QuestionNote;
import com.huatu.ztk.question.common.ZtkQuestionErrors;
import com.huatu.ztk.question.dao.QuestionNoteDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by shaojieyue on 5/3/16.
 */

@Service
public class QuestionNoteService {

    @Autowired
    private QuestionNoteDao questionNoteDao;

    /**
     * 保存笔记
     */
    public void save(QuestionNote questionNote) throws BizException {
        String content = StringUtils.trimToNull(questionNote.getContent());
        if (content == null) {//内容为空不做处理
            return;
        }
        if (content.length() > 200) {
            throw new BizException(ZtkQuestionErrors.NOTE_TOO_LONG);
        }
        questionNote.setContent(content);
        questionNoteDao.save(questionNote);
    }


    public List<QuestionNote> findByQuestions(int userId, int[] questions) {
        List<QuestionNote> list = null;
        return list;
    }
}
