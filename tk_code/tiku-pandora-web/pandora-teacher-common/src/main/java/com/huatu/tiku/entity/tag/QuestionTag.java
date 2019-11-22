package com.huatu.tiku.entity.tag;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "base_question_tag")
public class QuestionTag extends BaseEntity {

    private Long questionId;

    private Long tagId;
}
