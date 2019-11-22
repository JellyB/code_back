package com.huatu.ztk.pc.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 申论试卷
 * Created by ht on 2016/9/23.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class ShenlunPaper implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;//试卷id
    private String name;//试卷名称
    private int year;//年限
    private int area;//区域
    private int time;//答题时间
    private String restrict;//注意事项 tactics moudile 22
    private List<String> materials;//材料列表 tactics moudile 23
    private List<ShenlunQuestion> questions;//试题列表 tactics

}
