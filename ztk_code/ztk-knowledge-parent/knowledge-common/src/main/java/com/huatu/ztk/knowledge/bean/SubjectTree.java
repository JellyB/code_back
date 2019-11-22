package com.huatu.ztk.knowledge.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 科目树bean
 * Created by linkang on 17-5-15.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class SubjectTree  implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;         //科目id
    private String name;    //科目名称

    @Getter(onMethod = @__({ @JsonIgnore}))
    private int status; //状态

    @Getter(onMethod = @__({ @JsonIgnore}))
    private int parent; //父节点id

    private List<SubjectTree> childrens; //子节点列表

    private boolean tiku; //是否进入题库首页
}
