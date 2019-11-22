package com.huatu.ztk.backend.subject.service;

import com.google.common.collect.Lists;
import com.huatu.ztk.backend.subject.bean.*;
import com.huatu.ztk.backend.subject.dao.CatgoryDao;
import com.huatu.ztk.backend.subject.dao.SubjectDao;
import com.huatu.ztk.backend.subject.dao.SubjectTreeDao;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.commons.exception.ErrorResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by linkang on 17-5-16.
 */

@Service
public class SubjectTreeService {
    private static final Logger logger = LoggerFactory.getLogger(SubjectTreeService.class);
    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private CatgoryDao catgoryDao;


    @Autowired
    private SubjectTreeDao subjectTreeDao;

    private static final int HIDE_STATUS = 0;
    private static final int NOT_HIDE_STATUS = 1;

    public List<SubjectTreeBean> findTree(List<Integer> idList) {
        final List<SubjectTreeBean> newBeans = Collections.unmodifiableList(subjectTreeDao.findAll());

        List<SubjectBean> allSubjects = subjectDao.findAll();
        List<SubjectBean> allCatgorys = catgoryDao.findAll();

        for (SubjectTreeBean newBean : newBeans) {
            if (allCatgorys.stream().anyMatch(i -> i.getId() == newBean.getId())) {
                newBean.setType(SubjectTreeBeanType.CAT);
            } else if (allSubjects.stream().anyMatch(i -> i.getId() == newBean.getId())) {
                newBean.setType(SubjectTreeBeanType.SUB);
            } else {
                newBean.setType(SubjectTreeBeanType.MID);
            }
        }

        List<SubjectTreeBean> topBeans = null;
        if (CollectionUtils.isEmpty(idList)) {
            topBeans = newBeans.stream().filter(i -> i.getParent() == 0)
                    .collect(Collectors.toList());
        } else {
            topBeans = newBeans.stream()
                    .filter(i -> idList.contains(i.getId()))
                    .collect(Collectors.toList());
        }

        for (SubjectTreeBean topBean : topBeans) {
            fillChildrens(topBean, newBeans);
        }

        return topBeans;
    }


    /**
     * 递归
     * 填充子节点
     * @param bean
     * @param newBeans
     */
    private void fillChildrens(SubjectTreeBean bean, final List<SubjectTreeBean> newBeans) {
        List<SubjectTreeBean> childrens = findChildrens(bean.getId(), newBeans);

        bean.setChildrens(childrens);

        if (CollectionUtils.isEmpty(childrens)) {
            return;
        }

        for (SubjectTreeBean children : childrens) {
            fillChildrens(children, newBeans);
        }
    }


    /**
     * 查找子节点
     * @param parentId
     * @param newBeans
     * @return
     */
    private List<SubjectTreeBean> findChildrens(int parentId, final List<SubjectTreeBean> newBeans) {
        return newBeans.stream().filter(i -> i.getParent() == parentId).collect(Collectors.toList());
    }


    /**
     * 添加到科目树
     * @param subjectTreeBean
     */
    public void addSubjectTreeItem(SubjectTreeBean subjectTreeBean) throws BizException {
        if (subjectTreeBean.getId() > 0) {
            final List<SubjectTreeBean> newBeans = Collections.unmodifiableList(subjectTreeDao.findAll());
            SubjectTreeBean stb = null;
            for(SubjectTreeBean temp :newBeans){
                if(temp.getId()==subjectTreeBean.getId()){
                    stb = temp;
                }
            }
            if(stb!=null){
                if(stb.getStatus()== SubjectStatus.AVAILABLE){
                    throw new BizException(SubjectErrors.NODE_EXISTS);
                }else{
                    subjectTreeDao.setStatus(stb.getId(), SubjectStatus.AVAILABLE);
                }
            }else{
                subjectTreeDao.insertOld(subjectTreeBean);
            }
        } else {
            if (StringUtils.isBlank(subjectTreeBean.getName())) {
                throw new BizException(SubjectErrors.EMPTY_NAME);
            }

            subjectTreeDao.insertNew(subjectTreeBean);
        }
    }

    /**
     * 删除节点
     * @param id
     * @throws BizException
     */
    public void delSubjectTreeItem(int id) throws BizException{
        List<SubjectTreeBean> tree = findTree(null);

        if (tree.stream().anyMatch(i -> i.getId() == id && i.getType() == SubjectTreeBeanType.MID)) {
            throw new BizException(SubjectErrors.CANNOT_DEL);
        }

        subjectTreeDao.delete(id);
    }

    /**
     * 编辑节点
     * @param subjectTreeBean
     */
    public void editSubjectTreeItem(SubjectTreeBean subjectTreeBean) throws BizException{
        if (StringUtils.isBlank(subjectTreeBean.getName())) {
            throw new BizException(SubjectErrors.EMPTY_NAME);
        }

        subjectTreeDao.update(subjectTreeBean);
    }

    /**
     * 添加该考试类型下的所有学科
     * @param subjectTreeBean
     * @throws BizException
     */
    public void addAllSubjects(SubjectTreeBean subjectTreeBean) throws BizException{
        int catgoryId = subjectTreeBean.getId();

        if (catgoryDao.findAll().stream().noneMatch(i -> i.getId() == catgoryId)) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }
        final List<SubjectTreeBean> beans = Collections.unmodifiableList(subjectTreeDao.findAll());
        Set<Integer> oldSubjectIds = beans.stream()
                .filter(i -> i.getParent() == catgoryId)
                .map(i -> i.getId()).collect(Collectors.toSet());

        List<SubjectBean> subjects = subjectDao.findAll().stream()
                .filter(i -> i.getCatgory() == catgoryId)
                .filter(obj -> !oldSubjectIds.contains(obj.getId()))
                .collect(Collectors.toList());

        for (SubjectBean subject : subjects) {
            SubjectTreeBean newBean = SubjectTreeBean.builder()
                    .id(subject.getId())
                    .parent(catgoryId)
                    .name(subject.getName())
                    .build();
            subjectTreeDao.insertOld(newBean);
        }
    }


    /**
     * 删除该考试类型下的所有学科
     * @param catgoryId
     * @throws BizException
     */
    public void delAllSubjects(int catgoryId)  throws BizException{

        if (catgoryDao.findAll().stream().noneMatch(i -> i.getId() == catgoryId)) {
            throw new BizException(CommonErrors.RESOURCE_NOT_FOUND);
        }

        List<SubjectBean> subjects = subjectDao.findAll().stream()
                .filter(i -> i.getCatgory() == catgoryId)
                .collect(Collectors.toList());
        subjects.forEach(subject -> subjectTreeDao.delete(subject.getId()));
    }


    public void hide(int id) {
        subjectTreeDao.setStatus(id, HIDE_STATUS);
    }

    public void cancelHide(int id) {
        subjectTreeDao.setStatus(id, NOT_HIDE_STATUS);
    }

    public Object getStaticTree() {
        List<SubjectTreeBean> tree = findTree(null);


        //固定数据
        SubjectTreeBean gwyxc = SubjectTreeBean
                .builder()
                .id(1)
                .name("公务员行测")
                .type(2)
                .status(1)
                .parent(1)
                .childrens(Lists.newArrayList())
                .build();


        SubjectTreeBean gwy = SubjectTreeBean
                .builder()
                .id(1)
                .status(1)
                .parent(0)
                .name("公务员")
                .type(1)
                .childrens(Lists.newArrayList(gwyxc))
                .build();


        SubjectTreeBean sydwgj = SubjectTreeBean
                .builder()
                .id(2)
                .name("公基")
                .type(2)
                .status(1)
                .parent(3)
                .childrens(Lists.newArrayList())
                .build();

        SubjectTreeBean sydwzc = SubjectTreeBean
                .builder()
                .id(3)
                .name("职测")
                .type(2)
                .status(1)
                .parent(3)
                .childrens(Lists.newArrayList())
                .build();

        SubjectTreeBean zhyy = SubjectTreeBean
                .builder()
                .id(24)
                .status(1)
                .name("综合应用")
                .type(2)
                .parent(3)
                .status(1)
                .childrens(Lists.newArrayList())
                .build();

        SubjectTreeBean sydw = SubjectTreeBean
                .builder()
                .id(3)
                .name("事业单位")
                .type(1)
                .status(1)
                .parent(0)
                .childrens(Lists.newArrayList(sydwgj,sydwzc,zhyy))
                .build();


        tree.add(sydw);
        tree.add(gwy);

        tree.sort(Comparator.comparing(SubjectTreeBean::getId));
        return tree;
    }

}
