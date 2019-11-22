package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.CommentTemplateDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 3:09 PM
 **/
public interface CommentTemplateDetailRepository extends JpaRepository<CommentTemplateDetail, Long>, JpaSpecificationExecutor<CommentTemplateDetail> {


    @Query("select d from CommentTemplateDetail  d where d.templateId = ?1 and d.status = ?2 order by d.sort asc")
    List<CommentTemplateDetail> findAllByTemplateIdAndStatusAndPid(long templateId, int status);


    @Query("select d from CommentTemplateDetail  d where d.templateId = ?1 and d.status = ?2 order by d.sort asc")
    List<CommentTemplateDetail> findAllByTemplateIdAndStatus(long templateId, int status);

    List<CommentTemplateDetail> findAllByPidAndStatus(long pid, int status);

    List<CommentTemplateDetail> findByStatusAndTemplateIdIn(int status, Set<Long> templateIds);
}
