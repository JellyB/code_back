package com.huatu.tiku.teacher.service.tag;

import com.huatu.tiku.entity.tag.Tag;
import com.huatu.tiku.service.BaseService;

import java.util.List;

/**
 * @author zhouwei
 * @Description: 题库标签维护
 * @create 2018-04-25 下午2:28
 **/
public interface TeacherTagService extends BaseService<Tag> {
    /**
     * 查询所有标签
     *
     * @param channel  标签渠道
     * @param name
     * @param page     页数
     * @param pageSize 当前页数   @return  查询结果
     */
    Object findAll(Integer channel, String name, int page, int pageSize);


    /**
     * 批量插入试题标签关系数据
     */
    void insertBatchQuestionTags(List<Long> tags, Long questionId);

    /**
     * 批量修改标签试题关系
     *
     * @param tags
     * @param questionId
     * @param modifierId
     */
    void updateQuestionTag(List<Long> tags, Long questionId, Long modifierId);

    /**
     * 删除试题下的所有标签
     *
     * @param questionId
     * @param modifierId
     */
    void deleteQuestionTagByQuestion(Long questionId, Long modifierId);

    /**
     * 通过标签名，批量查询标签id
     *
     * @param tagNames
     * @return
     */
    List<Long> getTagIdByNames(List<String> tagNames);

    /**
     * 查询标签列表
     *
     * @param channel
     * @param name
     * @return
     */
    List<Tag> list(Integer channel, String name);
}
