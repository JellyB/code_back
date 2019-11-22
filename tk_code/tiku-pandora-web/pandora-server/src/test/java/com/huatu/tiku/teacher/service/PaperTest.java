package com.huatu.tiku.teacher.service;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.common.Area;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.teacher.*;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.enums.QuestionElementEnum;
import com.huatu.tiku.teacher.dao.mongo.OldPaperDao;
import com.huatu.tiku.teacher.dao.mongo.ReflectQuestionDao;
import com.huatu.tiku.teacher.service.common.AreaService;
import com.huatu.tiku.teacher.service.download.v1.WordWriteServiceV1;
import com.huatu.tiku.teacher.service.paper.PaperAreaService;
import com.huatu.tiku.teacher.service.paper.PaperEntityService;
import com.huatu.tiku.teacher.service.paper.PaperQuestionService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperStatus;
import com.huatu.ztk.question.bean.ReflectQuestion;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2018/12/10.
 */
public class PaperTest extends TikuBaseTest {

    @Autowired
    PaperEntityService paperEntityService;

    @Autowired
    PaperQuestionService paperQuestionService;

    @Autowired
    CommonQuestionServiceV1 commonQuestionServiceV1;

    @Autowired
    PaperAreaService paperAreaService;

    @Autowired
    AreaService areaService;

    @Autowired
    WordWriteServiceV1 wordWriteServiceV1;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    PaperActivityService paperActivityService;

    @Autowired
    OldPaperDao oldPaperDao;

    @Autowired
    ReflectQuestionDao reflectQuestionDao;

    @Test
    public void test() {
        ArrayList<Long> longs = Lists.newArrayList(2L, 3L, 24L);
        List<PaperEntity> paperEntities = paperEntityService.selectAll();
        List<PaperEntity> papers = paperEntities.stream().filter(i -> i.getStatus().intValue() == 1)
                .filter(i -> longs.contains(i.getSubjectId()))
                .collect(Collectors.toList());
        System.out.println("papers.size() = " + papers.size());
        List<List> list = Lists.newArrayList();
        for (PaperEntity paper : papers) {
            Map<Long, Integer> questionMap = findQuestionSubjectByPaper(paper);
            if (null == questionMap) {
                continue;
            }
            for (Map.Entry<Long, Integer> entry : questionMap.entrySet()) {
                ArrayList<Object> temp = Lists.newArrayList();
                temp.add(paper.getId());
                temp.add(paper.getName());
                temp.add(findSubject(paper.getSubjectId()));
                temp.add(findSubject(entry.getKey()));
                temp.add(entry.getValue());
                list.add(temp);
            }
        }
        System.out.println("JsonUtil.toJson(list) = " + JsonUtil.toJson(list));
    }

    private Object findSubject(Long subjectId) {
        switch (subjectId.intValue()) {
            case 2:
                return "公基";
            case 3:
                return "职测";
            case 24:
                return "综合应用";
            default:
                return "其他";
        }
    }

    private Map<Long, Integer> findQuestionSubjectByPaper(PaperEntity paper) {
        Long id = paper.getId();
        Long subjectId = paper.getSubjectId();
        List<PaperQuestion> paperQuestions = paperQuestionService.findByPaperIdAndType(id, PaperInfoEnum.TypeInfo.ENTITY);
        if (CollectionUtils.isEmpty(paperQuestions)) {
            return null;
        }
        List<Long> questionIds = paperQuestions.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());
        List<BaseQuestion> questions = commonQuestionServiceV1.findByIds(questionIds);
        Map<Long, List<BaseQuestion>> questionMap = questions.stream().collect(Collectors.groupingBy(BaseQuestion::getSubjectId));
        Map<Long, Integer> result = questionMap.entrySet().stream().collect(Collectors.toMap(i -> i.getKey(), i -> i.getValue().size()));
        if (result.size() == 1 && result.containsKey(subjectId)) {
            System.out.println("paperId = " + id + "科目一致");
            return null;
        }
        System.out.println("paperId = " + id + "科目不一致");
        return result;
    }

    @Test
    public void testDownload() throws BizException {
        String key = "duplicate_set";
        redisTemplate.delete(key);
        List<PaperEntity> paperEntities = paperEntityService.selectAll();
        List<PaperEntity> papers = paperEntities.stream().filter(i -> i.getSubjectId().intValue() == 1)
                .filter(i -> i.getMode().intValue() == 1)
                .collect(Collectors.toList());
        Set<String> set = Sets.newHashSet();
        ArrayList<List> errorModuleList = Lists.newArrayList();
        ArrayList<String> errorNames = Lists.newArrayList("言语理解和表达", "判断理解", "科学推理", "逻辑判断", "图形推理", "数字推理", "数学运算");
        papers.stream().forEach(i -> {
            String module = i.getModule();
            for (String errorName : errorNames) {
                if (module.indexOf(errorName) > -1) {
                    errorModuleList.add(Lists.newArrayList(errorName, i.getName()));
                }
            }
            List<PaperModuleInfo> list = JsonUtil.toList(module, PaperModuleInfo.class);
            if (CollectionUtils.isNotEmpty(list)) {
                set.addAll(list.stream().map(PaperModuleInfo::getName).map(name->{
                    String[] split = name.split(" ");
                    return split[split.length-1];
                }).collect(Collectors.toList()));
            }
        });
//        if (true) {
//            System.out.println("set = " + set);
//            System.out.println("errorModuleList = " + JsonUtil.toJson(errorModuleList));
//            return;
//        }
        papers.stream().forEach(i -> {
            List<PaperArea> list = paperAreaService.list(i.getId(), PaperInfoEnum.TypeInfo.ENTITY);
            if (CollectionUtils.isEmpty(list)) {
                i.setAreaIds("-1");
            } else {
                i.setAreaIds(list.get(0).getAreaId() + "");
            }
        });
        System.out.println("papers.stream().filter() = " + papers.stream().filter(i -> StringUtils.isBlank(i.getAreaIds())).map(i -> i.getName()).collect(Collectors.toList()));
        Map<String, List<PaperEntity>> paperAreaMap = papers.stream().filter(i -> StringUtils.isNotBlank(i.getAreaIds())).collect(Collectors.groupingBy(i -> i.getAreaIds()));
        System.out.println("paperAreaMap = " + paperAreaMap.keySet());
        List<List> list = Lists.newArrayList();
        List<Area> areas = areaService.findByIds(paperAreaMap.keySet().stream().map(Long::parseLong).collect(Collectors.toList()));
        Map<Long, String> areaMap = areas.stream().collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));
        System.out.println("共有地区数量 = " + paperAreaMap.size());
        int count = 1;
        for (Map.Entry<String, List<PaperEntity>> entry : paperAreaMap.entrySet()) {
            System.out.println("areaId = " + entry.getKey());
            System.out.println("ids = " + entry.getValue());
            System.out.println("第"+count+"个地区开始导出&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
//            if (null == areaMap.get(Long.parseLong(entry.getKey())) || areaMap.get(Long.parseLong(entry.getKey())).indexOf("江西") == -1) {
//                continue;
//            }
            handlerPaperDownload(entry, list, areaMap.get(Long.parseLong(entry.getKey())),set);
            count ++;
        }
        System.out.println("resut = " + JsonUtil.toJson(list));
        try {
            FileUtils.writeStringToFile(new File("C:\\Users\\x6\\Desktop\\1.txt"), JsonUtil.toJson(list), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlerPaperDownload(Map.Entry<String, List<PaperEntity>> entryList, List<List> table, String areaName, Set<String> set) throws BizException {
        List<PaperEntity> papers = entryList.getValue();
        List<String> moduleNames = Lists.newArrayList("常识判断", "言语理解与表达", "数量关系", "判断推理", "资料分析");
//        List<String> moduleNames = Lists.newArrayList( "数量关系");
        System.out.println("模块信息 " + set);
        for (String moduleName : set) {
            System.out.println("开始处理"+moduleName+"++++++++++++++++++++");
            String url = wordWriteServiceV1.downLoadList(papers.stream().map(PaperEntity::getId).collect(Collectors.toList()),
                    PaperInfoEnum.TypeInfo.ENTITY, QuestionElementEnum.QuestionFieldEnum.COMMON, moduleName, true);
            if(StringUtils.isNotBlank(url)){
                ArrayList<String> row = Lists.newArrayList(areaName, moduleName, url);
                table.add(row);
            }
        }

    }

    /**
     * 查询mysql和mongo试卷关联试题有区别的试卷
     */
    @Test
    public void testPaperQuestion() {
        List<PaperActivity> activities = paperActivityService.selectAll();
        Map<Integer, Long> paperMap = activities.stream().collect(Collectors.toMap(i -> i.getId().intValue(), i -> i.getPaperId()));
        List<Paper> papers = oldPaperDao.findAll();
        List<List> list = Lists.newArrayList();
        for (Paper paper : papers) {
            if (paper.getStatus() == PaperStatus.DELETED) {
                continue;
            }
            int id = paper.getId();
            Long paperId = paperMap.get(id);
            if (null == paperMap.get(id)) {
                continue;
            }
            List<PaperQuestion> questions = paperQuestionService.findByPaperIdAndType(paperId, PaperInfoEnum.TypeInfo.ENTITY);
            boolean b = handlerPaperDict(questions, paper.getQuestions());
            if(!b){
                ArrayList<String> temp = Lists.newArrayList(paper.getId() + "", paper.getName(),paper.getStatus()+"",
                        paper.getCatgory()+"",paper.getType()+"");
                System.out.println("试卷信息为:"+ JsonUtil.toJson(temp));
                list.add(temp);
            }
        }
        System.out.println("list = " + JsonUtil.toJson(list));
    }

    private boolean handlerPaperDict(List<PaperQuestion> questions, List<Integer> ids) {
        if (CollectionUtils.isEmpty(questions)) {
            if (CollectionUtils.isEmpty(ids)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (CollectionUtils.isEmpty(ids)) {
                return false;
            } else if (questions.size() != ids.size()) {
                return false;
            } else {
                for (int i = 0; i < questions.size(); i++) {
                    PaperQuestion paperQuestion = questions.get(i);
                    if (paperQuestion.getQuestionId().intValue() != ids.get(i).intValue()) {
                        if(handlerReflect(paperQuestion.getQuestionId().intValue(),ids.get(i).intValue())){
                            continue;
                        }
                        System.out.println("第"+(i+1)+"道题的题目ID对应不上……"+paperQuestion.getQuestionId()+"_____"+ids.get(i));
                        return false;
                    }
                }
                return true;
            }
        }
    }

    private boolean handlerReflect(int oldId, int newId) {
        ReflectQuestion reflectQuestion = reflectQuestionDao.findById(oldId);
        if(null == reflectQuestion){
            return false;
        }
        if(reflectQuestion.getNewId().intValue() == newId){
            return true;
        }
        System.out.println("reflectQuestion.getNewId().intValue() = " + reflectQuestion.getNewId().intValue());
        System.out.println("newId = " + newId);
        return  false;
    }
}
