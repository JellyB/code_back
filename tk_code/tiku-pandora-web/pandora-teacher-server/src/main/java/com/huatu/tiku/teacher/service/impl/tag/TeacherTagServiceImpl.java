package com.huatu.tiku.teacher.service.impl.tag;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.huatu.tiku.entity.tag.QuestionTag;
import com.huatu.tiku.entity.tag.Tag;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.tag.TeacherQuestionTagService;
import com.huatu.tiku.teacher.service.tag.TeacherTagService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouwei
 * @Description: TODO
 * @create 2018-04-25 下午2:37
 **/
@Service
@Slf4j
public class TeacherTagServiceImpl extends BaseServiceImpl<Tag> implements TeacherTagService {
    @Autowired
    private TeacherQuestionTagService questionTagService;

    public TeacherTagServiceImpl() {
        super(Tag.class);
    }

    @Override
    public Object findAll(Integer channel, String name, int page, int pageSize) {
        PageInfo<Tag> tagPage = PageHelper.startPage(page, pageSize).doSelectPageInfo(() -> {
            if ((channel == -1) && StringUtils.isBlank(name)) {
                selectAll();
            } else {
                Example example = new Example(Tag.class);
                example.and().andEqualTo("channel", channel);
                selectByExample(example);
            }

        });


        return tagPage;
    }


    @Override
    @Transactional
    public void insertBatchQuestionTags(List<Long> tags, Long questionId) {
        List<QuestionTag> insertTagList = Lists.newArrayList();
        tags.forEach(i -> {
            QuestionTag questionTag = new QuestionTag();
            questionTag.setQuestionId(questionId);
            questionTag.setTagId(i);
            insertTagList.add(questionTag);
        });
        questionTagService.insertAll(insertTagList);
    }

    @Override
    @Transactional
    public void updateQuestionTag(List<Long> tags, Long questionId, Long modifierId) {
        Example example = new Example(QuestionTag.class);
        example.and().andEqualTo("questionId", questionId);
        List<QuestionTag> questionTags = questionTagService.selectByExample(example);
        List<Long> oldTags = questionTags.stream().map(i -> i.getTagId()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(oldTags) && CollectionUtils.isNotEmpty(tags)) {
            Collection<Long> unChangedIds = CollectionUtils.intersection(oldTags, tags);
            tags.removeAll(unChangedIds);
            oldTags.removeAll(unChangedIds);
        }
        //批量删除
        if (CollectionUtils.isNotEmpty(oldTags)) {
            Example delExample = new Example(QuestionTag.class);
            delExample.and().andEqualTo("questionId", questionId).andIn("tagId", oldTags);
            QuestionTag delQuestionTag = new QuestionTag();
            delQuestionTag.setStatus(-1);
            delQuestionTag.setModifierId(modifierId);
            questionTagService.updateByExampleSelective(delQuestionTag, delExample);
        }

        //批量添加
        if (CollectionUtils.isNotEmpty(tags)) {
            insertBatchQuestionTags(tags, questionId);
        }

    }

    @Override
    @Transactional
    public void deleteQuestionTagByQuestion(Long questionId, Long modifierId) {
        Example delExample = new Example(QuestionTag.class);
        delExample.and().andEqualTo("questionId", questionId);
        questionTagService.deleteByExample(delExample);
    }

    @Override
    public List<Long> getTagIdByNames(List<String> tagNames) {
        Example example = new Example(Tag.class);
        example.and().andIn("name", tagNames);
        List<Tag> tags = selectByExample(example);
        if (CollectionUtils.isEmpty(tags)) {
            return Lists.newArrayList();
        }
        return tags.stream().map(i -> i.getId()).collect(Collectors.toList());
    }

    @Override
    public List<Tag> list(Integer channel, String name) {
        Example example = new Example(Tag.class);
        if (channel != -1) {
            example.and().andEqualTo("channel", channel);
        }
        if (StringUtils.isNotBlank(name)) {
            example.and().andLike("name", "%" + name + "%");
        }
        return selectByExample(example);
    }

    private List<Tag> selectByIds(List<Long> collect) {
        Example example = new Example(Tag.class);
        example.and().andIn("id", collect);
        return selectByExample(example);
    }
}
