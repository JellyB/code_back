package com.huatu.ztk.backend.subject.service;

import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.bean.SubjectErrors;
import com.huatu.ztk.backend.subject.dao.CatgoryDao;
import com.huatu.ztk.commons.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by ht on 2016/12/21.
 */
@Service
public class CatgoryService {

    private static final Logger logger = LoggerFactory.getLogger(CatgoryService.class);


    @Autowired
    private CatgoryDao catgoryDao;

    @Autowired
    private SubjectService subjectService;

    /**
     * 列表
     * @return
     */
    public List<SubjectBean> findAll() {
        List<SubjectBean> catgoryBeanList = catgoryDao.findAll();
        return catgoryBeanList;
    }


    /**
     * 通过id
     * @param id
     * @return
     * @throws BizException
     */
    public Object findById(int id) throws BizException {
            return catgoryDao.findAll().stream()
                    .filter(i -> i.getId() == id)
                    .findAny()
                    .orElseGet(null);
    }


    /**
     * 删除
     * @param id
     * @return
     * @throws BizException
     */
    public void delete(int id) throws BizException {
        catgoryDao.delete(id);
        subjectService.updateCacheMap();
    }


    /**
     * 更新
     * @param subjectBean
     */
    public void update(SubjectBean subjectBean) {
        catgoryDao.update(subjectBean);
        subjectService.updateCacheMap();
    }

    /**
     * 新增
     * @param bean
     */
    public void insert(SubjectBean bean) throws BizException{
        bean.setName(StringUtils.trimToEmpty(bean.getName()));

        if (findAll().stream().anyMatch(i -> i.getName().equals(bean.getName()))) {
            throw new BizException(SubjectErrors.SUBJECT_EXISTS);
        }

        catgoryDao.insert(bean);
        subjectService.updateCacheMap();
    }
}
