package com.huatu.tiku.interview.service;

import com.huatu.tiku.interview.entity.po.ModulePractice;
import com.huatu.tiku.interview.entity.po.PaperPractice;
import com.huatu.tiku.interview.entity.vo.request.PaperCommitVO;

import java.util.List;
import java.util.Map;

/**
 * @Author: ZhenYang
 * @Date: Created in 2018/1/11 15:13
 * @Modefied By:
 */
public interface LearningSituationService {

    Boolean savePaperPractice(PaperPractice data);

    boolean saveModulePractice(ModulePractice modulePractice);

    boolean saveMockPractice(PaperCommitVO vo);

    Object detail(String openId, String date);

    Map upMockList(Map<Object, List<Object>> map, String openId, String date);

//    int del(Long id);

//    List<PaperPractice> findList(String name,Pageable pageRequest);

//    long countByNameLikeStatus(String name);





}
