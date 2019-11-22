package com.huatu.tiku.essay.vo.admin.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author huangqingpeng
 * @title: QuestionLabelDetailInfoVO
 * @description: TODO
 * @date 2019-07-1315:26
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class QuestionLabelDetailInfoVO {
    private List<QuestionLabelDetailSimpleVO> labelDetails;

    private String labelContent;
}
