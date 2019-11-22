package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wangjian
 **/
public interface UserRepository extends BaseRepository<User, Long> {

    @Query(value = "SELECT COUNT(user_id) from user_areas  where area_id = ?1 ", nativeQuery = true)
    Integer findCountByArea(Long areaId);

    @Query(value = "SELECT count(DISTINCT ua.user_id) FROM user_areas ua LEFT JOIN area a ON a.id = ua.area_id WHERE a.parent_id= ?1 ", nativeQuery = true)
    Integer findCountByParentId(Long areaId);

    @Query(value = "SELECT count(DISTINCT ua.user_id) FROM user_areas ua LEFT JOIN area a ON a.id = ua.area_id WHERE a.parent_id in ?1 ", nativeQuery = true)
    Integer findCountByParentIds(List<Long> ids);

    @Query(value = "SELECT COUNT(DISTINCT ua.user_id) from user_areas ua LEFT JOIN user u on u.id=ua.user_id LEFT JOIN specialty s on u.specialty_id=s.id  where ua.area_id = ?1 and \n" +
            "s.parent_id=?2 and u.education=?3", nativeQuery = true)
    Integer getCountByAreaUserInfo(Long id, Long parentSpecialtyId, Integer education);

    @Query(value = "SELECT COUNT(DISTINCT ua.user_id) from user_areas ua LEFT JOIN user u on u.id=ua.user_id LEFT JOIN specialty s on u.specialty_id=s.id LEFT JOIN area a ON a.id=ua.area_id where a.parent_id = ?1 and \n" +
            "s.parent_id=?2 and u.education=?3", nativeQuery = true)
    Integer getCountByParentIdUserInfo(Long id, Long parentSpecialtyId, Integer education);

    @Query(value = "SELECT COUNT(DISTINCT ua.user_id) from user_areas ua LEFT JOIN user u on u.id=ua.user_id LEFT JOIN specialty s on u.specialty_id=s.id LEFT JOIN area a ON a.id=ua.area_id where a.parent_id in ?1 and \n" +
            "s.parent_id=?2 and u.education=?3", nativeQuery = true)
    Integer getCountByParentIdsUserInfo(List<Long> ids, Long parentSpecialtyId, Integer education);

    User findByOpenId(String openId);

    @Transactional
    @Modifying
    @Query(value = "update User set unionId =?1 where openId=?2")
    Integer updateUnionidByOpenId(String unionId,String openId);
}
