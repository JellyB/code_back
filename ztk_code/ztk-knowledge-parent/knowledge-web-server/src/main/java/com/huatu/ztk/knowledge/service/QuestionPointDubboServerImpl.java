package com.huatu.ztk.knowledge.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.Module;
import com.huatu.ztk.knowledge.api.ModuleDubboService;
import com.huatu.ztk.knowledge.api.QuestionPointDubboService;
import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.common.PointStatus;
import com.huatu.ztk.knowledge.common.QuestionPointLevel;
import com.huatu.ztk.knowledge.constant.CustomizeEnum;
import com.huatu.ztk.knowledge.util.DebugCacheUtil;
import com.huatu.ztk.knowledge.util.QuestionPointUtil;
import com.huatu.ztk.question.api.QuestionDubboService;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionCorrectType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 知识点dubbo server
 * Created by shaojieyue
 * Created time 2016-05-08 13:45
 */


@Service
public class QuestionPointDubboServerImpl implements QuestionPointDubboService {

    public static final Logger logger = LoggerFactory.getLogger(QuestionPointDubboServerImpl.class);

    @Autowired
    private QuestionDubboService questionDubboService;

    @Autowired
    private QuestionPointService questionPointService;

    @Autowired
    private ModuleDubboService moduleDubboService;
    
    @Autowired
    private PoxyUtilService poxyUtilService;

    //科目对应的知识点数量缓存
    private static final Cache<Integer, Integer> subjectPointCountCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.DAYS)//缓存时间
                    .maximumSize(100)
                    .build();


    /**
     * 知识点统计
     *
     * @param questions 试题列表
     * @param corrects  答题是否正确
     * @param times     答题时间
     *                  has not sub ids@return
     */
    @Override
    public List<QuestionPointTree> questionPointSummary(List<Integer> questions, int[] corrects, int[] times) {
        return questionPointSummary(questions, corrects, times, true);
    }

    @Override
    public List<QuestionPointTree> questionPointSummaryWithTotalNumber(List<Integer> questions, int[] corrects, int[] times) {
        return questionPointSummary(questions, corrects, times, false);
    }


    /**
     * 知识点统计
     *
     * @param questions 试题列表
     * @param corrects  答题是否正确
     * @param times     答题时间
     * @param totalType 获取错误信答题正确率时 计算总数的基数情况
     * @return update by lijun 2018-03-27
     * 此处代码原本为原本接口,此处为了区分不同情况下 知识点的正确率计算情况 重载了此方法
     */
    public List<QuestionPointTree> questionPointSummary(List<Integer> questions, int[] corrects, int[] times, boolean totalType) {
        if (null == questions) {
            return new ArrayList<>();
        }
        final List<Question> bath = questionDubboService.findBath(questions);
        Map<Integer, QuestionPointTree> data = new HashMap<>();
        if (bath != null) {
            for (int i = 0; i < bath.size(); i++) {
                Question question = bath.get(i);
                if (question == null || !(question instanceof GenericQuestion)) {//理论上是不存在的
//                logger.error("questionId={} not exist.",questions.get(i));
                    continue;
                }

                GenericQuestion genericQuestion = (GenericQuestion) question;
                if (null == genericQuestion || CollectionUtils.isEmpty(genericQuestion.getPoints())) {
                    logger.info("试题缺少知识点>>>>>,试题ID = {}", genericQuestion.getId());
                    continue;
                }
                final List<Integer> points = genericQuestion.getPoints();
                for (Integer point : points) {
                    QuestionPointTree questionPointTree = data.get(point);
                    if (questionPointTree == null) {
                        final QuestionPoint questionPoint = findById(point);
                        if (questionPoint == null) {//知识点没有查询到
                            continue;//不进行处理
                        }
                        questionPointTree = QuestionPointUtil.conver2Tree(questionPoint);
                        questionPointTree.setQnum(0);//初始化题数，防止conver2Tree里面设置qnum
                        //写入map
                        data.put(questionPointTree.getId(), questionPointTree);
                    }

                    if (questionPointTree == null) {//找不到对应的知识点，则处理,理论上不存在此情况
                        logger.error("can`t find parent knowledge point. pointId={}", point);
                        continue;
                    }

                    int currect = corrects[i];
                    questionPointTree.setQnum(questionPointTree.getQnum() + 1);
                    questionPointTree.setTimes(questionPointTree.getTimes() + times[i]);//设置试题所花时间
                    if (QuestionCorrectType.RIGHT == currect) {//答题正确
                        questionPointTree.setRnum(questionPointTree.getRnum() + 1);
                    } else if (QuestionCorrectType.WRONG == currect) {//答题错误
                        questionPointTree.setWnum(questionPointTree.getWnum() + 1);
                    } else if (QuestionCorrectType.UNDO == currect) {//没有作答
                        questionPointTree.setUnum(questionPointTree.getUnum() + 1);
                    } else {//非法的答案视为错误的
                        questionPointTree.setWnum(questionPointTree.getWnum() + 1);
                        logger.error("illegal knowledge answer status,status={},questionId={}", currect, question.getId());
                    }
                }
            }
        }
        for (QuestionPointTree questionPointTree : data.values()) {//遍历知识点列表，计算平均时间

            /**
             * 此处处理计算正确率时候 答题总数量问题
             * (1）专项练习知识树上显示的正确率=答对数量/已答数量
             *（2）抽题或套题报告里显示的正确率=答对数量/（已答数量+未答数量）
             *
             * add by lijun 2018-03-27
             */
            int questionNum;
            if (totalType) { //系统中的原始情况
                //已经作答的题数
                questionNum = questionPointTree.getRnum() + questionPointTree.getWnum();
            } else {
                questionNum = questionPointTree.getRnum() + questionPointTree.getWnum() + questionPointTree.getUnum();
            }
            /**
             * end
             */
            int speed = 0;
            double accuracy = 0;
            if (questionNum > 0) {
                speed = questionPointTree.getTimes() / questionNum;//计算平均时间
                //正确率
                accuracy = new BigDecimal(questionPointTree.getRnum() * 100).divide(new BigDecimal(questionNum), 1, RoundingMode.HALF_UP).doubleValue();
            }
            questionPointTree.setSpeed(speed);
            questionPointTree.setAccuracy(accuracy);
        }

        return QuestionPointUtil.wapper2Trees(data.values());
    }

    /**
     * 组装知识点树
     *
     * @param pointId   知识点
     * @param recursive 是否递归查询知识点
     * @return
     */
    public List<QuestionPointTree> findPointTree(final int pointId, boolean recursive) {
        final QuestionPoint questionPoint = findById(pointId);
        if (questionPoint == null) {
            logger.error("can`t found point,pointId={}", pointId);
            return new ArrayList<QuestionPointTree>();
        }
        List<QuestionPoint> questionPoints = new ArrayList<QuestionPoint>();
        questionPoints.add(questionPoint);
        if (recursive) {//需要递归处理其子节点
            if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_ONE) {
                final List<QuestionPoint> levelTwos = findChildren(pointId);
                questionPoints.addAll(levelTwos);
                for (QuestionPoint levelTwo : levelTwos) {
                    questionPoints.addAll(findChildren(levelTwo.getId()));
                }
            } else if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_TWO) {
                questionPoints.addAll(findChildren(questionPoint.getId()));
            } else if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_THREE) {
                //第三级不用处理
            } else {
                logger.error("valid pointId,point level error. point={}", JsonUtil.toJson(questionPoint));
            }
        }
        return QuestionPointUtil.transform2Trees(questionPoints);
    }

    /**
     * 随机获取知识点,智能出题用到，暂时返回null
     *
     * @return 随机知识点
     */
    @Override
    public QuestionPoint randomPoint() {
        QuestionPoint questionPoint = findById(394);
        return questionPoint;
    }

    /**
     * 从一个知识点下边,随机获取指定个数的3级知识点
     *
     * @param point 知识点
     * @param count 获取随机知识点个数
     * @return
     */
    @Override
    public List<QuestionPoint> randomPoint(int point, int count) {
        List<QuestionPoint> results = new ArrayList<>();
        final QuestionPoint questionPoint = findById(point);
        if (questionPoint == null) {
            return results;
        }
        if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_THREE) {
            results.add(questionPoint);
            return results;
        }

        if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_TWO) {//二级列表
            //批量查询,需要去掉已经删除掉的知识点
            results = findBath(questionPoint.getChildren()).stream().filter(point1 -> point1.getStatus() != PointStatus.DELETED).collect(Collectors.toList());
        }

        if (questionPoint.getLevel() == QuestionPointLevel.LEVEL_ONE) {//一级节点
            //批量查询 此处是二级节点
            results = findBath(questionPoint.getChildren()).stream().filter(point1 -> point1.getStatus() != PointStatus.DELETED).collect(Collectors.toList());
            List<QuestionPoint> tmp = new ArrayList<>();
            for (QuestionPoint result : results) {
                //此处是3级节点,需要去掉已经删除掉的知识点
                final List<QuestionPoint> questionPoints = findBath(result.getChildren()).stream().filter(point1 -> point1.getStatus() != PointStatus.DELETED).collect(Collectors.toList());
                tmp.addAll(questionPoints);
            }
            results = tmp;
        }

        //重新打乱顺序,达到随机的目的
        Collections.shuffle(results);

        //此处只取3倍的题,防止下边做过多的知识点检查,提高随机试题的效率
        final int toIndex = Math.min(count * 3, results.size());
        results = results.subList(0, toIndex);


        //按照比例检查知识点的题量,防止抽题时,题的重复率高
        final int seed = results.size() / count;
        if (seed > 1) {//只有>1才处理, =1说明知识点很少,无需清除题量少的知识点,防止抽题不够
            results.removeIf(obj -> {
                //随机来判定知识点是否需要检查其试题个数(题数太少容易造成抽题重复)
                if (RandomUtils.nextInt(0, 100) % seed != 0) {
                    int size = poxyUtilService.getQuestionPointService().count(obj.getId());
                    if (size < 30) {//小于固定题数的话,则不加入抽题列表
                        return true;
                    }
                }
                return false;
            });
        }

        if (results.size() <= count) {//直接返回
            return Lists.newArrayList(results);
        }
        return Lists.newArrayList(results.subList(0, count));
    }

    /**
     * 批量查询知识点
     *
     * @param children
     * @return
     */
    @Override
    public List<QuestionPoint> findBath(List<Integer> children) {
        List<QuestionPoint> results = new ArrayList<>();
        if (CollectionUtils.isEmpty(children)) {//集合为空,直接返回
            return results;
        }

        for (Integer child : children) {
            final QuestionPoint questionPoint = findById(child);
            if (questionPoint != null) {
                results.add(questionPoint);
            }
        }
        return results;
    }

    /**
     * 查询用户已经做过的知识点
     *
     * @param uid     用户id
     * @param subject 科目
     * @return 返回用户已经做过知识点集合
     */
    @Override
    public Set<Integer> findUserPoints(long uid, int subject) {
        return questionPointService.findUserPoints(uid, subject);
    }

    /**
     * 查询用户知识点数量
     * 里面包含了所有级别的节点
     *
     * @param subject 科目
     * @return
     */
    @Override
    public int findPointsCount(int subject) {
        DebugCacheUtil.showCacheContent(subjectPointCountCache, "subjectPointCountCache");
        //从本地取
        Integer pointCount = subjectPointCountCache.getIfPresent(subject);
        //不存在从数据库取
        if (pointCount == null || pointCount < 1) {
            final List<Module> modules = moduleDubboService.findSubjectModules(subject);
            pointCount = 0;
            for (Module module : modules) {
                final QuestionPoint questionPoint = findById(module.getId());
                //查询二级节点
                final List<QuestionPoint> children = findChildren(questionPoint.getId()).stream()
                        .filter(point -> point.getStatus() == PointStatus.AUDIT_SUCCESS)
                        .collect(Collectors.toList());
                //查询3级节点
                for (QuestionPoint child : children) {
                    //只计算审核通过的知识点
                    final List<QuestionPoint> threeChildren = findChildren(child.getId()).stream()
                            .filter(point -> point.getStatus() == PointStatus.AUDIT_SUCCESS)
                            .collect(Collectors.toList());
                    pointCount = pointCount + threeChildren.size();
                }
            }
            //缓存到本地
            subjectPointCountCache.put(subject, pointCount);
        }
        return pointCount;
    }


    /**
     * 查询一个知识点的子节点
     *
     * @param pointId
     * @return
     */
    public List<QuestionPoint> findChildren(final int pointId) {
        final QuestionPoint parent = findById(pointId);
        if (parent == null) {
            return new ArrayList<>(0);
        }
        return findBath(parent.getChildren());
    }

    /**
     * 查询父类节点
     *
     * @param pointId 父类节点信息
     * @return
     */
    public List<QuestionPoint> findParent(final int pointId) {
        List<QuestionPoint> idList = Lists.newArrayList();
        QuestionPoint threadById = findById(pointId);
        if (null != threadById) {
            idList.add(threadById);
            QuestionPoint twoById = findById(threadById.getParent());
            if (null != twoById) {
                idList.add(twoById);
                idList.add(findById(twoById.getParent()));
            }
        }
        return idList;
    }

    /**
     * 通过id查询知识点
     *
     * @param pointId 知识点id
     * @return
     */
    public QuestionPoint findById(final int pointId) {
        return poxyUtilService.getQuestionPointService().findById(pointId);
    }

    @Override
    public List<QuestionPoint> findDayTrainPoints(long userId, int subject, int size) {
        List<QuestionPointTree> oneLevels = questionPointService.questionPointTree(userId, subject, CustomizeEnum.ModeEnum.Write);
        List<QuestionPointTree> threeLevels = new ArrayList<>();
        for (QuestionPointTree pointTree : oneLevels) {
            List<QuestionPointTree> twoLevles = pointTree.getChildren();
            for (QuestionPointTree twoLevle : twoLevles) {
                List<QuestionPointTree> subPoints = twoLevle.getChildren();
                for (QuestionPointTree subPointTree : subPoints) {
                    //每日特训的知识点下必须保证起码有5道题
                    //update by lijun 修改节点数量的判断规则
//                    String pointQuestionKey = RedisKnowledgeKeys.getPointQuesionIds(subPointTree.getId());
//                    Long totalData = redisTemplate.opsForSet().size(pointQuestionKey);
                    int totalData = poxyUtilService.getQuestionPointService().count(subPointTree.getId());
                    if (totalData >= 5) {
                        threeLevels.add(subPointTree);
                    }
                    //finished
//                    if(subPointTree.getQnum()>=5){
//                        threeLevels.add(subPointTree);
//                    }
                }
            }
        }
        //按正确率排序
        threeLevels.sort(Comparator.comparing(QuestionPointTree::getAccuracy));
        //取前一半
        List<QuestionPointTree> tmpPointTrees = threeLevels.subList(0, threeLevels.size() / 2);
        Collections.shuffle(tmpPointTrees);
        List<Integer> pointIds = tmpPointTrees.subList(0, Math.min(size, tmpPointTrees.size()))
                .stream()
                .map(QuestionPointTree::getId).collect(Collectors.toList());

        return findBath(pointIds);
    }
}
