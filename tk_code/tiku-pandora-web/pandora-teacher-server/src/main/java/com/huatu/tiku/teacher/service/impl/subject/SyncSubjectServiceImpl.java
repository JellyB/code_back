package com.huatu.tiku.teacher.service.impl.subject;

import com.huatu.common.ErrorResult;
import com.huatu.common.exception.BizException;
import com.huatu.tiku.entity.subject.Category;
import com.huatu.tiku.entity.subject.OldSubject;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.teacher.dao.subject.CategoryMapper;
import com.huatu.tiku.teacher.dao.subject.OldSubjectMapper;
import com.huatu.tiku.teacher.service.subject.SyncSubjectService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/8/24.
 */
@Service
@Slf4j
public class SyncSubjectServiceImpl implements SyncSubjectService {

    @Autowired
    OldSubjectMapper oldSubjectMapper;
    @Autowired
    CategoryMapper categoryMapper;
    @Autowired
    TeacherSubjectService subjectService;

    @Override
    public Object syncQuestionByCatGory(String name) {
        List<String> categoryNames = Arrays.stream(name.replace("，", ",").split(",")).collect(Collectors.toList());
        for (String categoryName : categoryNames) {
            Category category = findCategoryByName(categoryName);
            if (category == null) {
                continue;
            }
            insertCategoryWithSubject(category);
        }
        return subjectService.getSubjectCount();
    }

    /**
     * 以考试类型为单位将其所属的科目和自身添加到subject表中
     *
     * @param category
     */
    private void insertCategoryWithSubject(Category category) {
        Subject build = Subject.builder().name(category.getName()).level(1).parent(0L).build();
        build.setId(new Long(category.getId()));
        subjectService.insert(build);
        List<OldSubject> subjectList = findOldSubjectByCategory(category.getId());
        if (CollectionUtils.isNotEmpty(subjectList)) {
            subjectList.stream().map(i -> {
                Subject subject = Subject.builder().parent(build.getId()).level(2).name(i.getName()).build();
                subject.setId(new Long(i.getId()));
                return subject;
            }).forEach(subject -> subjectService.insert(subject));
        }
    }

    @Override
    public Object syncQuestionByCatGoryId(Integer id) {
        Example example = new Example(Category.class);
        example.and().andEqualTo("id", id).andEqualTo("status", 1);
        List<Category> categories = categoryMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(categories)) {
            log.error("考试类型：{}不存在", id);
            throw new BizException(ErrorResult.create(12013124, "考试类型：" + id + "不存在"));
        }
        insertCategoryWithSubject(categories.get(0));
        return subjectService.getSubjectCount();
    }

    /**
     * 查询考试类型下的科目
     *
     * @param id
     * @return
     */
    private List<OldSubject> findOldSubjectByCategory(int id) {
        Example example = new Example(OldSubject.class);
        example.and().andEqualTo("catgory", id).andEqualTo("status", 1);
        List<OldSubject> oldSubjects = oldSubjectMapper.selectByExample(example);
        return oldSubjects;
    }

    /**
     * 查询考试类型ID，同步考试科目
     *
     * @param categoryName
     * @return
     */
    private Category findCategoryByName(String categoryName) {
        if (StringUtils.isBlank(categoryName)) {
            return null;
        }
        Example example = new Example(Category.class);
        example.and().andLike("name", "%" + categoryName + "%").andEqualTo("status", 1);
        List<Category> categories = categoryMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(categories) && categories.size() == 1) {
            return categories.get(0);
        }
        if (CollectionUtils.isEmpty(categories)) {
            log.error("考试类型：{}不存在", categoryName);
            return null;
        }
        if (categories.size() > 1) {
            log.error("考试类型：{}存在多个，categories={}", categoryName, categories);
            return null;
        }
        return null;
    }

}
