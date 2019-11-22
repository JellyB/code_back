package com.huatu.tiku.position.biz.respository;

import com.huatu.tiku.position.base.repository.BaseRepository;
import com.huatu.tiku.position.biz.domain.MsgUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author wangjian
 **/
public interface MsgRepository extends BaseRepository<MsgUser,Long>{

    @Query(value = "SELECT * from msg_user ORDER BY id DESC LIMIT 0,1",nativeQuery = true)
    MsgUser findlastOpenId();

    @Query(value = "select DISTINCT m.* from enroll e LEFT JOIN user u on u.id=e.user_id LEFT JOIN msg_user m on u.union_id= m.unionid",nativeQuery = true)
    List<MsgUser> getPositionMsgUser();

    Page<MsgUser> findBySubscribe(String subscribe,Pageable pageable);

//    List<MsgUser> findByUnionidIsNull(Pageable pageable);
}
