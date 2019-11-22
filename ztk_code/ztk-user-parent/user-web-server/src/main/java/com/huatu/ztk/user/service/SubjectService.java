package com.huatu.ztk.user.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.ztk.user.dao.SubjectDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by linkang on 17-6-6.
 */
@Service
public class SubjectService {
    @Autowired
    private SubjectDao subjectDao;


    //缓存
    private static final Cache<Integer,String> subjectNameCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.DAYS)//缓存时间
                    .maximumSize(300)
                    .build();

    /**
     * 获得科目名称
     * @param subjectId
     * @return
     */
    public String getSubjectName(int subjectId) {
        String name = subjectNameCache.getIfPresent(subjectId);
        if (StringUtils.isBlank(name)) {
            name = subjectDao.findSubjectNameById(subjectId);
            subjectNameCache.put(subjectId,name);
        }

        return name;
    }

}
