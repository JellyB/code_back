package com.huatu.tiku.interview.entity.vo.request;

import com.huatu.tiku.interview.entity.po.ChoiceInfo;
import com.huatu.tiku.interview.entity.po.PaperInfo;
import com.huatu.tiku.interview.entity.po.QuestionInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 试卷的所有信息
 * Created by junli on 2018/4/11.
 */
@NoArgsConstructor
@Data
public class PaperAllInfoVo extends PaperInfo{
    //试题列表
    private List<QuestionInfoAllVo> questionInfoAllVoList = new ArrayList<>();

    @NoArgsConstructor
    @Data
    public static class QuestionInfoAllVo extends QuestionInfo{
        //选项列表
        private List<ChoiceInfo> choiceInfoList = new ArrayList<>();
    }
}
