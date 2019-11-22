package com.huatu.tiku.request.question.v1;

import com.huatu.tiku.request.material.MaterialReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by huangqp on 2018\5\10 0010.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompositeQuestionReqV1 extends UpdateQuestionReqV1 {
    /**
     * 总括要求
     */
    private String omnibusRequirements;
    /**
     * 材料部分
     */
    @NotNull(message = "材料不能为空")
    @NotEmpty(message = "材料不能为空")
    private List<MaterialReq> materials;
}
