package com.huatu.tiku.essay.vo.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author zhaoxi
 * @Description: 后台导出试题相关VO
 * @date 2018/10/8下午1:25
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EssayExportReqVO {


    /**
     * 文件类型（0:word  1:pdf ）
     *
     *
     */
    private int fileType;
    public static final int FILE_TYPE_WORD = 0;
    public static final int FILE_TYPE_PDF = 1;

    /**
     * 试卷id列表
     */
    private List<Long> paperIdList;

    /**
     * 导出文件包含内容（导出套题）
     */
    private int type;
    public static final int BASE_CONTENT = 0;
    public static final int BASE_CONTENT_WITH_ANSWER = 1;
    public static final int BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS = 2;
    //算法导出
    public static final int BASE_CONTENT_WITH_ANSWER_AND_ANALYSIS_ARITHMETIC = 3;

    /**
     * 答题卡id列表
     */
    private List<Long> answerIdList;
    /**
     * 导出文件包含内容（导出学员答案）
     */
    public static final int ANSWER_WITHOUT_CORRECTED = 0;
    public static final int ANSWER_WITH_CORRECTED = 1;
}
