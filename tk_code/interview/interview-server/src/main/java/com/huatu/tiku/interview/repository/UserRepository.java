package com.huatu.tiku.interview.repository;

import com.huatu.tiku.interview.entity.po.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 学员管理
 * @author zhouwei
 * @Description: TODO
 * @create 2018-01-05 下午4:29
 **/
@Repository
public interface UserRepository extends JpaRepository<User, Long>,JpaSpecificationExecutor<User> {
    User getUserByOpenIdAndStatus(String openId,int status);
    User findByOpenId(String openId);

    List<User> findByStatus(int status);

    List<User> findByOpenIdAndStatus(String openId, int status);

    @Query(value = "select * from t_user INNER JOIN t_user_class uc ON t_user.open_id=uc.open_id where uc.class_id=?1 and uc.status = 1 and t_user.status = 1",nativeQuery = true)
    List<User> findByClassIdMy(Long classId);

    List<User> findByPhoneAndStatus(String phone,Integer status);

    List<User> findByStatusAndBizStatus(int status, Integer bizSatus);
}
