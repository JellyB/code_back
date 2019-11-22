package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayPhotoAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionBelongPaperArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 拍照答题相关
 */
public interface EssayPhotoAnswerRepository extends JpaRepository<EssayPhotoAnswer, Long> , JpaSpecificationExecutor<EssayPhotoAnswer> {


}
