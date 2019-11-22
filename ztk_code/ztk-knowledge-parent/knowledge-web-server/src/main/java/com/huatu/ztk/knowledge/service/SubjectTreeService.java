package com.huatu.ztk.knowledge.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.servicePandora.SubjectService;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 科目树service
 * Created by linkang on 17-5-15.
 */

@Service
public class SubjectTreeService {

    @Autowired
    private SubjectService subjectService;
    //缓存
    Cache<Integer, SubjectTree> SUBJECT_TREE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    /**
     * 组装科目树
     *
     * @param idList
     * @return
     * @throws BizException
     */
    public List<SubjectTree> findTree(List<Integer> idList) throws BizException {
        DebugCacheUtil.showCacheContent(SUBJECT_TREE_CACHE, "SUBJECT_TREE_CACHE");
        ImmutableMap<Integer, SubjectTree> allPresent = SUBJECT_TREE_CACHE.getAllPresent(idList);
        List<SubjectTree> subjectTrees = new ArrayList<>();
        for (Integer sid : idList) {
            SubjectTree subjectTree = allPresent.get(sid);
            if (subjectTree == null) {
                SubjectTree tree = subjectService.findById(sid);
                if (tree == null) {
                    continue;
                }
                fillChildren(tree);
                SUBJECT_TREE_CACHE.put(sid, tree);
                subjectTrees.add(tree);
            } else {
                subjectTrees.add(subjectTree);
            }
        }
        return subjectTrees;
    }

    /**
     * 填充子节点
     *
     * @param tree
     */
    private void fillChildren(SubjectTree tree) {
        List<SubjectTree> childrenList = subjectService.findChildren(tree);
        tree.setChildrens(childrenList);
        if (CollectionUtils.isEmpty(childrenList)) {
            return;
        }
        for (SubjectTree children : childrenList) {
            fillChildren(children);
        }
    }

    public static List<SubjectTree> findStaticTree() {
        return JsonUtil.toList("[{\"id\":1,\"name\":\"公务员\",\"childrens\":[{\"id\":1,\"name\":\"行测\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":3,\"name\":\"事业单位\",\"childrens\":[{\"id\":2,\"name\":\"公基\",\"childrens\":[],\"tiku\":false},{\"id\":3,\"name\":\"职测\",\"childrens\":[],\"tiku\":false},{\"id\":24,\"name\":\"综合应用\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100047,\"name\":\"公检法\",\"childrens\":[{\"id\":100100175,\"name\":\"公安招警\",\"childrens\":[],\"tiku\":false}],\"tiku\":true},{\"id\":200100045,\"name\":\"教师\",\"childrens\":[{\"id\":400,\"name\":\"教师\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100000,\"name\":\"医疗\",\"childrens\":[{\"id\":410,\"name\":\"医疗\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100002,\"name\":\"金融\",\"childrens\":[{\"id\":420,\"name\":\"金融\",\"childrens\":[],\"tiku\":false}],\"tiku\":false},{\"id\":200100046,\"name\":\"其他\",\"childrens\":[{\"id\":430,\"name\":\"其他\",\"childrens\":[],\"tiku\":false}],\"tiku\":false}]", SubjectTree.class);
    }
}
