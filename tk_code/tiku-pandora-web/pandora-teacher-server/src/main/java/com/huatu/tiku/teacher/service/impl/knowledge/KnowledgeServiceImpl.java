package com.huatu.tiku.teacher.service.impl.knowledge;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.constants.cache.RedisKeyConstant;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.entity.knowledge.KnowledgeSubject;
import com.huatu.tiku.entity.knowledge.QuestionKnowledge;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.enums.SubjectInfoEnum;
import com.huatu.tiku.request.knowledge.InsertKnowledgeReq;
import com.huatu.tiku.request.knowledge.UpdateKnowledgeReq;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.knowledge.KnowledgeMapper;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeSubjectService;
import com.huatu.tiku.teacher.service.knowledge.QuestionKnowledgeService;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.ztk.question.bean.KnowledgeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhouwei
 * @Description: impl
 * @create 2018-04-23 下午3:16
 **/
@Service
@Slf4j
public class KnowledgeServiceImpl extends BaseServiceImpl<Knowledge> implements KnowledgeService {
    @Autowired
    TeacherSubjectService subjectService;
    @Autowired
    KnowledgeMapper knowledgeMapper;
    @Autowired
    QuestionKnowledgeService questionKnowledgeService;
    @Autowired
    KnowledgeSubjectService knowledgeSubjectService;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    KnowledgeComponent knowledgeComponent;

    public KnowledgeServiceImpl() {
        super(Knowledge.class);
    }

    /**
     * 知识点的最大层级
     */
    private static final Integer MAX_LEVEL = 5;

    /**
     * 查询知识点树
     *
     * @param subjectId
     * @param countFlag
     * @return
     */
    @Override
    public List<KnowledgeVO> showKnowledgeTreeBySubject(Long subjectId, boolean countFlag) {
        List<KnowledgeVO> knowledgeTree = getKnowledgeTree(subjectId, countFlag);
        return knowledgeTree;
    }

    /**
     * 查询某一节点下的知识树
     *
     * @param subjectId
     * @param countFlag 是否附带查询统计数据
     * @return
     */
    public List<KnowledgeVO> getKnowledgeTree(long subjectId, boolean countFlag) {
        //根据科目和查询改科目及其上级节点上挂着的所有有效知识点
        List<Knowledge> knowledgeList = selectKnowledgeBySubject(subjectId);
        if (CollectionUtils.isEmpty(knowledgeList)) {
            return Lists.newArrayList();
        }
        knowledgeList = knowledgeList.stream().distinct().collect(Collectors.toList());
        List<KnowledgeVO> treeList = assertKnowledgeTree(knowledgeList, 0L);
        //附带查询统计数据
        if (countFlag) {
            Map<Long, Integer> countMap = getKnowledgeCountMap(subjectId);
            for (KnowledgeVO knowledgeVO : treeList) {
                knowledgeVO.setCount(getKnowledgeCount(knowledgeVO, countMap));
            }
        }
        return treeList;
    }

    /**
     * 递归计算每个知识点的试题数
     *
     * @param knowledgeVO
     * @param countMap
     * @return
     */
    private Integer getKnowledgeCount(KnowledgeVO knowledgeVO, Map<Long, Integer> countMap) {
        Integer count = 0;
        if (CollectionUtils.isEmpty(knowledgeVO.getKnowledgeTrees())) {
            count = countMap.getOrDefault(knowledgeVO.getKnowledgeId(), 0);
        } else {
            for (KnowledgeVO child : knowledgeVO.getKnowledgeTrees()) {
                count += getKnowledgeCount(child, countMap);
            }
        }
        knowledgeVO.setCount(count);
        return count;
    }

    /**
     * 查询科目下每个直接点都直接关联多少试题
     *
     * @param subjectId
     * @return
     */
    private Map<Long, Integer> getKnowledgeCountMap(long subjectId) {
        List<Map<String, Long>> mapList = knowledgeMapper.countKnowledgeQuestionBySubject(subjectId);
        return mapList.stream().collect(Collectors.toMap(i -> i.get("knowledgeId"), i -> i.get("questionSize").intValue()));
    }

    /**
     * 将知识点列表拼成知识树
     *
     * @param knowledgeList 知识点列表
     * @param parentId      父节点id
     * @return 父节点下的知识树组成的知识树列表
     */
    @Override
    public List<KnowledgeVO> assertKnowledgeTree(List<Knowledge> knowledgeList, Long parentId) {
        knowledgeList.forEach(i -> {
            if (null == i.getSortNum()) {
                i.setSortNum(Integer.MIN_VALUE);
            }
        });
        knowledgeList.sort(Comparator.comparing(Knowledge::getSortNum));
        List<KnowledgeVO> result = Lists.newLinkedList();
        List<Knowledge> root = findChildren(knowledgeList, parentId);
        if (CollectionUtils.isNotEmpty(root)) {
            for (Knowledge knowledge : root) {
                KnowledgeVO temp = KnowledgeVO.builder()
                        .knowledgeId(knowledge.getId())
                        .knowledgeName(knowledge.getName())
                        .level(knowledge.getLevel())
                        .build();
                List<KnowledgeVO> children = assertKnowledgeTree(knowledgeList, knowledge.getId());
                if (CollectionUtils.isEmpty(children)) {
                    temp.setHaveSub(false);
                    temp.setKnowledgeTrees(Lists.newArrayList());
                } else {
                    temp.setHaveSub(true);
                    temp.setKnowledgeTrees(children);
                }
                result.add(temp);
            }
        }
        return result;
    }

    private List<Knowledge> findChildren(List<Knowledge> knowledgeList, Long parentId) {
        return knowledgeList.stream().filter(i -> i.getParentId().equals(parentId)).collect(Collectors.toList());
    }

    @Override
    public long insertKnowledge(InsertKnowledgeReq knowledge) {
        List<Long> subjects = knowledge.getSubject();
        if (CollectionUtils.isEmpty(subjects)) {
            throwBizException("知识点的关联科目不能为空");
        }
        //通过知识点ID+科目ID组装知识点科目关联关系
        Function<Long, List<KnowledgeSubject>> transData = (id -> subjects.stream()
                .map(i -> KnowledgeSubject.builder().knowledgeId(id).subjectId(i).build())
                .collect(Collectors.toList()));
        //没有父节点直接添加顶级结点
        //查询父知识点
        Knowledge parent = null;
        if (knowledge.getParentId() > 0) {
            parent = selectByPrimaryKey(knowledge.getParentId());
        }
        if (null == parent) {
            Knowledge build = Knowledge.builder()
                    .name(knowledge.getName().trim())
                    .parentId(knowledge.getParentId())
                    .level(1)
                    .build();
            if (null != knowledge.getSortNum() && knowledge.getSortNum() > 0) {
                build.setSortNum(knowledge.getSortNum());
            } else {
                build.setSortNum(1);
            }
            save(build);
            knowledgeComponent.saveCacheInfo(build);
            knowledgeSubjectService.insertAll(transData.apply(build.getId()));
            return build.getId();
        } else {
            if (parent.getLevel() == MAX_LEVEL) {
                throwBizException("知识点最多支持" + MAX_LEVEL + "级");
            }
            //校验父节点科目跟子节点的关系
            List<HashMap<String, Object>> subjectList = findSubjectInfoByIds(Lists.newArrayList(parent.getId()));
            if (CollectionUtils.isEmpty(subjectList)) {
                throwBizException("上级知识点无科目");
            }
            String tempSubject = subjectList.get(0).getOrDefault("subjectId", "").toString();
            if (StringUtils.isBlank(tempSubject)) {
                throwBizException("上级知识点无有效的科目");
            }
            List<Long> parentSubjectIds = Arrays.stream(tempSubject.split("、")).map(Long::new).collect(Collectors.toList());
            if (!parentSubjectIds.containsAll(subjects)) {
                throwBizException("子节点选择的科目必须在上级节点的科目范围内");
            }
            //父节点变为非叶子结点
            parent.setIsLeaf(false);
            save(parent);
            knowledgeComponent.saveCacheInfo(parent);
            //添加
            Knowledge current = Knowledge.builder()
                    .parentId(knowledge.getParentId())
                    .name(knowledge.getName().trim())
                    .level(parent.getLevel() + 1)
                    .isLeaf(true)
                    .build();
            if (null != knowledge.getSortNum() && knowledge.getSortNum() > 0) {
                current.setSortNum(knowledge.getSortNum());
            } else {
                current.setSortNum(1);
            }
            insert(current);
            knowledgeComponent.saveCacheInfo(current);
            //插入科目绑定关系
            knowledgeSubjectService.insertAll(transData.apply(current.getId()));
            clearRedis();
            return current.getId();
        }
    }

    /**
     * 批量获取科目的子节点
     *
     * @param parentSubjectIds
     */
    public List<Long> findChildSubject(List<Long> parentSubjectIds) {
        Example example = new Example(Subject.class);
        example.and().andIn("parent", parentSubjectIds);
        List<Subject> subjects = subjectService.selectByExample(example);
        if (CollectionUtils.isNotEmpty(subjects)) {
            return subjects.stream().map(Subject::getId).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }


    @Override
    @Transactional
    public void updateQuestionKnowledge(List<Long> knowledgeIds, Long questionId) {
        //物理删除
        questionKnowledgeService.deleteByQuestionId(questionId);
        //批量添加
        if (CollectionUtils.isNotEmpty(knowledgeIds)) {
            questionKnowledgeService.insertQuestionKnowledgeInfo(knowledgeIds, questionId);
        }
    }


    /**
     * 查询知识点的完整信息
     *
     * @param ids
     * @return
     */
    @Override
    public List<Map> getKnowledgeInfoByIds(List<Long> ids) {
        ids.removeIf(i -> i.intValue() == -1);
        List<Map> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(ids)) {
            return result;
        }
        List<Knowledge> knowledgeList = findAll();
        for (Long id : ids) {
            Map map = Maps.newHashMap();
            map.put("key", id);
            map.put("value", assertKnowledgeName(knowledgeList, id).split("-"));
            result.add(map);
        }
        return result;
    }

    @Override
    public List<KnowledgeInfo> getPointListByIds(List<Long> knowledgeIds) {
        if (CollectionUtils.isEmpty(knowledgeIds)) {
            return Lists.newArrayList();
        }
        Map<Long, Knowledge> knowledgeMap = findAll().stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        /**
         * 根据知识点id,获取顶级结点到自身结点的所有结点ID+name，组成对象
         */
        Function<Long, KnowledgeInfo> transData = ((Long id) -> {
            Knowledge knowledge = knowledgeMap.get(id);
            LinkedList<String> pointsName = Lists.newLinkedList();
            LinkedList<Integer> points = Lists.newLinkedList();
            while (knowledge != null) {
                points.addFirst(knowledge.getId().intValue());
                pointsName.addFirst(knowledge.getName());
                Long parentId = knowledge.getParentId();
                knowledge = knowledgeMap.get(parentId);
            }
            return KnowledgeInfo.builder().pointsName(pointsName).points(points).build();
        });

        return knowledgeIds.stream().map(i -> transData.apply(i)).collect(Collectors.toList());
    }

    @Override
    public List<String> getKnowledgeNameByIds(List<Long> ids) {
        List<String> result = Lists.newArrayList();
        if (CollectionUtils.isEmpty(ids)) {
            return result;
        }
        List<Knowledge> knowledgeList = findAll();
        for (Long id : ids) {
            String s = assertKnowledgeName(knowledgeList, id);
            result.add(s);
        }
        return result;
    }

    @Override
    public List<Long> getKnowledgeIdByInfo(List<String> knowledgeList, List<KnowledgeVO> knowledgeVOS) {
        List<Long> ids = Lists.newArrayList();
        for (String knowledgeName : knowledgeList) {
            String[] split = knowledgeName.split("\\*");
            List<String> names = Lists.newArrayList(split);
            Long id = getKnowledgeIdByName(names, knowledgeVOS);
            if (id < 0) {
                throwBizException("知识点（" + knowledgeName + "）不存在,请核对知识树上知识点名称");
            }
            ids.add(id);
        }

        return ids;
    }

    /**
     * 通过多级知识点名称锁定最低层知识点id
     *
     * @param names
     * @param knowledgeVOS
     * @return
     */
    private Long getKnowledgeIdByName(List<String> names, List<KnowledgeVO> knowledgeVOS) {
        if (CollectionUtils.isEmpty(knowledgeVOS)) {
            return -1L;
        }
        String name = names.get(0);
        int length = names.size();
        for (KnowledgeVO knowledgeVO : knowledgeVOS) {
            if (knowledgeVO.getKnowledgeName().equals(name)) {
                if (length == 1) {
                    return knowledgeVO.getKnowledgeId();
                } else {
                    return getKnowledgeIdByName(names.subList(1, names.size()), knowledgeVO.getKnowledgeTrees());
                }
            }
        }
        //如果当前层级的知识点没有匹配到，可以往下级进行匹配
        List<Long> tempList = Lists.newArrayList();
        for (KnowledgeVO knowledgeVO : knowledgeVOS) {
            /**
             * 当前层级没有匹配到的，会穷举所有当前知识点，并做判断，如果有多个匹配值，则直接报错
             */
            Long tempId = getKnowledgeIdByName(names, knowledgeVO.getKnowledgeTrees());
            if (!tempId.equals(-1L)) {
                tempList.add(tempId);
                //在找到第二个知识点时，就报错，避免更多的穷举循环
                if (tempList.size() > 1) {
                    throwBizException("知识点(" + names.stream().collect(Collectors.joining("*")) + ")被多次匹配到，请添加层级，保证只能匹配一个知识点");
                }
            }
        }
        if (tempList.size() == 1) {
            return tempList.get(0);
        }
        return -1L;
    }

    public String assertKnowledgeName(List<Knowledge> knowledgeList, Long id) {
        Knowledge knowledge = knowledgeList.stream().filter(i -> i.getId().equals(id)).findAny().orElse(null);
        if (knowledge == null) {
            return id + "";
        }
        if (knowledge.getParentId() != 0) {
            return assertKnowledgeName(knowledgeList, knowledge.getParentId()) + "-" + knowledge.getName();
        }
        return knowledge.getName();
    }

    /**
     * 根据科目查询所有的知识点(向下兼容)
     *
     * @param subjectId
     * @return
     */
    public List<Knowledge> selectKnowledgeBySubject(Long subjectId) {
        List<Long> subjectIds = Lists.newArrayList(subjectId);
        Subject subject = subjectService.selectById(subjectId);
        if (subject == null) {
            return Lists.newArrayList();
        }
        int level = subject.getLevel();
        if (level == SubjectInfoEnum.SubjectTypeEnum.SUBJECT.getCode()) {
            List<Subject> children = subjectService.findChildren(subjectId, level);
            if (CollectionUtils.isNotEmpty(children)) {
                List<Subject> grades = children.stream().filter(i -> i.getLevel() == SubjectInfoEnum.SubjectTypeEnum.GRADE.getCode()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(grades)) {
                    subjectIds.addAll(grades.stream().map(Subject::getId).collect(Collectors.toList()));
                }
            }
        }
        return knowledgeMapper.selectKnowledgeBySubject(subjectIds);
    }

    /**
     * 更新知识点
     * 变动的属性只有 知识点名称+科目
     *
     * @param updateKnowledgeReq
     */
    @Override
    @Transactional
    public long updateKnowledge(UpdateKnowledgeReq updateKnowledgeReq) {
        long parentId = updateKnowledgeReq.getParentId();
        //判断所选科目和上级节点所选科目是否冲突
        if (parentId > 0) {
            Example example = new Example(KnowledgeSubject.class);
            example.and().andEqualTo("knowledgeId", parentId);
            List<Long> parentSubjectIds = knowledgeSubjectService.selectByExample(example).stream().map(KnowledgeSubject::getSubjectId).collect(Collectors.toList());
            if (!parentSubjectIds.containsAll(updateKnowledgeReq.getSubject())) {
                throwBizException("子节点选择的科目必须在上级节点的科目范围内");
            }
        }
        long id = updateKnowledgeReq.getId();
        //判断ID的合法性
        Knowledge knowledge = selectByPrimaryKey(id);
        if (knowledge == null) {
            throwBizException("无效的知识点ID");
        }
        //判断所选科目与下级知识点的科目是否冲突
        Example example = new Example(Knowledge.class);
        example.and().andEqualTo("parentId", id);
        List<Knowledge> children = selectByExample(example);
        if (CollectionUtils.isNotEmpty(children)) {
            //获取知识点对应的科目信息
            List<HashMap<String, Object>> subjectInfo = findSubjectInfoByIds(children.stream().map(i -> i.getId()).collect(Collectors.toList()));
            //关联的科目信息统计(subjectId->subjectName)
            Map<Long, String> subjectMap = Maps.newHashMap();
            //遍历科目信息，存入subjectMap
            for (HashMap<String, Object> info : subjectInfo) {
                if (info.get("subjectId") == null || StringUtils.isBlank(info.get("subjectId").toString())) {
                    continue;
                }
                List<Long> ids = Arrays.stream(info.get("subjectId").toString().split("、")).map(Long::new).collect(Collectors.toList());
                List<String> names = Arrays.stream(info.get("subjectName").toString().split("、")).map(String::valueOf).collect(Collectors.toList());
                for (int i = 0; i < ids.size(); i++) {
                    subjectMap.put(ids.get(i), names.get(i));
                }
            }
            //判断是下级知识点关联的科目是否全都在当前选定的科目下
            if (subjectMap.size() > 0 && !updateKnowledgeReq.getSubject().containsAll(subjectMap.keySet())) {
                Set<Long> ids = subjectMap.keySet();
                //去掉包含的，剩余多余的，当前不具备的科目
                ids.removeAll(updateKnowledgeReq.getSubject());
                String errorName = StringUtils.join(ids.stream().map(i -> subjectMap.get(i)).collect(Collectors.toList()), "、");
                throwBizException("科目“" + errorName + "”被下级知识点绑定，请先解绑下级知识点");
            }
        }
        //执行数据编辑操作
        knowledge.setName(updateKnowledgeReq.getName().trim());
        if (null != updateKnowledgeReq.getSortNum() && updateKnowledgeReq.getSortNum() > 0) {
            knowledge.setSortNum(updateKnowledgeReq.getSortNum());
        }
        save(knowledge);
        knowledgeComponent.saveCacheInfo(knowledge);
        //物理删除关联表数据
        knowledgeSubjectService.deleteByKnowledge(id);
        //添加关联数据
        knowledgeSubjectService.insertAll(updateKnowledgeReq.getSubject().stream()
                .map(i -> KnowledgeSubject.builder().knowledgeId(id).subjectId(i).build())
                .collect(Collectors.toList()));
        clearRedis();
        return id;
    }

    @Override
    @Transactional
    public int deleteKnowledge(Long knowledgeId) {
        Knowledge knowledge = selectByPrimaryKey(knowledgeId);
        if (null == knowledge) {
            clearRedis();
            return 0;
        }
        Function<Long, List<Knowledge>> findChildren = (parentId -> {
            Example example = new Example(Knowledge.class);
            example.and().andEqualTo("parentId", knowledgeId);
            return selectByExample(example);
        });
        List<Knowledge> knowledges = findChildren.apply(knowledgeId);
        if (CollectionUtils.isNotEmpty(knowledges)) {
            throwBizException("该知识点存在子知识点，请先删除子节点");
        }
        Example questionExample = new Example(QuestionKnowledge.class);
        questionExample.and().andEqualTo("knowledgeId", knowledgeId);
        List<QuestionKnowledge> questionKnowledges = questionKnowledgeService.selectByExample(questionExample);
        if (CollectionUtils.isNotEmpty(questionKnowledges)) {
            throwBizException("该知识点下有试题，请先删除试题");
        }
        //删除知识点表数据
        deleteByPrimaryKey(knowledgeId);
        knowledgeComponent.deleteCacheInfo(knowledgeId);
        //判断父节点是否还有子节点
        List<Knowledge> nodes = findChildren.apply(knowledge.getParentId());
        //
        //如果没有子节点，则父节点变为叶子结点
        if (CollectionUtils.isEmpty(nodes)) {
            //排除一级节点
            if (knowledge.getParentId() != 0) {
                Knowledge parent = selectByPrimaryKey(knowledge.getParentId());
                parent.setIsLeaf(true);
                knowledgeComponent.saveCacheInfo(parent);
                save(parent);
            }
        }
        //删除知识点科目关联表
        knowledgeSubjectService.deleteByKnowledge(knowledgeId);
        clearRedis();
        return 1;
    }

    /**
     * 查询知识点对应的科目信息
     *
     * @param knowledgeIds 知识点ID
     * @return knowledgeId -> 知识点ID
     * subjectId -> 对应科目ID(所有ID拼接的字符串)
     * subjectName -> 对应科目名称(所有Name拼接的字符串)
     */
    @Override
    public List<HashMap<String, Object>> findSubjectInfoByIds(List<Long> knowledgeIds) {
        if (CollectionUtils.isEmpty(knowledgeIds)) {
            return Lists.newArrayList();
        }
        return knowledgeMapper.findSubjectInfoByKnowledge(knowledgeIds);
    }

    @Override
    public List treeBySubject(Long subject) {
        //根据科目和查询改科目及其上级节点上挂着的所有有效知识点
        List<Knowledge> knowledgeList = selectKnowledgeBySubject(subject);
        if (CollectionUtils.isEmpty(knowledgeList)) {
            return Lists.newArrayList();
        }
        knowledgeList = knowledgeList.stream().distinct().collect(Collectors.toList());
        List<KnowledgeVO> treeList = assertKnowledgeTree(knowledgeList, 0L);
        List<Long> knowledgeIds = knowledgeList.stream().map(i -> i.getId()).collect(Collectors.toList());
        //知识点查询科目信息
        List<HashMap<String, Object>> subjectList = findSubjectInfoByIds(knowledgeIds);
        if (CollectionUtils.isEmpty(subjectList)) {
            return treeList;
        }
        //knowledgeID -》 科目信息
        Map<Long, HashMap<String, Object>> subjectMap = subjectList.stream().collect(Collectors.toMap(i -> Long.parseLong(i.get("knowledgeId").toString()), i -> i));
        return treeList.stream().map(i -> assertSubjectInfo(i, subjectMap)).collect(Collectors.toList());
    }

    /**
     * 知识点所属的科目信息填充
     *
     * @param knowledge
     * @param subjectMap
     */
    private KnowledgeVO assertSubjectInfo(KnowledgeVO knowledge, Map<Long, HashMap<String, Object>> subjectMap) {
        Function<KnowledgeVO, KnowledgeVO> transData = (knowledgeVO -> {
            Long knowledgeId = knowledgeVO.getKnowledgeId();
            if (subjectMap.get(knowledgeId) != null) {
                HashMap<String, Object> tempMap = subjectMap.get(knowledgeId);
                String subjectId = tempMap.getOrDefault("subjectId", "").toString();
                String subjectName = tempMap.getOrDefault("subjectName", "").toString();
                if (StringUtils.isNotBlank(subjectId)) {
                    knowledgeVO.setSubjectIds(Arrays.stream(subjectId.split("、")).map(Long::new).collect(Collectors.toList()));
                    knowledgeVO.setSubjectNames(Arrays.stream(subjectName.split("、")).collect(Collectors.toList()));
                }
            }
            return knowledgeVO;
        });
        KnowledgeVO result = transData.apply(knowledge);
        List<KnowledgeVO> knowledgeTrees = result.getKnowledgeTrees();
        if (CollectionUtils.isNotEmpty(knowledgeTrees)) {
            List<KnowledgeVO> children = Lists.newArrayList();
            for (KnowledgeVO knowledgeTree : knowledgeTrees) {
                children.add(assertSubjectInfo(knowledgeTree, subjectMap));
            }
            result.setKnowledgeTrees(children);
        }
        return result;
    }

    /**
     * 全量查询知识点
     *
     * @return
     */
    @Override
    public List<Knowledge> findAll() {
        //存储知识点列表（无科目信息）
        String key = RedisKeyConstant.getKnowledgeList();
        List<Knowledge> knowledgeList = (List<Knowledge>) redisTemplate.opsForValue().get(key);
        if (CollectionUtils.isEmpty(knowledgeList)) {
            List<Knowledge> knowledges = selectAll();
            //数据不为空，放缓存(过期时间：5分钟)
            if (CollectionUtils.isNotEmpty(knowledges)) {
                redisTemplate.opsForValue().set(key, knowledges);
                redisTemplate.expire(key, 7, TimeUnit.DAYS);
            }
            return knowledges;
        }
        return knowledgeList;
    }

    /**
     * 清除缓存
     */
    public void clearRedis() {
        String key = RedisKeyConstant.getKnowledgeList();
        redisTemplate.delete(key);
    }

    /**
     * 查询某一节点下的所有叶子节点
     *
     * @param subjectId
     * @return
     */
    public String getKnowledgeInfo(long subjectId, Long parentId) {
        //根据科目和查询改科目及其上级节点上挂着的所有有效知识点
        List<Knowledge> knowledgeList = selectKnowledgeBySubject(subjectId);
        if (CollectionUtils.isEmpty(knowledgeList)) {
            return "";
        }
        knowledgeList = knowledgeList.stream().distinct().collect(Collectors.toList());
        List<Long> knowledgeIdList = new ArrayList<>();
        getKnowledgeId(knowledgeList, parentId, knowledgeIdList);
        knowledgeIdList.add(0, parentId);
        return knowledgeIdList.stream().map(knowledgeId -> String.valueOf(knowledgeId)).collect(Collectors.joining(","));
    }


    public void getKnowledgeId(List<Knowledge> knowledgeList, Long parentId, List<Long> knowledgeIdList) {
        List<Knowledge> list = knowledgeList.stream().filter(i -> i.getParentId().equals(parentId)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            knowledgeIdList.add(parentId);
        } else {
            knowledgeIdList.add(parentId);      //选中知识点下属的所有知识点都得列入查询范围（避免试题绑定的知识点为非底层知识点）
            for (Knowledge kn : list) {
                getKnowledgeId(knowledgeList, kn.getId(), knowledgeIdList);
            }
        }
    }

    @Override
    public List<Knowledge> getAllChildrenKnowledge(long subjectId, Long parentId) {
        //根据科目和查询改科目及其上级节点上挂着的所有有效知识点
        List<Knowledge> allKnowledgeList = selectKnowledgeBySubject(subjectId);
        if (CollectionUtils.isEmpty(allKnowledgeList)) {
            return Lists.newArrayList();
        }
        allKnowledgeList = allKnowledgeList.stream().distinct().collect(Collectors.toList());
        ArrayList<Knowledge> knowledgeList = Lists.newArrayList();
        findAllChildren(allKnowledgeList, parentId, knowledgeList);

        final Optional<Knowledge> optionalKnowledge = allKnowledgeList.stream()
                .filter(knowledge -> knowledge.getId().equals(parentId))
                .findAny();
        if (optionalKnowledge.isPresent()) {
            knowledgeList.add(0, optionalKnowledge.get());
        }
        return knowledgeList;
    }


    public void findAllChildren(List<Knowledge> knowledgeList, Long parentId, final List<Knowledge> knowledgeIdList) {
        List<Knowledge> list = knowledgeList.stream()
                .filter(knowledge -> knowledge.getParentId().equals(parentId))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(list)) {
            list.forEach(knowledge -> {
                knowledgeIdList.add(knowledge);
                findAllChildren(knowledgeList, knowledge.getId(), knowledgeIdList);
            });
        }
    }

    @Override
    public Long transKnowledgeId(Long knowledgeId, Long parentId) {
        Knowledge knowledge = selectByPrimaryKey(knowledgeId);
        if (null == knowledge) {
            Example example = Example.builder(Knowledge.class).build();
            example.and().andEqualTo("parentId", parentId);
            example.and().andLike("name", "其他%");//补偿成同级的 其他 信息
            List<Knowledge> knowledgeList = selectByExample(example);
            if (CollectionUtils.isNotEmpty(knowledgeList)) {
                return knowledgeList.get(0).getId();
            }
            return knowledgeId;
        }
        return knowledgeId;
    }

    @Override
    public List<Knowledge> findKnowledgeInfoByKnowIds(List<Long> knowledgeIds) {
        Example example = new Example(Knowledge.class);
        example.and().andIn("id", knowledgeIds);
        List<Knowledge> knowledges = selectByExample(example);
        return knowledges;
    }
}
