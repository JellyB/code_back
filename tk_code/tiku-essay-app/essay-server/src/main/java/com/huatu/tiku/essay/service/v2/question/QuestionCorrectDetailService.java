package com.huatu.tiku.essay.service.v2.question;

import com.huatu.tiku.essay.vo.resp.EssayQuestionVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkListVo;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/7/9
 * @描述 app 批改详情
 */

public interface QuestionCorrectDetailService {


    List<EssayQuestionVO> answerDetailV3(int id, int type, long answerId, int terminal, String cv);

    /**
     * 获取试题本题阅卷批注
     *
     * @param type
     * @param totalId
     * @return
     */
    RemarkListVo getQuestionRemarkListInfo(int type, long totalId, String elseRemark);

    /**
     * 套卷 综合评价
     *
     * @param type
     * @param totalId
     * @return
     */
    RemarkListVo getPaperRemarkList(int type, long totalId, String elseRemark);

}
