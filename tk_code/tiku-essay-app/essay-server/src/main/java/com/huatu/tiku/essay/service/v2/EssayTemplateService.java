package com.huatu.tiku.essay.service.v2;

import com.huatu.common.exception.BizException;
import com.huatu.tiku.essay.dto.CommentTemplateDto;
import com.huatu.tiku.essay.vo.admin.CommentTemplateDetailVo;
import com.huatu.tiku.essay.vo.admin.correct.LabelCommentRelationVO;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkResultVo;
import com.huatu.tiku.essay.vo.resp.correct.report.RemarkVo;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 3:51 PM
 **/
public interface EssayTemplateService {

    Object findAllTemplateByType(int type) throws BizException;


    Object save(CommentTemplateDto dto) throws BizException;


    Object removeLogic(long templateId) throws BizException;

    List<CommentTemplateDetailVo> findTemplateByLabelTypeAndType(int type, int labelType);

}
