package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 班级信息表
 **/
@Repository
public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long>,JpaSpecificationExecutor<ClassInfo> {

    @Query("select n from ClassInfo n where n.startTime < ?1 and n.endTime > ?1")
    List<ClassInfo> findByTime(Date date);

    List<ClassInfo> findByStatus(int status);

    @Query("select n from ClassInfo n where n.areaId = ?1 and n.status = 1 ")
    List<ClassInfo> findByAreaId(Long areaId);


    @Query("select distinct(n.areaId) from ClassInfo n where n.status = 1 ")
    List<Long> findAreaList();

//    //自定义返回类型
//    @Query(value = "select new com.huatu.tiku.essay.entity.vo.admin.PaperAnswerStatisVO(max(eqa.examScore),min(eqa.examScore) ,avg(eqa.examScore) ,count(eqa)) from EssayPaperAnswer eqa where eqa.paperBaseId=?1 and eqa.status=1 and eqa.bizStatus=3 ")
//    List<AreaClassVO> findStatisData(long paperBaseId);
}
