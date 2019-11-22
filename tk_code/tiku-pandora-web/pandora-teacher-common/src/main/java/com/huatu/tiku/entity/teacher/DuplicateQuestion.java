package com.huatu.tiku.entity.teacher;

import com.huatu.common.bean.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * @创建人 lizhenjuan
 * @创建时间 2018/9/10
 * @描述
 */
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Table(name = "duplicate_question")
public class DuplicateQuestion  extends BaseEntity {

    private Long id;
    private Integer paperId;
    private String questionId;
    @Transient
    private List<Integer> questionIds;

}
