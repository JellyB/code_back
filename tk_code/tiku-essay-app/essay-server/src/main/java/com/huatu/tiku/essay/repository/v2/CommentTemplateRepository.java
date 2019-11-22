package com.huatu.tiku.essay.repository.v2;

import com.huatu.tiku.essay.entity.correct.CommentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-06-28 3:08 PM
 **/
public interface CommentTemplateRepository extends JpaRepository<CommentTemplate, Long>, JpaSpecificationExecutor<CommentTemplate> {

    /**
     * 根据type & labelType 查询该题型下所有的模板
     * @param type
     * @param status
     * @param labelType
     * @return
     */
    @Transactional
    @Query("select c from CommentTemplate c where c.type =?1 and c.status = ?2 and c.labelType = ?3 order by c.sort asc")
    List<CommentTemplate> findAllByTypeAndStatusAndLabelType(int type, int status, int labelType);









}
