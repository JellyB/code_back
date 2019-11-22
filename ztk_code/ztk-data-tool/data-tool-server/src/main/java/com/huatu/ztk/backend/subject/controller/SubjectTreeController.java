package com.huatu.ztk.backend.subject.controller;

import com.huatu.ztk.backend.subject.bean.SubjectTreeBean;
import com.huatu.ztk.backend.subject.service.SubjectTreeService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by linkang on 17-5-10.
 */

@RestController
@RequestMapping("/subject/tree")
public class SubjectTreeController {
    private static final Logger logger = LoggerFactory.getLogger(SubjectTreeController.class);

    @Autowired
    private SubjectTreeService subjectTreeService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public Object getTree(@RequestParam(required = false) String ids) {

        List<Integer> idList = new ArrayList<>();
        if (StringUtils.isNoneBlank(ids)) {
            idList = Arrays.stream(ids.split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        }

        return subjectTreeService.findTree(idList);
    }


    /**
     * 添加到科目树
     * @param subjectTreeBean
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Object addSubjectTreeItem(@RequestBody SubjectTreeBean subjectTreeBean) throws BizException {
        logger.info("subject new bean={}", JsonUtil.toJson(subjectTreeBean));
        subjectTreeService.addSubjectTreeItem(subjectTreeBean);

        return SuccessMessage.create("添加成功");
    }


    /**
     * 删除节点
     * @param id
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.DELETE)
    public Object delSubjectTreeItem(@RequestParam int id) throws BizException{
        subjectTreeService.delSubjectTreeItem(id);

        return SuccessMessage.create("删除成功");
    }


    /**
     * 编辑节点
     * @param subjectTreeBean
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public Object editSubjectTreeItem(@RequestBody SubjectTreeBean subjectTreeBean) throws BizException{
        subjectTreeService.editSubjectTreeItem(subjectTreeBean);

        return SuccessMessage.create("更新成功");
    }


    /**
     * 添加该考试类型下的所有学科
     * @param subjectTreeBean
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "allSubjects", method = RequestMethod.POST)
    public Object addAllSubjects(@RequestBody SubjectTreeBean subjectTreeBean) throws BizException {
        subjectTreeService.addAllSubjects(subjectTreeBean);
        return SuccessMessage.create("添加成功");
    }


    /**
     * 删除该考试类型下的所有学科
     * @param catgoryId
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "allSubjects", method = RequestMethod.DELETE)
    public Object delAllSubjects(@RequestParam int catgoryId) throws BizException {
        subjectTreeService.delAllSubjects(catgoryId);
        return SuccessMessage.create("删除成功");
    }


    /**
     * 隐藏
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "hide", method = RequestMethod.POST)
    public Object hide(@RequestParam int id) throws BizException{
        subjectTreeService.hide(id);
        return SuccessMessage.create("隐藏成功");
    }

    /**
     * 取消隐藏
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "hide", method = RequestMethod.DELETE)
    public Object cancelHide(@RequestParam int id) throws BizException{
        subjectTreeService.cancelHide(id);

        return SuccessMessage.create("取消隐藏成功");
    }


    /**
     * 完整的树
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "static", method = RequestMethod.GET)
    public Object getStaticTree() {
        return subjectTreeService.getStaticTree();
    }
}
