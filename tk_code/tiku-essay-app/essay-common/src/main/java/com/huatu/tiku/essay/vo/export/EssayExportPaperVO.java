package com.huatu.tiku.essay.vo.export;

import com.huatu.tiku.essay.entity.EssayMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 后台导出试卷相关VO
 * @date 2018/10/8下午1:25
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayExportPaperVO {

    /**
     * 试卷id
     */
    private Long paperId;
    private String paperName;
    /**
     * 材料列表
     */
    List<EssayMaterial> materialList;

    /**
     * 试题列表
     */
    List<EssayExportQuestionVO> questionList;




}
