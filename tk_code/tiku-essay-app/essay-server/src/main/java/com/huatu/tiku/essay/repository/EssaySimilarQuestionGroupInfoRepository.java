package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.EssaySimilarQuestionGroupInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by x6 on 2017/11/30.
 */
public interface EssaySimilarQuestionGroupInfoRepository extends JpaRepository<EssaySimilarQuestionGroupInfo, Long>, JpaSpecificationExecutor<EssaySimilarQuestionGroupInfo> {

    List<EssaySimilarQuestionGroupInfo> findByBizStatusAndStatusAndType(int bizStatus, int status, int type, Pageable pageRequest);

    List<EssaySimilarQuestionGroupInfo> findByBizStatusAndStatusAndTypeIn(int bizStatus, int status, List<Integer> typeList, Pageable pageRequest);

    long countByBizStatusAndStatusAndTypeIn(int bizStatus, int status, List<Integer> typeList);

    List<EssaySimilarQuestionGroupInfo> findByStatus(int status);


    List<EssaySimilarQuestionGroupInfo>  findByBizStatusAndStatus(int bizStatus, int status);
}
