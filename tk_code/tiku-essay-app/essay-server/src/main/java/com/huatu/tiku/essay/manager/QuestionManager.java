package com.huatu.tiku.essay.manager;

import com.google.common.collect.Lists;
import com.huatu.tiku.essay.entity.EssayQuestionBase;
import com.huatu.tiku.essay.entity.EssayQuestionDetail;
import com.huatu.tiku.essay.repository.EssayQuestionDetailRepository;
import com.huatu.tiku.essay.vo.resp.EssayQuestionAreaVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhouwei
 * @Description: Service中公共方法封装
 * @create 2018-03-29 下午2:58
 **/
@Service
public class QuestionManager {

    @Resource
    private EssayQuestionDetailRepository questionDetailRepository;


    public List<EssayQuestionDetail> listQuestionDetail(List<Long> questionDetailIds){
        if(CollectionUtils.isEmpty(questionDetailIds)){
            return Lists.newArrayList();
        }
        return questionDetailRepository.findByIdIn(questionDetailIds);
    }



    /**
     * vo转换
     * @param baseList
     * @return
     */
    public static LinkedList<EssayQuestionAreaVO> changeEssayQuestionBaseToEssayQuestionAreaVO(LinkedList<EssayQuestionBase> baseList) {
        LinkedList<EssayQuestionAreaVO> essayQuestionAreaVOList = new LinkedList<EssayQuestionAreaVO>();
        EssayQuestionAreaVO essayQuestionAreaVO;
        for (EssayQuestionBase essayQuestionBase : baseList) {
            essayQuestionAreaVO = new EssayQuestionAreaVO();
            BeanUtils.copyProperties(essayQuestionBase, essayQuestionAreaVO);
            if (StringUtils.isNotEmpty(essayQuestionBase.getSubAreaName())) {
                essayQuestionAreaVO.setAreaId(essayQuestionBase.getSubAreaId());
                essayQuestionAreaVO.setAreaName(essayQuestionBase.getSubAreaName());
            }
            essayQuestionAreaVO.setQuestionBaseId(essayQuestionBase.getId());
            essayQuestionAreaVO.setQuestionDetailId(essayQuestionBase.getDetailId());
            essayQuestionAreaVOList.add(essayQuestionAreaVO);
        }
        return essayQuestionAreaVOList;
    }


//    /**
//     * 获取题组名称
//     */
//    public static Map<Long,EssaySimilarQuestionGroupInfo> getQuestionGroupMap(EssaySimilarQuestionGroupInfoRepository essaySimilarQuestionGroupInfoRepository, RedisTemplate redisTemplate) {
//
//        //从缓存中取地区信息
//        String questionGroupMapKey = QuestionRedisKeyConstant.getQuestionGroupMapKey();
//        Map questionGroupMap = (HashMap)redisTemplate.opsForValue().get(questionGroupMapKey);
//        if(questionGroupMap == null || questionGroupMap.isEmpty()){
//            questionGroupMap = new HashMap<Long,EssaySimilarQuestionGroupInfo>();
//
//        }
//
//        //缓存没有命中，查mysql
//        List<EssaySimilarQuestionGroupInfo> groupList = essaySimilarQuestionGroupInfoRepository.findByStatus(EssayAreaConstant.EssayAreaStatusEnum.NORMAL.getStatus());
//        if(CollectionUtils.isNotEmpty(groupList)){
//            for(EssaySimilarQuestionGroupInfo group:groupList){
//                questionGroupMap.put(group.getId(),group);
//            }
//            if(!questionGroupMap.isEmpty()){
//                redisTemplate.opsForValue().set(questionGroupMapKey,questionGroupMap);
//                redisTemplate.expire(questionGroupMapKey,5, TimeUnit.MINUTES);
//            }
//        }
//        return questionGroupMap;
//    }
}
