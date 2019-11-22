package com.huatu.tiku.dataClean;

import com.google.common.collect.Lists;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.teacher.service.impl.knowledge.KnowledgeComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by lijun on 2019/1/5
 */
public class KnowledgeFilter extends TikuBaseTest {

    @Autowired
    private KnowledgeComponent knowledgeComponent;

    @Test
    public void test() {

        ArrayList<Long> knowledgeIds = Lists.newArrayList(65303L, 661L);

        Set<Long> set = knowledgeIds.stream()
                .flatMap(knowledgeId -> {
                    List<Knowledge> parentUtilRoot = knowledgeComponent.getParentUtilRoot(knowledgeId);
                    return parentUtilRoot.subList(1, parentUtilRoot.size())
                            .stream()
                            .map(Knowledge::getId);
                })
                .collect(Collectors.toSet());
        List<Long> newKnowledgeIdList = knowledgeIds.stream()
                .filter(knowledgeId -> !set.contains(knowledgeId))
                .collect(Collectors.toList());


        System.out.println(newKnowledgeIdList.size());
    }
}
