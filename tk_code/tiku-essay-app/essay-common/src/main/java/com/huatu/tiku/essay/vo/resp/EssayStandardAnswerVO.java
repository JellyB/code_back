package com.huatu.tiku.essay.vo.resp;

import com.huatu.tiku.essay.entity.EssayStandardAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 标准答案兼容多个落款人
 * @date 2018/12/103:41 PM
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EssayStandardAnswerVO extends EssayStandardAnswer {
    private List<String> inscribedNameList;
}
