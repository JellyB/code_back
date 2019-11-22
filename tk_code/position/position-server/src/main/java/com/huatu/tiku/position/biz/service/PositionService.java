package com.huatu.tiku.position.biz.service;

import com.huatu.tiku.position.base.service.BaseService;
import com.huatu.tiku.position.biz.domain.Position;
import com.huatu.tiku.position.biz.domain.ScoreLine;
import com.huatu.tiku.position.biz.domain.User;
import com.huatu.tiku.position.biz.dto.PositionInfoDto;
import com.huatu.tiku.position.biz.enums.*;
import com.huatu.tiku.position.biz.vo.PageVo;
import com.huatu.tiku.position.biz.vo.PositionVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author wangjian
 **/
public interface PositionService extends BaseService<Position, Long> {

    Page<Position> findPosition(PositionType type, List<Long> areas, Education education, Degree degree,
                                Political political, Exp exp, BaseExp baseExp, Sex sex, Integer year, PositionStatus status, Pageable page,
                                String search,
                                Integer searchType, Nature nature);

    List<Position> findPosition(PositionType type, List<Long> areas, Education education, Degree degree, Political political,
                                Exp exp, BaseExp baseExp, Sex sex, Integer year, PositionStatus status, Nature nature);

    //计算推荐等级
    PositionInfoDto getRecommendationRank(Position position, User user);

    Page<Position> findEnrollPositions(Pageable page, Long userId);

    Page<Position> findCollectionPositions(Pageable page, Long userId);

    List<ScoreLine> findByPositionId(Long id);

    Integer findCount();

    void renderDynamicData(PageVo<PositionVo> positionPage);

    PageVo<PositionVo> findPositionForCache(PositionType type, List<Long> areas, Education education, Degree degree,
                                            Political political, Exp exp, BaseExp baseExp, Sex sex,
                                            Integer year, PositionStatus status, Pageable page, String search,
                                            Integer searchType, Nature nature);

    void purgePositionCache();
}
