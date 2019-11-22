package com.huatu.tiku.essay.vo.admin.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huangqingpeng
 * @title: QuestionLabelDetailSimpleVO
 * @description: 详细批注主题信息
 * @date 2019-07-0914:25
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class QuestionLabelDetailSimpleVO {

    private long id;

    private Long imageId;

    private String imageAxis;

    private String imageAllAxis;

    private String labeledContent;

    private String content;

}
