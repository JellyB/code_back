package com.huatu.tiku.teacher.mongotest;

import com.huatu.ztk.question.bean.GenericQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserServiceImplT implements GenericQuestionSevice {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<GenericQuestion> paginationQuery(Integer pageNum) {
        SpringbootPageable pageable = new SpringbootPageable();
        PageModel pm = new PageModel();
        Query query = new Query();
        // 开始页
        pm.setPagenumber(pageNum);
        // 每页条数
        pm.setPagesize(5);
        // 排序
        pageable.setPage(pm);
        // 查询出一共的条数
        Long count = mongoTemplate.count(query, GenericQuestion.class);
        // 查询
        List<GenericQuestion> list = mongoTemplate.find(query.with(pageable), GenericQuestion.class);
        // 将集合与分页结果封装
        Page<GenericQuestion> pagelist = new PageImpl<>(list, pageable, count);
        return pagelist;
    }
}
