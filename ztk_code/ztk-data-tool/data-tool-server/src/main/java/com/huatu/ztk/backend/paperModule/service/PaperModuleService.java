package com.huatu.ztk.backend.paperModule.service;

import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paperModule.bean.PaperModuleBean;
import com.huatu.ztk.backend.paperModule.bean.PaperModuleErrors;
import com.huatu.ztk.backend.paperModule.dao.PaperModuleDao;
import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.dao.SubjectDao;
import com.huatu.ztk.backend.subject.service.SubjectService;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by ht on 2016/12/21.
 */
@Repository
public class PaperModuleService {

    private static final Logger logger = LoggerFactory.getLogger(PaperModuleService.class);


    @Autowired
    private PaperModuleDao paperModuleDao;


    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private SubjectService subjectService;


    /**
     * 列表
     * @return
     */
    public List<PaperModuleBean> findAll() {
        return paperModuleDao.findAll();
    }


    /**
     * 列表
     * @return
     */
    public List<PaperModuleBean> findList(int subjectId, long userId) {
        List<PaperModuleBean> all = findAll();

        //用户可以操作的考试科目
        List<SubjectBean> userSubjectList = subjectService.findList(0, userId);

        List<Integer> userSubjectIds = userSubjectList.stream().map(SubjectBean::getId).collect(Collectors.toList());

        //移除不能操作的考试科目
        all.removeIf(i -> !userSubjectIds.contains(i.getSubject()));

        if (subjectId > 0) {
            all.removeIf(i -> i.getSubject() != subjectId);
        }
        return all;
    }


    /**
     * 通过id
     * @param id
     * @return
     * @throws BizException
     */
    public PaperModuleBean findById(int id) throws BizException {
        if(id==0){
            return PaperModuleBean.builder().name("").build();
        }
        List<PaperModuleBean> list = findAll();

        PaperModuleBean bean = list.stream().filter(i -> i.getId() == id).findAny().orElseGet(null);

        if (bean == null) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        return bean;
    }


    /**
     * 删除
     * @param id
     * @return
     * @throws BizException
     */
    public void delete(int id) throws BizException {
        paperModuleDao.delete(id);
        subjectService.updateCacheMap();
    }


    /**
     * 更新
     * @param PaperModuleBean
     */
    public void update(PaperModuleBean PaperModuleBean) {
        paperModuleDao.update(PaperModuleBean);
        subjectService.updateCacheMap();
    }

    /**
     * 新增
     * @param bean
     */
    public void insert(PaperModuleBean bean) throws BizException{
        bean.setName(StringUtils.trimToEmpty(bean.getName()));

        //相同科目下不允许重复名称的试卷模块
        if (findAll().stream()
                .filter(i->i.getSubject() == bean.getSubject())
                .anyMatch(i -> i.getName().equals(bean.getName()))) {
            throw new BizException(PaperModuleErrors.PAPER_MODULE_EXISTS);
        }

        paperModuleDao.insert(bean);
        subjectService.updateCacheMap();
    }


    /**
     * 科目->(模块id，模块名称)map
     * @return
     */
    public Map getSubjectModuleMap() {
        List<PaperModuleBean> beans = paperModuleDao.findAvailableAll();

        List<SubjectBean> subjects = subjectDao.findAll();

        //科目->(模块id，模块名称)map
        Map<Integer, Map<Integer, String>> resultMap = Maps.newHashMap();

        for (SubjectBean subject : subjects) {

            Map<Integer, String> map = beans.stream()
                    .filter(b -> b.getSubject() == subject.getId())
                    .collect(Collectors.toMap(b -> b.getId(), b -> b.getName()));

            resultMap.put(subject.getId(), map);
        }

        return resultMap;
    }

    /**
     * module id 名称 map
     * @return
     */
    public Map getModuleNameMap() {
        List<PaperModuleBean> beans =  paperModuleDao.findAll();
        return beans.stream().collect(Collectors.toMap(b -> b.getId(), b -> b.getName()));
    }
}
