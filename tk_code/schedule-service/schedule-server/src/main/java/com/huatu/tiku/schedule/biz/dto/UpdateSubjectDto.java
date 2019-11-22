package com.huatu.tiku.schedule.biz.dto;//package com.huatu.tiku.schedule.biz.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**接收更改科目参数
 * @author wangjian
 **/
@Getter
@Setter
@ToString
public class UpdateSubjectDto implements Serializable {

    private static final long serialVersionUID = 6027565191390947929L;

    @NotNull(message = "courseLiveTeacherId不能为空")
    private Long courseLiveTeacherId;

    /**
     * 模块ID
     */
    private Long subjectId;
}
