package com.huatu.ztk.knowledge.controller;

import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.knowledge.bean.SubjectTree;
import com.huatu.ztk.knowledge.service.SubjectTreeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 科目树
 * Created by linkang on 17-5-11.
 */

@RestController
@RequestMapping(value = "/v1/subjects",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class SubjectController {

    @Autowired
    private SubjectTreeService subjectTreeService;

    @RequestMapping(value = "tree", method = RequestMethod.GET)
    public Object getSubjectTree(@RequestParam(required = false) String ids) throws BizException{
        List<Integer> idList = new ArrayList<>();
        if (StringUtils.isNoneBlank(ids)) {
            idList = Arrays.stream(ids.split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        }
        List<SubjectTree> trees = subjectTreeService.findTree(idList);
        return trees;
    }

    @RequestMapping(value = "tree/static", method = RequestMethod.GET)
    public Object getStaticSubjectTree() throws BizException{
        return subjectTreeService.findStaticTree();
    }
}
