package com.huatu.tiku.essay.service.paper;

import com.huatu.tiku.essay.constant.status.EssayLabelStatusConstant;
import com.huatu.tiku.essay.entity.correct.EssayPaperLabelTotal;
import com.huatu.tiku.essay.essayEnum.CommonOperateEnum;
import com.huatu.tiku.essay.essayEnum.LabelFlagEnum;
import com.huatu.tiku.essay.vo.admin.correct.EssayPaperLabelTotalVo;

public interface EssayPaperLabelService {
    EssayPaperLabelTotal findByPaperAnswerId(Long paperAnswerId,LabelFlagEnum labelFlagEnum);

    /**
     * 查询--套卷阅卷批注
     *
     * @param paperAnswerCardId
     * @param labelFlagEnum
     * @return
     */
    EssayPaperLabelTotalVo getPaperLabelMark(long paperAnswerCardId, LabelFlagEnum labelFlagEnum);

    void save(EssayPaperLabelTotalVo paperLabelTotalVo,LabelFlagEnum labelFlagEnum);

    void delete(long labelId);

    Object getMainLabelInfo(long paperAnswerCardId, CommonOperateEnum commonOperateEnum, LabelFlagEnum labelFlagEnum);

    /**
     * 套卷完成批注操作
     * @param labelId
     */
    void labelFinish(long labelId);

    void updateLabelStatus(EssayPaperLabelTotal paperLabelTotal, EssayLabelStatusConstant.EssayLabelBizStatusEnum finish);

}
