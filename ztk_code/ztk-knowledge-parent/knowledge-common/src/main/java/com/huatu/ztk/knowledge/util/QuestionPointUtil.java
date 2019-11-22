package com.huatu.ztk.knowledge.util;

import com.huatu.ztk.knowledge.bean.QuestionPoint;
import com.huatu.ztk.knowledge.bean.QuestionPointTree;
import com.huatu.ztk.knowledge.common.QuestionPointLevel;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by shaojieyue
 * Created time 2016-05-08 15:09
 */
public class QuestionPointUtil {

    /**
     * 将知识点组装为一个树
     *
     * @param points
     * @return
     */
    public static List<QuestionPointTree> wapper2Trees(Collection<QuestionPointTree> points) {
        List<QuestionPointTree> treeRootList = new ArrayList();
        Map<Integer, QuestionPointTree> levelOneMap = new HashMap();
        Map<Integer, QuestionPointTree> levelTwoMap = new HashMap();
        final QuestionPointTree[] pointTrees = points.stream().filter(point -> point != null).toArray(QuestionPointTree[]::new);

        for (QuestionPointTree questionPointTree : pointTrees) {
            if (questionPointTree.getLevel() != QuestionPointLevel.LEVEL_ONE) {
                //非一级节点,不处理
                continue;
            }

            levelOneMap.put(questionPointTree.getId(), questionPointTree);
            //添加到list，来保证树的顺序正确性
            treeRootList.add(questionPointTree);
        }
        //是否存在一级节点
        boolean levelOneExist = levelOneMap.size() > 0;

        for (QuestionPointTree questionPointTree : pointTrees) {//遍历获取二级节点
            if (questionPointTree.getLevel() != QuestionPointLevel.LEVEL_TWO) {
                //非二级节点,不处理
                continue;
            }

            if (!levelOneExist) {//说明一级节点不存在,那么二级节点作为根节点
                treeRootList.add(questionPointTree);
            } else if (levelOneMap.containsKey(questionPointTree.getParent())) {
                final QuestionPointTree levelOnePoint = levelOneMap.get(questionPointTree.getParent());
                //设置到所属一级节点
                levelOnePoint.getChildren().add(questionPointTree);
            }
            //存入两级节点map
            levelTwoMap.put(questionPointTree.getId(), questionPointTree);
        }
        //是否存在二级节点
        boolean levelTwoExist = levelTwoMap.size() > 0;
        for (QuestionPointTree questionPointTree : pointTrees) {//遍历获取三级节点
            if (questionPointTree.getLevel() != QuestionPointLevel.LEVEL_THREE) {
                //非三级节点,不处理
                continue;
            }

            if (!levelTwoExist) {//说明一级节点不存在,那么二级节点作为根节点
                treeRootList.add(questionPointTree);
            } else if (levelTwoMap.containsKey(questionPointTree.getParent())) {//二级节点
                final QuestionPointTree levelTwoPoint = levelTwoMap.get(questionPointTree.getParent());
                //设置到所属二级节点
                levelTwoPoint.getChildren().add(questionPointTree);
            }
        }
        return treeRootList;
    }

    /**
     * 将知识点组装为一个树（wapper2Trees 的备用方案--不再考虑知识点的层级）
     *
     * @param points
     * @return
     */
    public static List<QuestionPointTree> wrapper2Trees(Collection<QuestionPointTree> points) {
        if (CollectionUtils.isEmpty(points)) {
            return new ArrayList<>();
        }
        Map<Integer, QuestionPointTree> questionPointMap = points.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        List<QuestionPointTree> rootList = points.stream().filter(i -> !questionPointMap.containsKey(i.getParent())).collect(Collectors.toList());
        for (QuestionPointTree point : points) {
            if(questionPointMap.containsKey(point.getParent())){
                questionPointMap.get(point.getParent()).getChildren().add(point);
            }
        }
        return rootList;
    }

    public static List<QuestionPointTree> transform2Trees(Collection<QuestionPoint> points) {
        if (CollectionUtils.isEmpty(points)) {
            return new ArrayList<>();
        }

        List<QuestionPointTree> list = new ArrayList<QuestionPointTree>();
        for (QuestionPoint point : points) {
            list.add(QuestionPointUtil.conver2Tree(point));
        }
        return wapper2Trees(list);
    }

    /**
     * 转换为QuestionPointTree
     *
     * @param questionPoint
     * @return
     */
    public static QuestionPointTree conver2Tree(QuestionPoint questionPoint) {
        if (questionPoint == null) {
            return null;
        }

        final QuestionPointTree questionPointTree = QuestionPointTree.builder()
                .parent(questionPoint.getParent())
                .id(questionPoint.getId())
                .name(questionPoint.getName())
                .children(new ArrayList<QuestionPointTree>())
                .level(questionPoint.getLevel())
                .build();

        return questionPointTree;
    }

    /**
     * 获取所有的3级知识点id
     *
     * @param pointTree
     * @return
     */
    public static List<Integer> getPonintIds(List<QuestionPointTree> pointTree) {
        List<Integer> pointIds = new ArrayList();
        for (QuestionPointTree questionPointTree : getPointTrees(pointTree)) {
            pointIds.add(questionPointTree.getId());
        }
        return pointIds;
    }

    /**
     * 获取该树下所有的3级列表
     *
     * @param pointTree
     * @return
     */
    public static List<QuestionPointTree> getPointTrees(List<QuestionPointTree> pointTree) {
        List<QuestionPointTree> pointTrees = new ArrayList();

        for (QuestionPointTree questionPointTree : pointTree) {
            if (questionPointTree.getLevel() == QuestionPointLevel.LEVEL_THREE) {
                pointTrees.add(questionPointTree);
            } else {
                final List<QuestionPointTree> children = questionPointTree.getChildren();
                pointTrees.addAll(getPointTrees(children));
            }
        }
        return pointTrees;
    }
}
