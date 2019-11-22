package com.huatu.tiku.essay.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2019/9/25
 * @描述
 */
@Builder
@AllArgsConstructor
@Data
@NoArgsConstructor
public class AdminExportKeyWordAndPhraseVO {

    //带描述的关键词
    private List<AdminQuestionKeyWordWithDescVO> adminQuestionKeyWordWithDescVO;

    //不带描述的关键词
    private List<AdminQuestionKeyWordVO> adminQuestionKeyWordVO;

    //带描述的关键句
    private List<AdminQuestionKeyPhraseWithDescVO> KeySentencesWithDescVoList;

    //主题句
    private AdminQuestionTopicVO topic;
    //中心论点
    private List<AdminQuestionKeyPhraseVO> argumentList;


}
