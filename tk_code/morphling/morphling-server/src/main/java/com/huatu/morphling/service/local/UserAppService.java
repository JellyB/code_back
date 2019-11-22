package com.huatu.morphling.service.local;

import com.huatu.morphling.dao.jpa.api.UserAppDao;
import com.huatu.morphling.dao.jpa.api.UserDao;
import com.huatu.morphling.dao.jpa.entity.User;
import com.huatu.morphling.dao.jpa.entity.UserApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hanchao
 * @date 2017/12/5 18:29
 */
@Service
public class UserAppService extends BaseService<UserApp,Integer>{
    @Autowired
    private UserAppDao userAppDao;
    @Autowired
    private UserDao userDao;
    @Override
    public JpaRepository<UserApp, Integer> getDefaultDao() {
        return userAppDao;
    }

    public List<User> findByApp(int appId){
        return userDao.findByAppId(appId);
    }
}
