package com.huatu.ztk.backend.subject.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paperModule.service.PaperModuleService;
import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.bean.SubjectErrors;
import com.huatu.ztk.backend.subject.bean.SubjectStatus;
import com.huatu.ztk.backend.subject.dao.CatgoryDao;
import com.huatu.ztk.backend.subject.dao.SubjectDao;
import com.huatu.ztk.backend.system.bean.Catgory;
import com.huatu.ztk.backend.system.dao.RoleManageDao;
import com.huatu.ztk.backend.teachType.service.TeachTypeService;
import com.huatu.ztk.commons.exception.BizException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by ht on 2016/12/21.
 */
@Service
public class SubjectService {

    private static final Logger logger = LoggerFactory.getLogger(SubjectService.class);


    @Autowired
    private SubjectDao subjectDao;

    @Autowired
    private CatgoryDao catgoryDao;

    @Autowired
    private PaperModuleService paperModuleService;

    @Autowired
    private TeachTypeService teachTypeService;

    @Autowired
    private RoleManageDao roleManageDao;


    //考试类型/科目/试卷模块/教研题型map的缓存
    private static final Cache<String,Map> totalMapCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.DAYS)//缓存时间
                    .build();

    private static final String TOTAL_MAP_CACHE_KEY = "total_map_cache_key";


    /**
     * 查询用户可以操作的考试科目列表
     *
     * @param catgory 默认0,查询全部可以操作的考试类型
     * @param uid 管理用户id
     * @return
     */
    public List<SubjectBean> findList(int catgory, long uid) {
        List<SubjectBean> all = subjectDao.findAll();

        Map<Integer, String> userCatgories = getUserCatgories(uid);

        //移除用户不能操作的考试类型
        all.removeIf(i -> !userCatgories.keySet().contains(i.getCatgory()));

        if (catgory > 0) {
            all.removeIf(i -> i.getCatgory() != catgory);
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
        return subjectDao.findAll().stream()
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
        subjectDao.delete(id);
        updateCacheMap();
    }


    /**
     * 更新
     *
     * @param subjectBean
     */
    public void update(SubjectBean subjectBean) {
        subjectDao.update(subjectBean);
        updateCacheMap();
    }

    /**
     * 新增
     *
     * @param bean
     */
    public void insert(SubjectBean bean) throws BizException{
        bean.setName(StringUtils.trimToEmpty(bean.getName()));
        boolean match = subjectDao.findAll().stream()
                .filter(i->i.getCatgory() == bean.getCatgory())
                .anyMatch(i -> i.getName().equals(bean.getName()));

        if (match) {
            throw new BizException(SubjectErrors.SUBJECT_EXISTS);
        }

        subjectDao.insert(bean);

        updateCacheMap();
    }


    /**
     * 所有考试类型下的考试科目 id->(id,name)
     * @return
     */
    public Map getTotalMap() throws BizException{
        Map cacheMap = totalMapCache.getIfPresent(TOTAL_MAP_CACHE_KEY);

        if (MapUtils.isNotEmpty(cacheMap)) {
            return cacheMap;
        } else {
            return queryTotalMap();
        }
    }

    private Map queryTotalMap() {
        List<SubjectBean> catgorys = catgoryDao.findAll();
        List<SubjectBean> subjects = subjectDao.findAll();

        Map<Integer, Map<Integer, String>> totalCatgorySubjectMap = Maps.newHashMap();

        //catgory默认的subject map
        Map<Integer, Integer> defaultSubjectMap = Maps.newHashMap();

        for (SubjectBean catgory : catgorys) {

            Map<Integer, String> map = subjects.stream().filter(i -> i.getCatgory() == catgory.getId()
                    && i.getStatus() == SubjectStatus.AVAILABLE)
                    .collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));

            totalCatgorySubjectMap.put(catgory.getId(), map);


            List<Integer> tmpList = new ArrayList(map.keySet());

            defaultSubjectMap.put(catgory.getId(), tmpList.size() > 0 ? tmpList.get(0) : -1);
        }


        Map m = new HashMap();

        //考试类型id->(考试科目id->name) map
        m.put("total_catgory_subject_map", totalCatgorySubjectMap);

        Map<Integer, String> catgoryMap = catgorys.stream()
                .collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));
        //考试类型id->name map
        m.put("catgory_map", catgoryMap);

        Map<Integer, String> availableCatgoryMap = catgorys.stream()
                .filter(i->i.getStatus() == SubjectStatus.AVAILABLE)
                .collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));

        //可用的考试类型
        m.put("available_catgory_map", availableCatgoryMap);

        Map<Integer, String> subjectMap = subjects.stream()
                .collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));

        //考试科目id->name map
        m.put("subject_map", subjectMap);

        //考试类型id->默认考试科目id map
        m.put("default_subject_map", defaultSubjectMap);

        //考试科目id->(模块id，模块name) map
        m.put("subject_module_map", paperModuleService.getSubjectModuleMap());

        //模块id名称 map
        m.put("module_map", paperModuleService.getModuleNameMap());

        //教研题型
        m.put("subject_teach_type_map", teachTypeService.getTeachTypeMap());

        //放入缓存
        totalMapCache.put(TOTAL_MAP_CACHE_KEY, m);

        return m;
    }


    public void updateCacheMap() {
        queryTotalMap();
    }


    /**
     *获得用户可以操作的考试类型
     * @param uid
     * @return
     */
    public Map<Integer, String> getUserCatgories(long uid) {
        List<Catgory> catgories = roleManageDao.findAllCatgoryByUserId((int) uid).stream().filter(i->i.getStatus()==1).collect(Collectors.toList());

        Map<Integer, String> nameMap = Maps.newHashMap();
        catgories.stream()
                .forEach(i->nameMap.put(i.getId(),i.getName()));
        return nameMap;
    }

    public List<SubjectBean> findAll(int category){
        List<SubjectBean> subjectBeans = subjectDao.findAll();
        if(category>0){
            subjectBeans.removeIf(i->i.getCatgory()!=category);
        }
        return subjectBeans;
    }

}
