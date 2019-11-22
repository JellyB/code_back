package com.huatu.tiku.match.web.controller.v1.tag;

import com.google.common.collect.Lists;
import com.huatu.springboot.web.version.mapping.annotation.ApiVersion;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.match.common.Tag;
import com.huatu.tiku.match.service.v1.tag.TagService;
import com.huatu.ztk.commons.TerminalType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.util.VersionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2018-10-24 上午10:05
 **/
@RequestMapping(value = "tag")
@RestController
@ApiVersion(value = "v1")
public class TagControllerV1 {

    @Autowired
    private TagService tagService;

    /**
     * 获取科目对应模考大赛的标签
     * --标签数据通过pandora接口生成(/pand/match/tag)
     * --申论标签查询可以只返回申论相关的标签（通过flag做的筛选）
     * --其他科目的标签查询会附带兄弟科目的标签（详情见package-info）
     *
     * @param subject
     * @return
     * @throws BizException
     */
    @LogPrint
    @GetMapping("{subject}")
    public Object getTags(@PathVariable(value = "subject") int subject,
                          @RequestParam(defaultValue = "-1") int subjectId,
                          @RequestHeader(defaultValue = "-1") int terminal,
                          @RequestHeader(defaultValue = "-1") String cv) throws BizException {
        if (subjectId > 0) {
            subject = subjectId;
        }
        if (subject < 0) {
            return Lists.newArrayList();
        }

        List<Tag> resultTag = new ArrayList<>();

        List<Tag> tags = tagService.getTags(subject);
        switch (terminal) {
            case TerminalType.ANDROID:
            case TerminalType.ANDROID_IPAD: {       //安卓系统7.1.162版本前的需要做适配
                if (VersionUtil.compare(cv, "7.1.162") > 0) {
                    return tags;
                }
                tags.stream().forEach(tag -> {
                    //申论标签
                    if (tag.getSubject() == 14) {
                        if (tag.getId() <= 3) {
                            resultTag.add(tag);
                        }
                    } else {
                        resultTag.add(tag);
                    }
                });
                break;
            }
            case TerminalType.PC://pc不展示申论标签
                resultTag.addAll(tags.stream().filter(i -> i.getSubject() != 14).collect(Collectors.toList()));
                break;
            default:
                return tags;

        }
        return resultTag;
    }

    /**
     * 获得科目所属考试类型的所有含模考大赛的科目集合
     * 模考大赛首页科目切换功能用到
     *
     * @return
     */
    @GetMapping("subject/{subject}")
    public Object getMatchSubject(@PathVariable(value = "subject") int subject,
                                  @RequestParam(defaultValue = "-1") int subjectId) throws BizException {
        if (subjectId > 0) {
            subject = subjectId;
        }
        if (subject < 0) {
            return Lists.newArrayList();
        }
        return tagService.getSubjectList(subject);
    }

    /**
     * 获得支持模考大赛的所有考试类型信息
     *
     * @return
     */
    @GetMapping("category")
    public Object getMatchCategory(@RequestHeader int terminal) throws BizException {
        return tagService.getMatchCategory(terminal);
    }
}
