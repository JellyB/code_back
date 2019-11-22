package com.huatu.tiku.essay.vo.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * Created by huangqp on 2017\11\21 0021.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EssayMaterialVO implements Serializable{

    //材料id
    private Long id;
    //材料内容
    private String content;
    //材料序号
    private Integer sort;
    //试卷id
    private Long paperId;

    //标志位(是否和题目关联)
    private boolean flag;


}
