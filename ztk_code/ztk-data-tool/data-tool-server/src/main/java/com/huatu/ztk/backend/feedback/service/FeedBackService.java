package com.huatu.ztk.backend.feedback.service;

import com.huatu.ztk.backend.arena.dao.ArenaDao;
import com.huatu.ztk.backend.feedback.dao.FeedbackDao;
import com.huatu.ztk.backend.feedback.feedback.Feedback;
import com.huatu.ztk.user.bean.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by ht on 2016/12/1.
 */
@Service
public class FeedBackService {
    private static final Logger logger = LoggerFactory.getLogger(FeedBackService.class);

    @Autowired
    private FeedbackDao feedbackDao;

    @Autowired
    private ArenaDao arenaDao;

    /**
     * 反馈列表
     *
     * @param catgory 科目
     * @return
     */
    public List<Feedback> query(int catgory) {
        return feedbackDao.query(catgory);
    }

    /**
     * 获取反馈详情
     *
     * @param id
     * @return
     */
    public Feedback find(long id) {
        Feedback feedback = feedbackDao.find(id);
        UserDto userDto = arenaDao.findUserById(feedback.getUid());
        feedback.setUname(userDto.getName());
        return feedback;
    }

}
