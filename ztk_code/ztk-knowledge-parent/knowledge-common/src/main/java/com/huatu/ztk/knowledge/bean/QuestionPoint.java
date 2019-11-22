package com.huatu.ztk.knowledge.bean;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**知识点
 * Created by shaojieyue
 * Created time 2016-05-06 18:26
 */

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class QuestionPoint  implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;//id
    private String name;//名称
    private int parent;//父节点
    private int qnumber;//试题数
    private int level;//节点所在层级
    private int status;//知识点状态
    private List<Integer> children;//子节点列表
}
