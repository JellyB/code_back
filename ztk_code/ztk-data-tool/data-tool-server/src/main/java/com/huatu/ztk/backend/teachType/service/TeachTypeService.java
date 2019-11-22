package com.huatu.ztk.backend.teachType.service;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.dao.SubjectDao;
import com.huatu.ztk.backend.subject.service.SubjectService;
import com.huatu.ztk.backend.system.bean.Catgory;
import com.huatu.ztk.backend.teachType.bean.TeachTypeErrors;
import com.huatu.ztk.backend.teachType.bean.TeachTypeBean;
import com.huatu.ztk.backend.teachType.bean.TeachTypeStatus;
import com.huatu.ztk.backend.teachType.dao.TeachTypeDao;
import com.huatu.ztk.commons.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ht on 2016/12/21.
 */
@Service
public class TeachTypeService {

    private static final Logger logger = LoggerFactory.getLogger(TeachTypeService.class);



    @Autowired
    private TeachTypeDao teachTypeDao;

    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SubjectService subjectService;


    /**
     * 列表
     *
     * @return
     */
    public List<TeachTypeBean> findList(int subject,long uid) {
        List<TeachTypeBean> all = teachTypeDao.findAll();

        //用户可以操作的考试科目
        List<SubjectBean> userSubjectList = subjectService.findList(0, uid);

        List<Integer> userSubjectIds = userSubjectList.stream().map(SubjectBean::getId).collect(Collectors.toList());

        //移除不能操作的考试科目
        all.removeIf(i -> !userSubjectIds.contains(i.getSubject()));

        if (subject > 0) {
            all.removeIf(i -> i.getSubject() != subject);
        }
        return all;
    }


    /**
     * 通过id
     *
     * @param id
     * @return
     * @throws BizException
     */
    public Object findById(int id) throws BizException {
        return teachTypeDao.findAll().stream()
                .filter(i -> i.getId() == id)
                .findAny()
                .orElseGet(null);
    }


    /**
     * 删除
     *
     * @param id
     * @return
     * @throws BizException
     */
    public void delete(int id) throws BizException {
        teachTypeDao.delete(id);
        subjectService.updateCacheMap();
    }


    /**
     * 更新
     *
     * @param subjectBean
     */
    public void update(TeachTypeBean subjectBean) {
        teachTypeDao.update(subjectBean);
        subjectService.updateCacheMap();
    }

    /**
     * 新增
     *
     * @param bean
     */
    public void insert(TeachTypeBean bean) throws BizException{

        boolean match = teachTypeDao.findAll().stream()
                .filter(i->i.getSubject() == bean.getSubject())
                .anyMatch(i -> i.getName().equals(bean.getName()));

        if (match) {
            throw new BizException(TeachTypeErrors.TEACH_TYPE_EXISTS);
        }

        teachTypeDao.insert(bean);
        subjectService.updateCacheMap();
    }

    /**
     * 科目->(教研题型id，教研题型名称)map
     * @return
     */
    public Map getTeachTypeMap() {
        List<TeachTypeBean> beans = teachTypeDao.findAll();

        List<SubjectBean> subjects = subjectDao.findAll();

        Map<Integer, Map<Integer, String>> resultMap = Maps.newHashMap();

        for (SubjectBean subject : subjects) {

            Map<Integer, String> map = beans.stream()
                    .filter(b -> b.getSubject() == subject.getId() && b.getStatus() == TeachTypeStatus.AVAILABLE)
                    .collect(Collectors.toMap(b -> b.getId(), b -> b.getName()));

            resultMap.put(subject.getId(), map);
        }

        return resultMap;
    }
}
