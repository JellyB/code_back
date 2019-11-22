package com.huatu.tiku.schedule.biz.service.imple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.schedule.base.exception.BadRequestException;
import com.huatu.tiku.schedule.base.service.impl.BaseServiceImpl;
import com.huatu.tiku.schedule.biz.domain.ClassHourFeedback;
import com.huatu.tiku.schedule.biz.domain.ClassHourInfo;
import com.huatu.tiku.schedule.biz.domain.Teacher;
import com.huatu.tiku.schedule.biz.dto.CreatFeedBackDto;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import com.huatu.tiku.schedule.biz.enums.FeedbackStatus;
import com.huatu.tiku.schedule.biz.repository.ClassHourFeedbackRepository;
import com.huatu.tiku.schedule.biz.repository.ClassHourInfoRepository;
import com.huatu.tiku.schedule.biz.repository.TeacherRepository;
import com.huatu.tiku.schedule.biz.service.ClassHourFeedbackService;
import com.huatu.tiku.schedule.biz.util.DateformatUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wangjian
 **/
@Service
public class ClassHourFeedbackServiceImpl extends BaseServiceImpl<ClassHourFeedback, Long> implements ClassHourFeedbackService {

    private ClassHourFeedbackRepository classHourFeedbackRepository;

    private ClassHourInfoRepository classHourInfoRepository;

    private TeacherRepository teacherRepository;

    @Autowired
    public ClassHourFeedbackServiceImpl(ClassHourFeedbackRepository classHourFeedbackRepository, ClassHourInfoRepository classHourInfoRepository, TeacherRepository teacherRepository) {
        this.classHourFeedbackRepository = classHourFeedbackRepository;
        this.classHourInfoRepository = classHourInfoRepository;
        this.teacherRepository = teacherRepository;
    }

    @Override
    @Transactional
    public void saveX(CreatFeedBackDto dto) {
        ClassHourFeedback feedback = new ClassHourFeedback();
        BeanUtils.copyProperties(dto, feedback);
        feedback.setStatus(FeedbackStatus.DSH);
        feedback.setDate(DateformatUtil.getLastDate(feedback.getYear(),feedback.getMonth()));
        feedback = classHourFeedbackRepository.save(feedback);
        Long feedbackId = feedback.getId();
        Map<Long,Integer> map=Maps.newHashMap();
        for (CreatFeedBackDto.ClassHourInfoDto infoDto : dto.getInfos()) {
            ClassHourInfo info= new ClassHourInfo();
            BeanUtils.copyProperties(infoDto, info);
            Integer integer = map.get(info.getTeacherId());
            if(null!=integer){
                throw new BadRequestException("出现重复教师,请修改后重新提交");
            }
            if(info.getReallyExam()==null||0>=info.getReallyExam()){
                info.setReallyExam(0d);
            }
            if(info.getReallyHour()==null||0>=info.getReallyHour()){
                info.setReallyHour(0.0);
            }
            if(info.getSimulationExam()==null||0>=info.getSimulationExam()){
                info.setSimulationExam(0d);
            }
            if(info.getSimulationHour()==null||0>=info.getSimulationHour()){
                info.setSimulationHour(0.0);
            }
            if(info.getArticleHour()==null||0>=info.getArticleHour()){
                info.setArticleHour(0.0);
            }
            if(info.getAudioHour()==null||0>=info.getAudioHour()){
                info.setAudioHour(0.0);
            }
            info.setFeedbackId(feedbackId);
            classHourInfoRepository.save(info);
            map.put(info.getTeacherId(),1);
        }
    }

    @Override
    public List<ClassHourFeedback> check(ExamType examType, Long subjectId, Integer year, Integer month) {
        List list=Lists.newArrayList(FeedbackStatus.DSH,FeedbackStatus.YSH);
//        List list=Lists.newArrayList();
//        list.add(FeedbackStatus.DSH);
//        list.add(FeedbackStatus.YSH);
        return classHourFeedbackRepository.findByExamTypeAndSubjectIdAndYearAndMonthAndStatusIn(examType, subjectId, year, month,list);
    }

    @Override
    public Page<ClassHourFeedback> findClassHourFeedback(ExamType examType, Long subjectId, Integer year, Integer month, FeedbackStatus status, Pageable page) {
        Specification<ClassHourFeedback> querySpecific = (root, criteriaQuery, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();
            if (examType != null) {
                predicates.add(criteriaBuilder.equal(root.get("examType"), examType));
            }
            if (subjectId != null) {
                predicates.add(criteriaBuilder.equal(root.get("subjectId"), subjectId));
            }
            if (year != null) {
                predicates.add(criteriaBuilder.equal(root.get("year"), year));
            }
            if (month != null) {
                predicates.add(criteriaBuilder.equal(root.get("month"), month));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        List<Sort.Order> list = Lists.newArrayList();
        list.add(new Sort.Order(Sort.Direction.DESC, "id"));//创建日期倒序
        Pageable pageable = new PageRequest(page.getPageNumber(), page.getPageSize(), new Sort(list));
        return classHourFeedbackRepository.findAll(querySpecific, pageable);
    }

    @Override
    public List<Map> importExcel(List<List<List<String>>> list) {
        List<Map> result=Lists.newArrayList();
        List<List<String>> sheet=list.get(0);
        for(int i=1;i<sheet.size();i++){
            Map<String,String> map= Maps.newHashMap();
            List<String> row = sheet.get(i);
            String teacherName = row.get(0);//教师
            map.put("name",teacherName);
            if(StringUtils.isNotBlank(teacherName)){
                Teacher oneByName = teacherRepository.findOneByName(teacherName.replace(" ",""));
                if(null==oneByName){
                    throw new BadRequestException(i+"行教师"+teacherName+"未找到");
                }
                String teacherId = String.valueOf(oneByName.getId());
                map.put("teacherId",teacherId);
            }else{
                throw new BadRequestException(i+"行教师名称为空");
            }
            String reallyExam = row.get(1);//真题题数
            String reallyHour = row.get(2);//真题课时
            String simulationExam = row.get(3);//模拟题数
            String simulationHour = row.get(4);//模拟题课时
            String articleHour= row.get(5);//文章课时
            String audioHour= row.get(6);//音频课时
            String remark = row.get(7);//备注
            map.put("reallyExam",reallyExam);
            map.put("reallyHour",reallyHour);
            map.put("simulationExam",simulationExam);
            map.put("simulationHour",simulationHour);
            map.put("articleHour",articleHour);
            map.put("audioHour",audioHour);
            map.put("remark",remark);
            result.add(map);
        }
        return result;
    }


}
