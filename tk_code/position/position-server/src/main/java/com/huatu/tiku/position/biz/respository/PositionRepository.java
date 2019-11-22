package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.Position;
import org.springframework.data.jpa.repository.Query;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author wangjian
 **/
public interface PositionRepository extends BaseRepository<Position,Long>{

//    @Query(nativeQuery = true,value = "SELECT DISTINCT p.id from position p,(SELECT * from position where id =?1 ) a where a.name=p.name and a.company=p.company and a.department_id=p.department_id")
//    List<BigInteger> scoreLine(Long id);

    Integer countByLastModifiedDateIsAfter(Date date);

    @Query(value = "select count(position) from Position as position where position.year = 2019 and position.nature = 0")
    Integer findCount();

    List<Position> findBySpecialtysIsNull();

    List<Position> findByCode(String code);

    List<Position> findByYear(Integer year);

    @Query(value = "SELECT * FROM\tposition p LEFT JOIN department d ON d.id = p.department_id WHERE p.company =?1 " +
            "AND p.name_str =?2 AND p.introduce =?3 AND p. YEAR =?4  AND p.specialty_string =?5  AND p.education =?6  AND p.CODE =?7 AND d.code =?8",nativeQuery = true)
    List<Position> findByCompanyAndNameStrAndIntroduceAndYearAndSpecialtyStringAndEducationAndCode(String company,String nameStr,String introduce,Integer year,
                                                                                            String specialtyStrings,String education,String code,String department);
    @Query(value = "SELECT * FROM position p INNER JOIN department d ON d.id = p.department_id WHERE p.company =?1 " +
            "AND p.name_str =?2 AND p.year =?3 AND p.code =?4 And p.number=?5 AND d.code =?6",nativeQuery = true)
    List<Position> findByCompanyAndNameStrAndYearAndCodeAndNumberAndDepartmentId(String company, String name, int year, String code,
                                                                       Integer integer, Long departmentCode);
}
