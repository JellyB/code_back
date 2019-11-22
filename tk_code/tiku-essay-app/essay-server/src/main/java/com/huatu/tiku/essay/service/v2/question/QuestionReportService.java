package com.huatu.tiku.essay.service.v2.question;

import com.huatu.tiku.essay.vo.resp.correct.report.EssayQuestionCorrectReportVO;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/11
 * @描述 人工批改, 单题批改报告
 */

public interface QuestionReportService {

    EssayQuestionCorrectReportVO getQuestionReport(long answerId);

}
