package com.huatu.tiku.essay.manager;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.constant.status.EssayQuestionAnswerConstant;
import com.huatu.tiku.essay.entity.EssayPaperAnswer;
import com.huatu.tiku.essay.entity.EssayQuestionAnswer;
import com.huatu.tiku.essay.entity.correct.CorrectFeedBack;
import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.essayEnum.EssayStatusEnum;
import com.huatu.tiku.essay.repository.EssayPaperAnswerRepository;
import com.huatu.tiku.essay.repository.EssayQuestionAnswerRepository;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.repository.v2.EssayCorrectFeedBackRepository;
import com.huatu.tiku.essay.vo.teacher.CorrectOrderQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * Created by duanxiangchao on 2019/7/13
 */
@Service
public class CorrectOrderManager {

    @Resource
    private CorrectOrderRepository correctOrderRepository;
    @Resource
    private EssayQuestionAnswerRepository questionAnswerRepository;
    @Resource
    private EssayPaperAnswerRepository paperAnswerRepository;
    @Resource
    private EssayCorrectFeedBackRepository feedBackRepository;

    public List<CorrectOrder> listCorrectOrder(Long teacherId){
        return correctOrderRepository.findByReceiveOrderTeacherEquals(teacherId);
    }

    public Page<CorrectOrder> pageCorrectOrder(CorrectOrderQuery query){
        PageRequest pageable = new PageRequest(query.getPage() - 1, query.getPageSize(), Sort.Direction.ASC, "id");
        Specification<CorrectOrder> specification = new Specification<CorrectOrder>() {
            @Override
            public Predicate toPredicate(Root<CorrectOrder> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {

                List<Predicate> list = Lists.newArrayList();
                if(query.getReceiveOrderTeacher() != null){
                    list.add(criteriaBuilder.equal(root.get("receiveOrderTeacher").as(Long.class), query.getReceiveOrderTeacher()));
                }
                Predicate[] p = new Predicate[list.size()];
                return criteriaBuilder.and(list.toArray(p));

            }
        };
        final Page<CorrectOrder> paperList = correctOrderRepository.findAll(specification, pageable);
        return paperList;

    }

    public List<EssayPaperAnswer> listPaperAnswer(List<Long> ids){
        return paperAnswerRepository.findByIdIn(ids);
    }

    public List<EssayQuestionAnswer> listQuestionAnswer(List<Long> ids){
        return questionAnswerRepository.findByIdIn(ids);
    }

//    public List<EssayQuestionAnswer> listQuestionAnswer(Long paperAnswerId){
//        return questionAnswerRepository.findByPaperAnswerIdAndStatus
//                (paperAnswerId, EssayQuestionAnswerConstant.EssayQuestionAnswerStatusEnum.NORMAL.getStatus(), new Sort(Sort.Direction.ASC, "id"));
//    }

    public List<CorrectFeedBack> listFeedBack(List<Long> answerIds){
        return feedBackRepository.findByAnswerIdIn(answerIds);
    }

    public List<CorrectFeedBack> listFeedBackByOrderId(List<Long> orderIds){
        return feedBackRepository.findByStatusAndOrderIdIn(EssayStatusEnum.NORMAL.getCode(), orderIds);
    }


}
