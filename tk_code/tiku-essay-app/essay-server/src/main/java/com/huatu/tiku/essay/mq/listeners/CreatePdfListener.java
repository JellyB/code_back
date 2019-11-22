package com.huatu.tiku.essay.mq.listeners;

import com.huatu.tiku.essay.constant.status.EssayPdfTypeConstant;
import com.huatu.tiku.essay.constant.status.EssayQuestionConstant;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssaySimilarQuestion;
import com.huatu.tiku.essay.repository.EssaySimilarQuestionRepository;
import com.huatu.tiku.essay.vo.resp.EssayCreatePdfVO;
import com.huatu.tiku.essay.repository.EssayQuestionBaseRepository;
import com.huatu.tiku.essay.service.EssayQuestionPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.huatu.tiku.essay.constant.status.SystemConstant.CREATE_PDF_ROUTING_KEY;

/**
 *
 * @author x6
 * @date 2017/12/18
 * 生成PDF文件
 */
@Component
@Slf4j
public class CreatePdfListener {

    @Autowired
    private MessageConverter messageConverter;
    @Autowired
    private EssayQuestionPdfService essayQuestionPdfService;
    @Autowired
    EssayQuestionBaseRepository essayQuestionBaseRepository;
    @Autowired
    EssaySimilarQuestionRepository essaySimilarQuestionRepository;

    @RabbitListener(queues = CREATE_PDF_ROUTING_KEY)
    public void onMessage(Message message){
        try {

            EssayCreatePdfVO pdfVO = (EssayCreatePdfVO) messageConverter.fromMessage(message);
            log.info("开始处理MQ消息。生成PDF文件id:{},type:{}",pdfVO.getId(),pdfVO.getType());
            createPdf(pdfVO);
        } catch(MessageConversionException e){
            log.error("convert error，data={}",message,e);
            throw new AmqpRejectAndDontRequeueException("convert error...");
        } catch(Exception e){
            log.error("deal message error，data={}",message,e);
        }
    }




    private void createPdf(EssayCreatePdfVO pdfVO){
        if(EssayPdfTypeConstant.PAPER == pdfVO.getType()){
            //生成试卷pdf
            essayQuestionPdfService.getCoverPdfPath(pdfVO.getId());
            //生成试卷下试题pdf
            List<EssayQuestionBase> essayQuestionBaseList = essayQuestionBaseRepository.findByPaperIdAndBizStatusAndStatusOrderBySortAsc
                    (pdfVO.getId(),EssayQuestionConstant.EssayQuestionBizStatusEnum.ONLINE.getBizStatus(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());
            for(EssayQuestionBase essayQuestionBase:essayQuestionBaseList){
                essayQuestionPdfService.getSinglePdfPath(essayQuestionBase.getId());
            }

        }else if(EssayPdfTypeConstant. SINGLE_QUESTION== pdfVO.getType()){
            List<EssaySimilarQuestion> questionList = essaySimilarQuestionRepository.findBySimilarIdAndStatus
                    (pdfVO.getId(), EssayQuestionConstant.EssayQuestionStatusEnum.NORMAL.getStatus());

            for(EssaySimilarQuestion essayQuestionBase:questionList){
                essayQuestionPdfService.getSinglePdfPath(essayQuestionBase.getQuestionBaseId());
            }
        } else if(EssayPdfTypeConstant.PAPER_WITH_ANSWER == pdfVO.getType()){
            essayQuestionPdfService.getMultiCorrectPdfPath(pdfVO.getId());
        }else if(EssayPdfTypeConstant.SINGLE_QUESTION_WITH_ANSWER == pdfVO.getType()){
            essayQuestionPdfService.getSingleCorrectPdfPath(pdfVO.getId());
        }
    }
}
