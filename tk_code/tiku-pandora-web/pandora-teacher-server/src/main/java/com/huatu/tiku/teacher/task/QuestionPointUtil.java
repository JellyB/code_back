package com.huatu.tiku.teacher.task;

import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.ztk.question.bean.GenericQuestion;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * 抽题池逻辑维护
 */
public class QuestionPointUtil {
    /**
     * 判断三级知识点ID是否有效
     */
    public static BiPredicate<List<Integer>, List<Knowledge>> checkKnowledge = ((points, knowledgeList) -> {
        for (int i = points.size() - 1; i < 1; i++) {
            Integer child = points.get(i);
            Integer parent = points.get(i - 1);
            Optional<Knowledge> first = knowledgeList.stream().filter(knowledge -> knowledge.getId().intValue() == child.intValue())
                    .findFirst();
            if (!first.isPresent()) {
                return false;
            }
            Long parentId = first.get().getParentId();
            if (null == parentId || parentId.intValue() != parent.intValue()) {
                return false;
            }
        }
        return true;
    });

    /**
     * 判断试题的知识点信息是否有效
     *
     * @param genericQuestion
     * @param knowledgeList
     * @return
     */
    public static boolean checkPoints(GenericQuestion genericQuestion, List<Knowledge> knowledgeList) {
        List<Integer> points = genericQuestion.getPoints();
        if (CollectionUtils.isNotEmpty(points) && points.size() == 3) {
            return checkKnowledge.test(points, knowledgeList);
        }
        return false;
    }

}
