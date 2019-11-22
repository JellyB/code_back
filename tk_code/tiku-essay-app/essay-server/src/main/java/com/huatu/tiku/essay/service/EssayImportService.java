package com.huatu.tiku.essay.service;

import com.huatu.tiku.essay.vo.admin.AdminQuestionKeyRuleVO;
import org.springframework.web.multipart.MultipartFile;

public interface EssayImportService {

    AdminQuestionKeyRuleVO readQuestionRule(MultipartFile file,long questionDetailId);
}
