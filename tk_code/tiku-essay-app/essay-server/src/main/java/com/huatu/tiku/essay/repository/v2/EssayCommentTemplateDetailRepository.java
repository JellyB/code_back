package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.CommentTemplateDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EssayCommentTemplateDetailRepository extends JpaRepository<CommentTemplateDetail, Long>, JpaSpecificationExecutor<CommentTemplateDetail> {

    List<CommentTemplateDetail> findByIdIn(List<Long> ids, int status);

    List<CommentTemplateDetail> findByIdInAndStatus(List<Long> ids, int status);





}
