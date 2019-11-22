package com.huatu.ztk.paper.controller;

import com.huatu.common.SuccessMessage;
import com.huatu.ztk.paper.bean.PaperSyllabus;
import com.huatu.ztk.paper.dao.PaperSyllabusDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/2/20
 * @描述
 */
@RestController
@RequestMapping(value = "formativeTestPaper", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class FormativeTestPaperController {


    @Autowired
    PaperSyllabusDao paperSyllabusDao;

    /**
     * @param courseId             课程ID
     * @param syllabusId           课程大纲ID
     * @param startTimeIsEffective 考试时间是否起作用 0 否;1 是
     * @param paperId              活动卷ID
     * @return
     */
    @RequestMapping(value = "saveRelation", method = RequestMethod.POST)
    public Object saveRelation(@RequestParam int courseId,
                               @RequestParam int syllabusId,
                               @RequestParam(defaultValue = "0") int startTimeIsEffective,
                               @RequestParam(required = false) List<Long> paperId) {
        StringBuffer id = new StringBuffer();
        String resultId = id.append(courseId).append(syllabusId).toString();

        PaperSyllabus paperSyllabus = PaperSyllabus.builder()
                .id((Integer.valueOf(resultId)))
                .courseId(courseId)
                .syllabusId(syllabusId)
                .startTimeIsEffective(startTimeIsEffective)
                .paperId(paperId)
                .updateTIme(new Date())
                .build();

        paperSyllabusDao.save(paperSyllabus);
        return SuccessMessage.create("操作成功!");
    }


}
