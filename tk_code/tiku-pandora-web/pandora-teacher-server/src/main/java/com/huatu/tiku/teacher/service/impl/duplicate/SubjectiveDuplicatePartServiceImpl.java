package com.huatu.tiku.teacher.service.impl.duplicate;

import com.google.common.collect.Lists;
import com.huatu.tiku.entity.duplicate.SubjectiveDuplicatePart;
import com.huatu.tiku.response.question.DuplicatePartResp;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.question.SubjectiveDuplicatePartMapper;
import com.huatu.tiku.teacher.service.duplicate.SubjectiveDuplicatePartService;
import com.huatu.tiku.util.html.HtmlConvertUtil;
import com.huatu.tiku.util.question.StringMatch;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\28 0028.
 */
@Service
public class SubjectiveDuplicatePartServiceImpl extends BaseServiceImpl<SubjectiveDuplicatePart> implements SubjectiveDuplicatePartService {

    @Autowired
    private SubjectiveDuplicatePartMapper subjectiveDuplicatePartMapper;

    public SubjectiveDuplicatePartServiceImpl() {
        super(SubjectiveDuplicatePart.class);
    }



    @Override
    public int insertWithFilter(SubjectiveDuplicatePart subjectiveDuplicatePart){
        subjectiveDuplicatePart.setId(null);
        assertFilterAttr(subjectiveDuplicatePart);
        return  save(subjectiveDuplicatePart);
    }

    @Override
    public int updateWithFilter(SubjectiveDuplicatePart subjectiveDuplicatePart) {
        assertFilterAttr(subjectiveDuplicatePart);
        return save(subjectiveDuplicatePart);
    }


    /**
     * 给筛选字段赋值
     * @params
     */
    private void assertFilterAttr(SubjectiveDuplicatePart subjectiveDuplicatePart) {
        //分别对题干，答案，分析，拓展,总括字段等，做字符处理,然后给筛选字段赋值
        subjectiveDuplicatePart.setStemFilter(StringMatch.replaceNotChinese(subjectiveDuplicatePart.getStem()));
        subjectiveDuplicatePart.setAnswerCommentFilter(StringMatch.replaceNotChinese(subjectiveDuplicatePart.getAnswerComment()));
        subjectiveDuplicatePart.setAnalyzeQuestionFilter(StringMatch.replaceNotChinese(subjectiveDuplicatePart.getAnalyzeQuestion()));
        subjectiveDuplicatePart.setExtendFilter(StringMatch.replaceNotChinese(subjectiveDuplicatePart.getExtend()));
        subjectiveDuplicatePart.setOmnibusRequirementsFilter(StringMatch.replaceNotChinese(subjectiveDuplicatePart.getOmnibusRequirements()));
        subjectiveDuplicatePart.setAnswerRequestFilter(StringMatch.replaceNotChinese(subjectiveDuplicatePart.getAnswerRequest()));
        subjectiveDuplicatePart.setBestowPointExplainFilter(StringMatch.replaceNotChinese(subjectiveDuplicatePart.getBestowPointExplain()));
         subjectiveDuplicatePart.setTrainThoughtFilter(StringMatch.replaceNotChinese(subjectiveDuplicatePart.getTrainThought()));

    }

    @Override
    public List<DuplicatePartResp> selectByMyExample(String stem, String extend, String answerComment, String analyzeQuestion, String answerRequest,
                                                     String bestowPointExplain, String trainThought, String omnibusRequirements, Integer questionType) {
        if(StringMatch.isAllBlank(stem,extend,answerComment,analyzeQuestion,answerRequest,bestowPointExplain,trainThought,omnibusRequirements)){
            return Lists.newArrayList();
        }
        Example example = new Example(SubjectiveDuplicatePart.class);
        example.and().andEqualTo("status",1);
        String stemFilter = StringMatch.replaceNotChinese(stem);
        if(StringUtils.isNotBlank(stemFilter)){
            example.and().andLike("stemFilter","%"+stemFilter.trim()+"%");
        }
        String extendFilter = StringMatch.replaceNotChinese(extend);
        if(StringUtils.isNotBlank(extendFilter)){
            example.and().andLike("extendFilter","%"+extendFilter.trim()+"%");
        }
        String answerCommentFilter = StringMatch.replaceNotChinese(answerComment);
        if(StringUtils.isNotBlank(answerCommentFilter)){
            example.and().andLike("answerCommentFilter","%"+answerCommentFilter.trim()+"%");
        }
        String analyzeQuestionFilter = StringMatch.replaceNotChinese(analyzeQuestion);
        if(StringUtils.isNotBlank(analyzeQuestionFilter)){
            example.and().andLike("analyzeQuestionFilter","%"+analyzeQuestionFilter.trim()+"%");
        }
        String answerRequestFilter = StringMatch.replaceNotChinese(answerRequest);
        if(StringUtils.isNotBlank(answerRequestFilter)){
            example.and().andLike("answerRequestFilter","%"+answerRequestFilter.trim()+"%");
        }
        String bestowPointExplainFilter = StringMatch.replaceNotChinese(bestowPointExplain);
        if(StringUtils.isNotBlank(bestowPointExplainFilter)){
            example.and().andLike("bestowPointExplainFilter","%"+bestowPointExplainFilter.trim()+"%");
        }
        String trainThoughtFilter = StringMatch.replaceNotChinese(trainThought);
        if(StringUtils.isNotBlank(trainThoughtFilter)){
            example.and().andLike("trainThoughtFilter","%"+trainThoughtFilter.trim()+"%");
        }
        String omnibusRequirementsFilter = StringMatch.replaceNotChinese(omnibusRequirements);
        if(StringUtils.isNotBlank(omnibusRequirementsFilter)){
            example.and().andLike("omnibusRequirementsFilter","%"+omnibusRequirementsFilter.trim()+"%");
        }
        List<SubjectiveDuplicatePart> subjectiveDuplicateParts = selectByExample(example);
        if(CollectionUtils.isEmpty(subjectiveDuplicateParts)){
            return Lists.newArrayList();
        }
        //做出每个查询结果和查询条件的相似度值
        Map<Long, Double> percentMap = subjectiveDuplicateParts.stream().collect(Collectors.toMap(i -> i.getId(),
                i -> getSimilarPercent(i, stemFilter, extendFilter,answerCommentFilter,analyzeQuestionFilter,answerRequestFilter,bestowPointExplainFilter,
                        trainThoughtFilter,omnibusRequirementsFilter )));
        //按照相似度值倒序排列
        subjectiveDuplicateParts.sort(Comparator.comparingDouble(i -> - percentMap.get(i.getId())));
        //取前1000的数值
        if(subjectiveDuplicateParts.size()>1000){
            subjectiveDuplicateParts = subjectiveDuplicateParts.subList(0,1000);
        }
        return subjectiveDuplicateParts.stream().map(i->{
            DuplicatePartResp duplicatePartResp = new DuplicatePartResp();
            BeanUtils.copyProperties(i, duplicatePartResp);
            duplicatePartResp.setDuplicateId(i.getId());
            if(StringUtils.isNotBlank(duplicatePartResp.getStem())){
                duplicatePartResp.setStemShow(HtmlConvertUtil.span2Img(duplicatePartResp.getStem(), false));
            }
            if(StringUtils.isNotBlank(duplicatePartResp.getExtend())){
                duplicatePartResp.setExtendShow(HtmlConvertUtil.span2Img(duplicatePartResp.getExtend(), false));
            }
            return duplicatePartResp;
        }).collect(Collectors.toList());
    }

    @Override
    public HashMap<String, Object> findByQuestionId(long id) {
        return subjectiveDuplicatePartMapper.findByQuestionId(id);
    }

    /**
     * 相似度倒序排序
     * @param objectiveDuplicatePart
     * @param targets
     * @param
     * @return
     */
    private double getSimilarPercent(SubjectiveDuplicatePart objectiveDuplicatePart, String ... targets) {
        if(ArrayUtils.isEmpty(targets)){
            return 0;
        }
        double percent = 0d;
        for(String target:targets){
            if(StringUtils.isNotBlank(target)){
                double p = StringMatch.getSimilar(target,objectiveDuplicatePart.getStem());
                if(p>0){
                    percent += p;
                }
            }
        }
        return percent;
    }
}