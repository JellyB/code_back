package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayDetailAndSortVO {
    //答题要求
    private EssayQuestionDetail essayQuestionDetail;
    //题号
    private int sort;
}
