package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.entity.EssayPaperBase;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import lombok.Data;

/**
 * @Author ZhenYang
 * @Date Created in 2018/2/2 16:44
 * @Description
 */
@Data
public class EssayCollectVO {
    private long id;
    private int userId;
    private EssayQuestionBase questionBase;
    private EssayQuestionDetail questionDetail;
    private EssayPaperBase paperBase;
}
