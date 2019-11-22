package com.huatu.ztk.question.controller.v3;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;
import com.huatu.common.SuccessMessage;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.CommonErrors;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.common.QuestionReidsKeys;
import com.huatu.ztk.question.service.QuestionService;
import com.huatu.ztk.question.util.PageUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


/**
 *
 * 获取试题接口 (课程绑定题目查询，没有走rocksdb)
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/v3/questions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class QuestionControllerV3 {
    public static final Logger logger = LoggerFactory.getLogger(QuestionControllerV3.class);
    @Autowired
    private QuestionService questionService;


    /**
     * 批量获取问题实体(按照请求的id顺序排序)
     * @param ids id列表,逗号分割
     * @return
     */
    @RequestMapping(value = "batch", method = RequestMethod.GET)
    @ResponseBody
    public Object batchV3(@RequestParam(defaultValue = "") String ids,
                          @RequestHeader(defaultValue = "-1") int terminal) throws BizException {

        if (StringUtils.isEmpty(ids)) {
            return new ArrayList();
        }
        String[] idArray = ids.split(",");
        List<Integer> idList = new ArrayList();
        for (String str : idArray) {
            Integer id = Ints.tryParse(str);
            //id列表转换错误
            if (id == null) {
                throw new BizException(CommonErrors.INVALID_ARGUMENTS);
            }
            idList.add(id);
        }
//        List<Question> questions = questionService.findBatchV3(idList);
        List<Question> questions = questionService.findBatchV3WithTerminal(idList,terminal);
        Map<Integer, Question> questionMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(questions)){
            /**
             * 空指针异常排除
             */
            List<Question> collect = questions.stream().filter(i -> null != i).collect(Collectors.toList());
            for(Question question:collect){
                questionMap.put(question.getId(),question);
            }
        }
        List<Question> result = new LinkedList<>();
        if(CollectionUtils.isNotEmpty(idList)){
            //将题目按请求参数中id的顺序排列
            for(Integer questionId:idList){
                Question question = questionMap.get(questionId);
                if(null != question){
                    result.add(question);

                }
            }
        }
        return result;
    }




    /**
     *  试题分页查询
     * @param type         题目类型（1客观题，2复合题）
     * @param difficult   难度级别（简单，较易，中等，困难，较难）
     * @param mode        题类型（真题，模拟题）
     * @param points     知识点（“,”拼接的字符串）
     * @param content    文本信息（id或题干内容）
     * @return
     * @throws BizException
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public Object findByConditionV3(@RequestParam(name = "type",defaultValue = "0") Integer type,
                       @RequestParam(name = "difficult",defaultValue = "0")  Integer difficult,
                       @RequestParam(name = "mode",defaultValue = "0")  Integer mode,
                       @RequestParam(name = "subject",defaultValue = "")  String subject,
                       @RequestParam(name = "points",defaultValue = "") String points,
                       @RequestParam(name = "ids",defaultValue = "") String ids,
                       @RequestParam(name = "content",defaultValue = "") String content,
                       @RequestParam(name = "page",defaultValue = "1")  Integer page,
                       @RequestParam(name = "pageSize",defaultValue = "20") Integer pageSize
    ) throws BizException {
        logger.info("==========分页查询开始==========");
        Stopwatch stopwatch = Stopwatch.createStarted();
        PageUtil<Question> questions = questionService.findByConditionV3(type,difficult,mode,points,content,ids,page,pageSize,subject);
        logger.info("=======分页查询用时====="+String.valueOf(stopwatch.stop()));
        return questions;
    }


    @Autowired
    private RedisTemplate redisTemplate;
    @RequestMapping(value = "test",method = RequestMethod.GET)
    public Object test(@RequestParam(value = "ids") String ids){
        logger.info("ids = {}",ids);
        Arrays.asList(ids.split(",")).parallelStream()
                .map(id -> Integer.valueOf(id))
        .forEach(id -> {
            final HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
            final String questionMetaKey = QuestionReidsKeys.getQuestionMetaKey(id);
            final Map<String, String> metaMap = hashOperations.entries(questionMetaKey);
            logger.info("result = {}",metaMap);
        });
        return SuccessMessage.create();
    }
}
