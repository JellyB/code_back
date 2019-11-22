package com.huatu.ztk.knowledge.controller;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.common.consts.TerminalType;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.common.SubjectTreeConfig;
import com.huatu.ztk.knowledge.common.TeacherSubjectTreeConfig;
import com.huatu.ztk.knowledge.service.SubjectTreeService;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 科目树
 * Created by linkang on 17-5-11.
 */

@RestController
@RequestMapping(value = "/v2/subjects", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SubjectControllerV2 {

    @Autowired
    private SubjectTreeService subjectTreeService;
    @Autowired
    private SubjectTreeConfig subjectTreeConfig;
    @Autowired
    private TeacherSubjectTreeConfig teacherSubjectTreeConfig;

    @RequestMapping(value = "tree", method = RequestMethod.GET)
    public Object getSubjectTree(@RequestParam(required = false) String ids) throws BizException {
        List<Integer> idList = new ArrayList<>();
        if (StringUtils.isNoneBlank(ids)) {
            idList = Arrays.stream(ids.split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        }
        List<SubjectTree> trees = subjectTreeService.findTree(idList);
        return trees;
    }

    @CrossOrigin(origins = "*", allowedHeaders = "terminal")
    @RequestMapping(value = "tree/static", method = RequestMethod.GET)
    public Object getStaticSubjectTree(@RequestHeader(defaultValue = "1") int terminal) throws BizException {
        List<SubjectTree> treeList = JsonUtil.toList(subjectTreeConfig.getSubectJson(), SubjectTree.class);
        if (CollectionUtils.isNotEmpty(treeList)) {
            //招警考试类型
            List<SubjectTree> trees = treeList.stream().filter(subjectTree -> subjectTree.getId() == 200100047).
                    collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(trees)) {
                //PC端,招警多一个科目，行测（招警）
                if (TerminalType.PC == terminal) {
                    SubjectTree subjectTree = new SubjectTree();
                    subjectTree.setId(100100173);
                    subjectTree.setName("行测");
                    subjectTree.setChildrens(Lists.newArrayList());
                    List<SubjectTree> children = trees.get(0).getChildrens();
                    children.add(subjectTree);
                    List<SubjectTree> newChildren = children.stream().sorted(Comparator.comparing(SubjectTree::getId)).collect(Collectors.toList());
                    trees.get(0).setChildrens(newChildren);
//                    treeList.addAll(trees);
                }
            }
        }
        return treeList;
    }


    @CrossOrigin(origins = "*", allowedHeaders = "terminal")
    @RequestMapping(value = "subject/info", method = RequestMethod.GET)
    public Object getSubjectInfo(@RequestHeader(defaultValue = "1") int terminal, @RequestParam int subjectId) throws BizException {
        HashMap<Object, Object> map = Maps.newHashMap();
        List<SubjectTree> subjectTrees = (List<SubjectTree>) getStaticSubjectTree(terminal);
        for (SubjectTree subjectTree : subjectTrees) {
            List<SubjectTree> childrens = subjectTree.getChildrens();
            if (CollectionUtils.isNotEmpty(childrens)) {
                for (SubjectTree child : childrens) {
                    if (child.getId() == subjectId) {
                        map.put("category", subjectTree.getId());
                        map.put("categoryName", subjectTree.getName());
                        map.put("subjectId", child.getId());
                        map.put("subjectName", child.getName());
                        return map;
                    }
                }
            }
        }
        return map;
    }

    /**
     * 测试缓存
     *
     * @param terminal
     * @return
     * @throws Exception
     */
    @CrossOrigin(origins = "*", allowedHeaders = "terminal")
    @RequestMapping(value = "tree/staticWithCache", method = RequestMethod.GET)
    public Object staticWithCache(@RequestHeader(defaultValue = "1") int terminal) throws Exception {
        DebugCacheUtil.showCacheContent(TREE_STATIC, "TREE_STATIC");
        return TREE_STATIC.get(terminal);
    }

    private LoadingCache<Integer, Object> TREE_STATIC =
            CacheBuilder.newBuilder()
                    .maximumSize(10)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build(
                            new CacheLoader<Integer, Object>() {
                                @Override
                                public Object load(Integer terminal) throws Exception {
                                    return getStaticSubjectTree(terminal);
                                }
                            }
                    );

    /**
     * 为教师题库，提供科目树
     *
     * @param terminal
     * @return
     * @throws BizException
     */
    @CrossOrigin(origins = "*", allowedHeaders = "terminal")
    @RequestMapping(value = "/teacher/tree/static", method = RequestMethod.GET)
    public Object getTeacherStaticSubjectTree(@RequestHeader(defaultValue = "1") int terminal) throws BizException {
        List<SubjectTree> treeList = JsonUtil.toList(teacherSubjectTreeConfig.getSubjectJson(), SubjectTree.class);
        return treeList;
    }


}
