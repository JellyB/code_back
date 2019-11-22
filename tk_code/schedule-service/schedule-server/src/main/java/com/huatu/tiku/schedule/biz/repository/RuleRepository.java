package com.huatu.tiku.schedule.biz.repository;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.biz.domain.Rule;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
public interface RuleRepository extends BaseRepository<Rule, Long> {

    @Query(value = "SELECT * from rule where ?1 BETWEEN date_begin_int and date_end_int or ?2 BETWEEN  date_begin_int and date_end_int or date_begin_int BETWEEN ?1  and ?2 ",nativeQuery = true)
    List<Rule> checkDate(Integer begin, Integer end);

    @Query(value = "SELECT * from rule where (?1 BETWEEN date_begin_int and date_end_int or ?2 BETWEEN  date_begin_int and date_end_int or date_begin_int BETWEEN ?1  and ?2 )and id  <> ?3 ",nativeQuery = true)
    List<Rule> checkDateExceptId(Integer begin, Integer end,Long id);

}
