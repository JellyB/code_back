package com.huatu.tiku.teacher.service;

import com.google.common.collect.Maps;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.dto.KnowledgeVO;
import com.huatu.tiku.entity.knowledge.Knowledge;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.common.QuestionMetaService;
import com.huatu.tiku.teacher.service.knowledge.KnowledgeService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hwpf.model.FibBase;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.security.CodeSigner;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MetaServiceTest extends TikuBaseTest {

    @Autowired
    private QuestionMetaService questionMetaService;

    @Autowired
    private NewQuestionDao questionDao;

    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;

    @Autowired
    KnowledgeService knowledgeService;

    @Autowired
    ImportService importService;

    @Test
    public void test() {
        final Map<Integer, List<Integer>> map = Maps.newHashMap();
        final Map<Integer, String> nameMap = Maps.newHashMap();
        Consumer<List<Question>> getList = (questions -> {
            int size = 0;

            for (Question question : questions) {
                if (question instanceof GenericQuestion) {
                    List<Integer> points = ((GenericQuestion) question).getPoints();
                    List<String> pointsName = ((GenericQuestion) question).getPointsName();
                    if (CollectionUtils.isEmpty(points)) {
                        continue;
                    }
                    Integer point = points.get(0);
                    String s = nameMap.get(point);
                    if (null == s) {
                        nameMap.put(point, pointsName.get(0));
                    }
                    handlerQuestion(question, map, point);
                    if (null != map && !map.isEmpty()) {
                        size = map.values().stream().mapToInt(i -> i.size()).min().getAsInt();
                        System.out.println("keyCount="+map.size()+"||size="+size);
                        if (size == 50 && map.size() >= 5) {
                            break;
                        }
                    }
                }
            }
            if (size < 50) {
                return;
            }
            for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
                Integer point = entry.getKey();
                List<Integer> value = entry.getValue();
                System.out.println("point = " + nameMap.get(point) + "size = " + value.size());
                System.out.println("value = " + value.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
        });
        commonQuestionServiceV1.findAndHandlerQuestion(getList, 1);
    }

    @Test
    public void test45(){
        commonQuestionServiceV1.findAndHandlerQuestion(questions -> {
                    questions.parallelStream().forEach(question -> {
                        int id = question.getId();
                        importService.sendQuestion2SearchForDuplicate(new Long(id));
                        System.out.println("发送ES 试题ID是：{}" + id);
                    });
                }, -1
        );
    }

    private void handlerQuestion(Question question, Map<Integer, List<Integer>> map, Integer point) {
        if (question instanceof GenericQuestion) {
            List<Integer> ids = map.getOrDefault(point, Lists.newArrayList());
            if (CollectionUtils.isNotEmpty(ids) && ids.size() > 50) {
                return;
            }
            if (question.getMode() != 1) {
                return;
            }
            if (question.getYear() <= 2014) {
                return;
            }
            QuestionMeta meta = questionMetaService.findMeta((GenericQuestion) question);
            if (meta.getCount() <= 100) {
                return;
            }
            int percent = meta.getPercents()[meta.getRindex()];
            if (percent > 40) {
                return;
            }
            ids.add(question.getId());
            map.put(point, ids);
        }
    }

    @Test
    public void test2() throws IOException {
        File file = new File("/Users/huangqingpeng/Documents/1.txt");
        String s = FileUtils.readFileToString(file);
        List<Integer> collect = Arrays.stream(s.split(",")).filter(NumberUtils::isDigits)
                .map(Integer::parseInt)
                .distinct()
                .collect(Collectors.toList());
//        importService.sendQuestion2Mongo(collect);
//        for (Integer questionId : collect) {
//            importService.sendQuestion2SearchForDuplicate(questionId.longValue());
//        }
    }
}
