package com.huatu.tiku.teacher.service;

import com.huatu.tiku.TikuBaseTest;
import com.huatu.tiku.entity.duplicate.ObjectiveDuplicatePart;
import com.huatu.tiku.entity.duplicate.QuestionDuplicate;
import com.huatu.tiku.entity.question.BaseQuestion;
import com.huatu.tiku.entity.subject.Subject;
import com.huatu.tiku.teacher.dao.mongo.NewQuestionDao;
import com.huatu.tiku.teacher.service.duplicate.ObjectiveDuplicatePartService;
import com.huatu.tiku.teacher.service.duplicate.QuestionDuplicateService;
import com.huatu.tiku.teacher.service.question.v1.CommonQuestionServiceV1;
import com.huatu.tiku.teacher.service.subject.TeacherSubjectService;
import com.huatu.tiku.teacher.util.file.ExcelManageUtil;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.question.bean.GenericQuestion;
import com.huatu.ztk.question.bean.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by huangqingpeng on 2019/2/21.
 */
@Slf4j
public class QuestionDuplicateT extends TikuBaseTest {
    @Autowired
    private ObjectiveDuplicatePartService objectiveDuplicatePartService;

    @Autowired
    private QuestionDuplicateService questionDuplicateService;

    @Autowired
    private CommonQuestionServiceV1 commonQuestionServiceV1;

    @Autowired
    private TeacherSubjectService teacherSubjectService;

    @Autowired
    private NewQuestionDao questionDao;


    @Test
    public void test() {
        File file = new File("C:\\Users\\x6\\Desktop\\1.txt");
        List<QuestionInfo> list = Lists.newArrayList();
        try {
            String text = FileUtils.readFileToString(file);
            System.out.println("file = " + text);
            List<Long> ids = Arrays.stream(text.split(",")).map(Long::parseLong).collect(Collectors.toList());
            int index = 0;
            int size = 100;
            while (index < ids.size()) {
                int end = index + size > ids.size() ? ids.size() : index + size;
                System.out.println("ids.subList(index,end) = " + ids.subList(index, end));
                List<Long> temps = ids.subList(index, end);
                handlerQuestionInfo(list, temps);
                index = end;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<QuestionInfo> errors = list.stream().filter(i -> CollectionUtils.isNotEmpty(i.getErrorAttrs())).collect(Collectors.toList());
        List<QuestionInfo> rights = list.stream().filter(i -> CollectionUtils.isEmpty(i.getErrorAttrs())).collect(Collectors.toList());
        System.out.println("rights = " + rights.size());
        System.out.println("errors = " + errors.size());
        File file1 = new File("C:\\Users\\x6\\Desktop\\2.txt");
        try {
            FileUtils.writeStringToFile(file1, JsonUtil.toJson(errors));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Long> dupIds = errors.stream().map(QuestionInfo::getDuplicateId).collect(Collectors.toList());
        Example example = new Example(QuestionDuplicate.class);
        example.and().andIn("duplicateId", dupIds);
        List<QuestionDuplicate> questionDuplicates = questionDuplicateService.selectByExample(example);
        List<Long> questionIds = questionDuplicates.stream().map(QuestionDuplicate::getQuestionId).collect(Collectors.toList());
        List<BaseQuestion> questions = commonQuestionServiceV1.findByIds(questionIds);
        List<Subject> subjectList = teacherSubjectService.selectAll();
        Map<Long, String> subjectMap = subjectList.stream().filter(i -> i.getLevel().intValue() != 1).collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));
        List<List> resullt = Lists.newArrayList();
        List<Question> temps = questionDao.findByIds(questionIds.stream().map(Long::intValue).collect(Collectors.toList()));
        Map<Integer, String> knowledgeMap = temps.stream().collect(Collectors.toMap(i -> i.getId(), i -> {
            if (i instanceof GenericQuestion) {
                return ((GenericQuestion) i).getPointsName().get(0);
            }
            return i.getPointList().get(0).getPointsName().get(0);
        }));

        for (BaseQuestion question : questions) {
            Long id = question.getId();
            Optional<QuestionDuplicate> first = questionDuplicates.parallelStream().filter(i -> i.getQuestionId().equals(id)).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            QuestionDuplicate questionDuplicate = first.get();
            Optional<QuestionInfo> optional = list.stream().filter(i -> i.getDuplicateId().equals(questionDuplicate.getDuplicateId())).findFirst();
            if (optional.isPresent()) {
                ArrayList<String> list1 = Lists.newArrayList(question.getId().toString(), subjectMap.get(question.getSubjectId()),
                        optional.get().getErrorAttrs().stream().collect(Collectors.joining(",")),
                        knowledgeMap.get(question.getId().intValue()));
                resullt.add(list1);
            }
        }
        String[] title = new String[]{"试题ID", "科目ID", "图片位置", "知识点"};
        try {
            ExcelManageUtil.writer("C:\\Users\\x6\\Desktop\\", "试题图片问题", "xls", resullt, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlerQuestionInfo(List<QuestionInfo> list, List<Long> temps) {
        Example example = new Example(ObjectiveDuplicatePart.class);
        example.and().andIn("id", temps);
        List<ObjectiveDuplicatePart> objectiveDuplicateParts = objectiveDuplicatePartService.selectByExample(example);
        if (CollectionUtils.isNotEmpty(objectiveDuplicateParts)) {
            List<QuestionInfo> collect = objectiveDuplicateParts.stream().map(this::getQuestionInfo).collect(Collectors.toList());
            list.addAll(collect);
        }
    }

    private QuestionInfo getQuestionInfo(ObjectiveDuplicatePart objectiveDuplicatePart) {
        String stem = objectiveDuplicatePart.getStem();
        String analysis = objectiveDuplicatePart.getAnalysis();
        String extend = objectiveDuplicatePart.getExtend();
        String choices = objectiveDuplicatePart.getChoices();
        ArrayList<String> list = Lists.newArrayList();
        if (checkPng(stem)) {
            list.add("题干");
        }
        if (checkPng(analysis)) {
            list.add("解析");
        }
        if (checkPng(choices)) {
            list.add("选项");
        }
        if (checkPng(extend)) {
            list.add("拓展");
        }
        return QuestionInfo.builder().duplicateId(objectiveDuplicatePart.getId()).errorAttrs(list).build();
    }

    /**
     * @param content
     * @return false是没问题的意思
     */
    private boolean checkPng(String content) {
        if (content.indexOf("latex") > -1) {
            return false;
        }
        if (content.indexOf("..png") == -1) {
            return false;
        }
        Pattern pattern = Pattern.compile("<img[^>]+>");
        Matcher matcher = pattern.matcher(content);
        int index = 0;
        while (matcher.find(index)) {
            String group = matcher.group();
            boolean flag = checkWidth(group);
            index = matcher.end();
            System.out.println("flag=" + flag + ",group=" + group);
            if (!flag) {
                return true;
            }
        }
        return false;
    }

    private boolean checkWidth(String group) {
        int width = group.indexOf("width");
        if (width == -1) {
            return false;
        }
        try {
            int i = group.indexOf("width", width + 1);
            if (i > -1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Test
    public void test12(){
        ArrayList<Integer> ids = com.google.common.collect.Lists.newArrayList(30006057, 45120, 30006154, 268768, 45125, 262753, 45071, 266165, 30006299, 45074, 30006298, 45139, 30005917, 30006323, 30006485, 226495, 232450, 226567, 259782, 264461, 270861, 226568, 45518, 100494, 45137, 45073, 259735, 59860, 268764, 259736, 43806, 268763, 110430, 110433, 30006059, 244256, 236455, 236456, 268776, 78000, 264502, 66229, 266290, 259700, 66230, 45049, 266431, 271288, 46397, 226494, 44031, 45078, 261209, 261912, 261886, 95020, 43389, 113582, 265349, 235817, 264463, 218915, 233474, 43393, 236049, 46468, 236458, 95018, 46095, 43264, 44033, 265350, 233472, 259654, 46471, 266253, 259785, 259784, 259656, 266248, 100492, 45072, 260050, 259729, 268765, 259738, 259737, 263448, 263450, 260067, 262756, 260066, 218914, 61410, 268775, 44003, 259748, 78441, 268781, 95019, 66219, 66218, 99181, 236460, 264501, 45811, 264503, 259703, 45812, 45813, 45048, 268671, 261887, 264504, 228322, 218913, 45123, 268769, 226566, 259653, 113579, 266251, 266164, 236048, 260080, 259728, 268784, 261212, 43391, 228323, 259779, 268774, 226599, 262992, 268782, 268783, 226527, 78444, 263449, 271289, 45038, 45121, 110432, 45122, 45098, 77999, 45199, 45810, 45178, 259672, 45179, 45180, 45116, 45119, 261889, 236047, 270859, 233475, 259778, 45954, 259655, 45381, 268674, 45382, 45383, 44361, 262989, 100491, 44811, 44364, 45517, 44813, 260049, 45076, 45077, 45079, 46168, 259674, 43929, 44894, 45092, 259749, 268771, 260068, 236457, 78445, 45036, 260082, 266167, 259702, 30006399, 115902, 266432, 226598, 235819, 95021, 266249, 95057, 60563, 59859);
        System.out.println("ids = " + ids.size());
        List<Question> temps = questionDao.findByIds(ids);
        System.out.println("temps = " + temps.size());
        if(temps.size()!=ids.size()){
            ids.removeAll(temps.stream().map(Question::getId).distinct().collect(Collectors.toList()));
        }
        System.out.println("ids = " + ids.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    static class QuestionInfo {
        Long subjectId;
        Long questionId;
        Long duplicateId;
        List<String> errorAttrs;

    }
}
