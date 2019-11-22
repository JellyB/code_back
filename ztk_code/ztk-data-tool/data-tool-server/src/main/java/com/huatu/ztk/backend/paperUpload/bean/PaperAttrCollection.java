package com.huatu.ztk.backend.paperUpload.bean;

import com.huatu.ztk.backend.paperModule.bean.PaperModuleBean;
import com.huatu.ztk.backend.question.bean.QuestionPointTreeMin;
import com.huatu.ztk.backend.teachType.bean.TeachTypeBean;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lenovo on 2017/6/13.
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Builder
public class PaperAttrCollection {
    private List<QuestionPointTreeMin> pointList;
    private List<PaperModuleBean> moduleList;
    private List<TeachTypeBean> teachTypeList;
}
