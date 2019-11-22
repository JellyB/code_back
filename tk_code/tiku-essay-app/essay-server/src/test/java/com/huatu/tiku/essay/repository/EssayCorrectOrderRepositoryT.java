package com.huatu.tiku.essay.repository;

import com.huatu.tiku.essay.entity.correct.CorrectOrder;
import com.huatu.tiku.essay.repository.v2.CorrectOrderRepository;
import com.huatu.tiku.essay.test.correct.TikuBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@Slf4j
public class EssayCorrectOrderRepositoryT extends TikuBaseTest {

    @Autowired
    private CorrectOrderRepository correctOrderRepository;
    
      @Autowired
    EntityManager entityManager; 

    @Test
    public void getSalaryList() {
        Long teacherId = 7L;

        Date now = new Date();

        Date start = new Date(now.getTime() - (long) 100 * 24 * 60 * 60 * 1000);

        List<Object[]> salaryList = correctOrderRepository.getSalaryList(teacherId, start, now);

        log.info("result is {}", salaryList);
    }
    
    @Test
	public void getCorrectOrderT() {
		String str = "SELECT DISTINCT(correct.id) , IF( TIMEDIFF(now() , correct.gmt_dead_line) > 0 AND correct.biz_status < 4 , 1 , 0) timeOutStatus , IF( correct.biz_status != 4 AND correct.biz_status != 7 , 0 , 1) biz_sort , correct.* FROM v_essay_correct_order correct LEFT JOIN v_essay_teacher teacher ON correct.receive_order_teacher = teacher.id LEFT JOIN v_essay_question_answer question ON correct.answer_card_id = question.id LEFT JOIN v_essay_question_detail detail ON question.question_detail_id = detail.id LEFT JOIN v_essay_paper_answer paper ON correct.answer_card_id = paper.id LEFT JOIN v_essay_similar_question similarQuestion ON question.question_base_id = similarQuestion.question_base_id LEFT JOIN v_essay_similar_question_group similarGroup ON similarQuestion.similar_id = similarGroup.id WHERE correct. STATUS = 1 ORDER BY timeOutStatus DESC , biz_sort , correct.gmt_dead_line";

		Query dataQuery = entityManager.createNativeQuery(str, CorrectOrder.class);

		List<CorrectOrder> resultList = dataQuery.getResultList();
		System.out.println(resultList);
	}
}
