package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by x6 on 2017/12/27.
 * 模考相关 答题卡缓存数据
 *
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EssayMockExamAnswerVO {

    //试卷答题卡信息
    private EssayPaperAnswer essayPaperAnswer;

    //试题答题卡信息
    List<EssayQuestionAnswer> essayQuestionAnswerList;

}
