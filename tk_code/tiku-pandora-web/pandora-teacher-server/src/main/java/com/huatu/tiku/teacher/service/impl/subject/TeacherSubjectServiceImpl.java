package com.huatu.tiku.teacher.service.impl.subject;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.enums.SubjectInfoEnum;
import com.huatu.tiku.response.subject.SubjectMetaResp;
import com.huatu.tiku.response.subject.SubjectNodeResp;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.paper.PaperActivitySubjectMapper;
import com.huatu.tiku.teacher.dao.paper.PaperEntityMapper;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeSubjectService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\5\9 0009.
 */
@Service
public class TeacherSubjectServiceImpl extends BaseServiceImpl<Subject> implements TeacherSubjectService {

    @Autowired
    CommonQuestionServiceV1 commonQuestionService;
    @Autowired
    PaperActivitySubjectMapper paperActivitySubjectMapper;
    @Autowired
    PaperEntityMapper paperEntityMapper;
    @Autowired
    KnowledgeSubjectService knowledgeSubjectService;

    public TeacherSubjectServiceImpl() {
        super(Subject.class);
    }


    /**
     * 科目树，每个树节点的统计信息（如果一个试卷被多个科目使用，也按多次计算）
     *
     * @return
     */
    @Override
    public Object getSubjectCount() {
        List<SubjectMetaResp> result = getSubjectTree();
        return result;
    }

    @Override
    public List<String> getNameByIds(List<Long> grades) {
        Example example = new Example(Subject.class);
        example.and().andIn("id", grades);
        List<Subject> subjects = selectByExample(example);
        if (CollectionUtils.isEmpty(subjects)) {
            return Lists.newArrayList();
        }
        return subjects.stream().map(i -> i.getName()).collect(Collectors.toList());
    }

    @Override
    public List<Subject> findChildren(Long subjectId, int level) {
        Example example = new Example(Subject.class);
        example.and().andEqualTo("level", level + 1);
        if (subjectId != -1) {
            example.and().andEqualTo("parent", subjectId);
        }
        List<Subject> subjects = selectByExample(example);
        if (CollectionUtils.isEmpty(subjects)) {
            return Lists.newArrayList();
        } else {
            return subjects;
        }
    }

    @Override
    public List<Subject> findChildren(Long parentId) {
        WeekendSqls<Subject> weekendSql = WeekendSqls.custom();
        weekendSql.andEqualTo(Subject::getParent, parentId);
        final Example example = Example.builder(Subject.class)
                .where(weekendSql)
                .build();
        return selectByExample(example);
    }

    /**
     * 根据给定知识点，得到选定的科目，返回下级知识点可选科目范围
     *
     * @param knowledgeId
     * @return
     * @update 2018/08/20 huangqp
     * @description 1、如果知识点为0，表示需要返回顶级知识点的可选科目范围：
     * 原来逻辑：返回所有二三级科目；现在逻辑：如果二级科目没有下级结点，返回二级，如果二级科目有下级结点，则返回三级（学段）
     * 2、如果知识点为有效知识点，则：
     * 原有逻辑：根据情况，返回所有涉及的二级科目（如果选定的是某一个科目的三级科目，则二级科目返回，但是标识为不可选）
     * 现有逻辑：返回给定知识点对应的所有科目（如果对应的科目是三级科目，也只返回三级科目，没有flag和children字段）
     */
    @Override
    public Object treeForKnowledge(Long knowledgeId) {
        Example example = new Example(Subject.class);
        example.and().andIn("level", Lists.newArrayList(SubjectInfoEnum.SubjectTypeEnum.SUBJECT.getCode(),
                SubjectInfoEnum.SubjectTypeEnum.GRADE.getCode()));
        List<Subject> subjects = selectByExample(example);
        if (CollectionUtils.isEmpty(subjects)) {
            return Lists.newArrayList();
        }
        Map<Long, Subject> subjectMap = subjects.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        //关联的科目ID
        Set<Long> subjectIds = Sets.newHashSet();
        if (knowledgeId > 0) {
            Example subjectExample = new Example(KnowledgeSubject.class);
            subjectExample.and().andEqualTo("knowledgeId", knowledgeId);
            //关联的科目ID
            subjectIds.addAll(knowledgeSubjectService.selectByExample(subjectExample).stream()
                    .map(KnowledgeSubject::getSubjectId).collect(Collectors.toSet()));
        } else {
            subjectIds.addAll(subjects.stream().map(Subject::getId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isEmpty(subjectIds)) {
            return Lists.newArrayList();
        }
        //有三级科目（学段）的二级科目集合
        Map<Long, List<Subject>> childMap = subjects.stream().filter(i -> i.getLevel() == SubjectInfoEnum.SubjectTypeEnum.GRADE.getCode())
                .collect(Collectors.groupingBy(i -> i.getParent()));
        //删除拥有三级科目的二级科目ID
        subjectIds.removeAll(childMap.keySet());
        if (CollectionUtils.isEmpty(subjectIds)) {
            return Lists.newArrayList();
        }
        return subjectIds.stream().map(i -> {
            Subject subject = subjectMap.get(i);
            //如果是三级科目，拼接二级科目名
            if (SubjectInfoEnum.SubjectTypeEnum.GRADE.getCode() == subject.getLevel().intValue()) {
                String name = subject.getName();
                Subject parent = subjectMap.get(subject.getParent());
                if (parent != null) {
                    name = parent.getName() + "-" + name;
                    subject.setName(name);
                }
            }
            return subject;
        }).sorted(Comparator.comparing(Subject::getId)).collect(Collectors.toList());
    }


    /**
     * 分析科目涉及到的一二三级
     *
     * @param subjectId
     * @return
     */
    @Override
    public SubjectNodeResp parseSubject(Long subjectId) {
        List<Subject> subjects = selectAll();
        Subject subject = subjects.stream().filter(i -> i.getId().equals(subjectId)).findFirst().orElse(null);
        if (subject == null) {
            return SubjectNodeResp.builder().categoryIds(Lists.newArrayList()).subject(-1L).grades(Lists.newArrayList()).build();
        }
        if (subject.getLevel() == SubjectInfoEnum.SubjectTypeEnum.SUBJECT.getCode()) {
            Subject parent = subjects.stream().filter(i -> i.getId().equals(subject.getParent())).findFirst().orElse(null);
            return SubjectNodeResp.builder().categoryIds(Lists.newArrayList(parent.getId())).subject(subjectId).build();
        } else if (subject.getLevel() == SubjectInfoEnum.SubjectTypeEnum.GRADE.getCode()) {
            Subject parent = subjects.stream().filter(i -> i.getId().equals(subject.getParent())).findFirst().get();
            Subject category = subjects.stream().filter(i -> i.getId().equals(parent.getParent())).findFirst().get();
            return SubjectNodeResp.builder().categoryIds(Lists.newArrayList(category.getId())).subject(parent.getParent())
                    .grades(Lists.newArrayList(subject.getId()))
                    .gradeList(Lists.newArrayList(subject.getName())).build();
        }
        return SubjectNodeResp.builder().categoryIds(Lists.newArrayList()).subject(-1L).grades(Lists.newArrayList()).build();
    }

    @Override
    public Subject selectById(Long subjectId) {
        Example example = new Example(Subject.class);
        example.and().andEqualTo("id", subjectId);
        List<Subject> subjects = selectByExample(example);
        if (CollectionUtils.isEmpty(subjects)) {
            return null;
        }
        //如果科目存在多个，则取低层级（level大）的科目
        if (subjects.size() != 1) {
            return subjects.stream().max(Comparator.comparing(i -> i.getLevel())).get();
        }
        return subjects.get(0);
    }


    /**
     * 获取带有统计信息的科目树
     *
     * @return
     */
    public List<SubjectMetaResp> getSubjectTree() {
        List<Subject> subjects = selectAll();
        //科目树结构初始化
        List<SubjectMetaResp> result = getChildrenByPid(subjects, 0L, 1);
        //试题数量查询
        Map<Long, Integer> subjectQuestionCountMap = getSubjectQuestionCount();
        Map<Long, Integer> subjectPaperEntityCountMap = getSubjectPaperEntityCount();
        Map<Long, Integer> subjectPaperActivityCountMap = getSubjectPaperActivityCount();
        fillCount2Tree(result, subjectPaperActivityCountMap, subjectPaperEntityCountMap, subjectQuestionCountMap);
        return result;
    }

    /**
     * 给科目树节点填充统计数据
     *
     * @param target                       科目树
     * @param subjectPaperActivityCountMap 科目活动卷数量对应关系
     * @param subjectPaperEntityCountMap   科目实体卷数量对应关系
     * @param subjectQuestionCountMap      科目试题数量对应关系
     */
    private void fillCount2Tree(List<SubjectMetaResp> target, Map<Long, Integer> subjectPaperActivityCountMap, Map<Long, Integer> subjectPaperEntityCountMap, Map<Long, Integer> subjectQuestionCountMap) {
        if (CollectionUtils.isEmpty(target)) {
            return;
        }
        //遍历科目节点
        for (SubjectMetaResp subject : target) {
            Long subjectId = subject.getId();
            //得到每个节点直接关联的试卷，试题数量
            Integer paperActivityTotal = subjectPaperActivityCountMap.getOrDefault(subjectId, 0);
            Integer paperEntityTotal = subjectPaperEntityCountMap.getOrDefault(subjectId, 0);
            Integer questionTotal = subjectQuestionCountMap.getOrDefault(subjectId, 0);
            /**
             * 如果科目层级为1,意味着是顶级科目，这层的科目不会挂题，所数量初始化为0
             * @reason 一级科目和其他层级的节点的id可能相同，比如行测科目id为1 ，公务员考试科目也为1,所以初始化是必要的
             */
            if (subject.getLevel() == 1) {
                paperActivityTotal = 0;
                paperEntityTotal = 0;
                questionTotal = 0;
            }
            List<SubjectMetaResp> children = subject.getChildren();
            //判断是否有子节点，如果有，分析子节点上的试卷，试题数量，并将结果累加到直接关联的试题上
            if (CollectionUtils.isNotEmpty(children)) {
                fillCount2Tree(children, subjectPaperActivityCountMap, subjectPaperEntityCountMap, subjectQuestionCountMap);
                for (SubjectMetaResp child : children) {
                    paperActivityTotal += child.getPaperActivityTotal();
                    paperEntityTotal += child.getPaperEntityTotal();
                    questionTotal += child.getQuestionTotal();
                }
            }
            //赋值
            subject.setPaperActivityTotal(paperActivityTotal);
            subject.setPaperEntityTotal(paperEntityTotal);
            subject.setPaperTotal(paperActivityTotal);
            subject.setQuestionTotal(questionTotal);
        }
    }

    /**
     * 筛选父节点的子节点数据
     *
     * @param subjects
     * @param parent
     * @param level
     * @return
     */
    private List<SubjectMetaResp> getChildrenByPid(List<Subject> subjects, Long parent, Integer level) {
        List<SubjectMetaResp> subjectMetaResps = Lists.newArrayList();
        List<Subject> subjectList = subjects.stream().filter(i -> i.getParent().equals(parent)).filter(i -> i.getLevel().equals(level)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(subjectList)) {
            return subjectMetaResps;
        }
        for (Subject subject : subjectList) {
            Integer childLevel = level + 1;
            SubjectMetaResp temp = new SubjectMetaResp();
            BeanUtils.copyProperties(subject, temp);
            List<SubjectMetaResp> children = getChildrenByPid(subjects, subject.getId(), childLevel);
            temp.setChildren(children);
            subjectMetaResps.add(temp);
        }
        return subjectMetaResps;
    }

    /**
     * 查询科目下的试题数量
     *
     * @return
     */
    public Map<Long, Integer> getSubjectQuestionCount() {
        List<Map<String, Long>> result = commonQuestionService.countQuestionGroupBySubject();
        return result.stream().collect(Collectors.toMap(i -> i.get("subjectId"), i -> i.get("total").intValue()));
    }

    /**
     * 查询科目下的实体试卷数量
     *
     * @return
     */
    public Map<Long, Integer> getSubjectPaperEntityCount() {
        List<Map<String, Long>> result = paperEntityMapper.countGroupBySubject();
        return result.stream().collect(Collectors.toMap(i -> i.get("subjectId"), i -> i.get("total").intValue()));
    }

    /**
     * 查询科目下的活动卷数量
     *
     * @return
     */
    public Map<Long, Integer> getSubjectPaperActivityCount() {
        List<Map<String, Long>> result = paperActivitySubjectMapper.countGroupBySubject();
        return result.stream().collect(Collectors.toMap(i -> i.get("subjectId"), i -> i.get("total").intValue()));
    }


    /*
     查找一级节点
     */
    public Long findParent(Long subjectId, int level) {
        Subject subject = new Subject();
        subject.setLevel(level);
        subject.setId(subjectId);
        Subject subjectResult = selectOne(subject);

        if (subjectResult == null) {
            throwBizException("不存在的科目信息");
        }
        if (subjectResult.getLevel().equals(1) && subjectResult.getParent() == 0) {
            return subjectResult.getId();
        }
        return findParent(subjectResult.getParent(), subjectResult.getLevel() - 1);

    }

    @Override
    public List<Subject> findFriendNodes(Long subjectId) {
        Subject subject = selectById(subjectId);
        if (null == subject) {
            return Lists.newArrayList();
        }
        return findChildren(subject.getParent());
    }


    /**
     * 根据级别获取子科目ID
     *
     * @param subjectId 父科目ID
     * @param level     级别
     * @return
     */
    @Override
    public List<Integer> findChildrenId(Long subjectId, int level) {
        List<Subject> children = findChildren(subjectId, level);
        if (CollectionUtils.isNotEmpty(children)) {
            return children.stream().map(Subject::getId)
                    .map(id -> id.intValue())
                    .collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 查询所有的一级考试类别
     *
     * @param level
     * @return
     */
    public List<Subject> findAllCategory(int level, int parent) {
        Example example = new Example(Subject.class);
        example.and().andEqualTo("level", level)
                .andEqualTo("parent", parent);
        List<Subject> subjects = selectByExample(example);
        return subjects;
    }
}
