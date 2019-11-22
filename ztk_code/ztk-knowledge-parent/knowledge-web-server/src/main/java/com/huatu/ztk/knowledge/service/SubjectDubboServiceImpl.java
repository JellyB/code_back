package com.huatu.ztk.knowledge.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.knowledge.api.SubjectDubboService;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.common.SubjectTreeConfig;
import com.huatu.ztk.knowledge.servicePandora.SubjectService;

/**
 * Created by linkang on 17-4-18.
 */

@Service
public class SubjectDubboServiceImpl implements SubjectDubboService {
    private static final Logger logger = LoggerFactory.getLogger(SubjectDubboServiceImpl.class);

    //缓存
    private static final Cache<Integer, Integer> subjectCatgoryCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.DAYS)//缓存时间
                    .maximumSize(100)
                    .build();
    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private SubjectTreeConfig subjectTreeConfig;

    //银行招聘考试类型id
    private static final int YIN_HANG_ZHAO_PIN = 21;
    //农信社考试类型id
    private static final int NONG_XIN_SHE = 22;
    //中国银行科目id
    private static final int ZHONG_GUO_YIN_HANG = 100100126;

    @Override
    public int getCatgoryBySubject(int subject) {
        DebugCacheUtil.showCacheContent(subjectCatgoryCache, "subjectCatgoryCache");
        Integer catgory = subjectCatgoryCache.getIfPresent(subject);
        if (catgory == null) {
            catgory = subjectService.getCategoryBySubjectId(subject);
            subjectCatgoryCache.put(subject, catgory);
        }
        return catgory;
    }

    @Override
    public int getBankSubject(int subject) {
        if(subject == SubjectType.GWY_XINGCE_SHENLUN){
            return SubjectType.GWY_XINGCE;
        }
        //考试类型
        int catgory = getCatgoryBySubject(subject);
        return catgory == YIN_HANG_ZHAO_PIN || catgory == NONG_XIN_SHE ?
                ZHONG_GUO_YIN_HANG : subject;
    }

	@Override
	public String getCategoryNameById(int categoryId) {
		List<SubjectTree> treeList = JsonUtil.toList(subjectTreeConfig.getSubectJson(), SubjectTree.class);
		String categoryName = "公务员";
		if (CollectionUtils.isNotEmpty(treeList)) {
			Optional<SubjectTree> categoryInfo = treeList.stream().filter(category -> category.getId() == categoryId)
					.findFirst();
			if (categoryInfo.isPresent()) {
				return categoryInfo.get().getName();
			}
		}
		return categoryName;
	}

    @Override
    public List<SubjectTree> getSubjectTree() {
        List<SubjectTree> treeList = JsonUtil.toList(subjectTreeConfig.getSubectJson(), SubjectTree.class);
        return treeList;
    }
}
