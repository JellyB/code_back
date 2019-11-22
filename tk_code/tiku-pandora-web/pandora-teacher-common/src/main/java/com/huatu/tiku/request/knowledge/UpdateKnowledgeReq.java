package com.huatu.tiku.request.knowledge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by x6 on 2018/5/8.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdateKnowledgeReq {
    /**
     * 知识点Id
     */
    @NotNull(message = "知识点ID不能为空")
    private Long id;
    /**
     * 知识点名称
     */
    @NotBlank(message = "知识点名称不能为空")
    private String name;
    /**
     * 上级知识点id
     */
    @NotNull(message = "上级知识点不能为空")
    private Long parentId;

    /**
     * 知识点所属科目
     */
    @NotNull(message = "知识点所属科目不能为空")
    @NotEmpty(message = "知识点所属科目不能为空")
    private List<Long> subject;

    /**
     * 知识点顺序
     */
    private Integer sortNum;


}
