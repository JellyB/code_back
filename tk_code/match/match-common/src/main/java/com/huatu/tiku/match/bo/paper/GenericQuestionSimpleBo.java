package com.huatu.tiku.match.bo.paper;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lijun on 2019/1/2
 */
@Data
@NoArgsConstructor
public class GenericQuestionSimpleBo extends QuestionSimpleBo {

    /**
     * 选项
     */
    private List<String> choiceList;

    @Builder
    public GenericQuestionSimpleBo(Integer id, Integer parentId, Integer type, String typeName, List<String> materialList, String stem, String moduleName, List<String> choiceList) {
        super(id, parentId, type, typeName, materialList, stem, moduleName);
        this.choiceList = choiceList;
    }
}
