package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayQuestionBelongPaperArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by x6 on 2017/11/24.
 */
public interface EssayAreaRepository extends JpaRepository<EssayQuestionBelongPaperArea, Long> , JpaSpecificationExecutor<EssayQuestionBelongPaperArea> {

    List<EssayQuestionBelongPaperArea> findByPIdAndBizStatusAndStatusOrderBySortAsc(long i, int bizStatus, int status);

    List<EssayQuestionBelongPaperArea> findByNameAndBizStatusAndStatus(String name, int bizStatus, int status);

    List<EssayQuestionBelongPaperArea> findByNameLikeAndBizStatusAndStatus(String name,String sub,Integer bizstatus,Integer status);

    /**
     * 不查询biz
     * @param i
     * @param status
     * @return
     */
    List<EssayQuestionBelongPaperArea> findByPIdAndStatusOrderBySortAsc(long i, int status);

    /**
     * 只过滤状态
     * @param status
     * @return
     */
    List<EssayQuestionBelongPaperArea> findByStatus( int status);


//    List<EssayQuestionBelongPaperArea> findByNameLike(String name);


    List<EssayQuestionBelongPaperArea> findByNameLikeAndPId(String province,Long pid);
}
