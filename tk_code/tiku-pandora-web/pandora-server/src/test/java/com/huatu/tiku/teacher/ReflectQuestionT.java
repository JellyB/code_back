package com.huatu.tiku.teacher;

import com.google.common.collect.Maps;
import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.teacher.PaperActivity;
import com.huatu.tiku.entity.teacher.PaperEntity;
import com.huatu.tiku.entity.teacher.PaperQuestion;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.service.PaperActivityService;
import com.huatu.tiku.teacher.service.common.ImportService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.ReflectQuestion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/1/4.
 */
@Slf4j
public class ReflectQuestionT extends TikuBaseTest {

    @Autowired
    PaperQuestionService paperQuestionService;

    @Autowired
    ReflectQuestionDao reflectQuestionDao;


    @Autowired
    PaperActivityService paperActivityService;

    @Autowired
    PaperEntityService paperEntityService;

    @Autowired
    ImportService importService;

    @Autowired
    NewQuestionDao questionDao;


    /**
     * 处理试卷中需要删除的试题
     */
    @Test
    public void Test() throws IOException {
        List<ReflectQuestion> all = reflectQuestionDao.findAll();
        ArrayList<Map> list = Lists.newArrayList();
        for (ReflectQuestion reflectQuestion : all) {
            checkPaperQuestion(reflectQuestion, list);
        }
        if (CollectionUtils.isEmpty(list)) {
            System.out.println("没有要处理的");
            return;
        }
        Map<Object, List<Map>> paperType = list.stream().collect(Collectors.groupingBy(i -> i.get("paperType")));
        System.out.println("paperType = " + paperType.keySet());
        if (paperType.containsKey(1)) {
            List<Map> maps = paperType.get(1);
            Map<Object, List<Map>> paperIdMap = maps.stream().collect(Collectors.groupingBy(i -> i.get("paperId")));
            for (Map.Entry<Object, List<Map>> entity : paperIdMap.entrySet()) {
                handerPaperEntity(entity.getValue(), Long.parseLong(entity.getKey() + ""));
            }
        }
        if (paperType.containsKey(2)) {
            List<Map> maps = paperType.get(2);
            Map<Object, List<Map>> paperIdMap = maps.stream().collect(Collectors.groupingBy(i -> i.get("paperId")));
            for (Map.Entry<Object, List<Map>> entity : paperIdMap.entrySet()) {
                handerPaperActivity(entity.getValue(), Long.parseLong(entity.getKey() + ""));
            }
        }
        System.out.println("list = " + list);
        List<List> result = new ArrayList<>();

        for (Map map : list) {
            Integer newId = MapUtils.getInteger(map, "newId");
            List temp = Lists.newArrayList(MapUtils.getString(map, "paperType"),
                    MapUtils.getString(map, "paperName"),
                    MapUtils.getString(map, "sort"),
                    MapUtils.getInteger(map, "oldId"),
                    MapUtils.getInteger(map, "newId"),
                    MapUtils.getInteger(map, "paperId")
            );
            Question question = questionDao.findById(newId);
            if(null!= question &&question.getSubject() == 1 && question instanceof GenericQuestion){
                temp.add(((GenericQuestion) question).getPointsName().get(0));
                result.add(temp);
            }
        }
        Map<Integer, List<Map>> newIds = list.stream().collect(Collectors.groupingBy(i -> MapUtils.getInteger(i, "newId")));
        List<Integer> ids = newIds.keySet().stream().collect(Collectors.toList());
        List<ReflectQuestion> collect = all.stream().filter(i -> ids.contains(i.getNewId())).collect(Collectors.toList());
        for (ReflectQuestion reflectQuestion : collect) {
            PaperQuestion paperQuestion = PaperQuestion.builder().questionId(Long.parseLong(reflectQuestion.getNewId()+"")).build();
            Example example = new Example(PaperQuestion.class);
            example.and().andEqualTo("questionId",Long.parseLong(reflectQuestion.getOldId()+""));
            paperQuestionService.updateByExampleSelective(paperQuestion,example);
        }
        importService.sendQuestion2Mongo(ids);
        System.out.println("result = " + JsonUtil.toJson(result));
        File file = new File("C:\\Users\\x6\\Desktop\\2.txt");
        FileUtils.writeStringToFile(file,JsonUtil.toJson(result));
    }

    private void handerPaperEntity(List<Map> value, Long key) {
        PaperEntity paperEntity = paperEntityService.selectByPrimaryKey(key);
        if (null == paperEntity) {
            return;
        }
        for (Map map : value) {
            handerPaperEntity(map, paperEntity);
        }
    }

    private void handerPaperActivity(List<Map> maps, Long paperId) {
        PaperActivity paperActivity = paperActivityService.selectByPrimaryKey(paperId);
        if (null == paperActivity) {
            return;
        }
        for (Map map : maps) {
            map.put("paperType", "活动卷");
            map.put("paperName", paperActivity.getName());
        }
    }


    private void handerPaperEntity(Map map, PaperEntity paperEntity) {
        map.put("paperType", "实体卷");
        map.put("paperName", paperEntity.getName());
    }

    /**
     * 判断是是否是需要替换的试卷试题关系
     *
     * @param reflectQuestion
     * @param list
     */
    private void checkPaperQuestion(ReflectQuestion reflectQuestion, ArrayList<Map> list) {
        Long newId = new Long(reflectQuestion.getNewId());
        Long oldId = new Long(reflectQuestion.getOldId());
        List<PaperQuestion> tempLists = paperQuestionService.findByQuestionId(oldId);
        if (CollectionUtils.isEmpty(tempLists)) {
            return;
        }
        for (PaperQuestion temp : tempLists) {
            list.add(getQuetionInfo(temp, newId, oldId));
        }

    }

    private Map getQuetionInfo(PaperQuestion temp, Long newId, Long oldId) {
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("newId", newId);
        map.put("oldId", oldId);
        map.put("paperId", temp.getPaperId());
        map.put("sort", temp.getSort());
        map.put("paperType", temp.getPaperType());
        return map;
    }

}
