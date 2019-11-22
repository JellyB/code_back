package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EssayCommentTemplateRepository extends JpaRepository<CommentTemplate, Long>, JpaSpecificationExecutor<CommentTemplate> {

    List<CommentTemplate> findByIdIn(List<Long> ids);

    List<CommentTemplate> findByIdInAndStatus(List<Long> ids, int status);

}
