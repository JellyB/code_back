package com.huatu.tiku.essay.constant.cache;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huatu.tiku.essay.vo.resp.EssayMaterialVO;
import com.huatu.tiku.essay.vo.resp.courseExercises.ExercisesListVO.ExercisesItemVO;

/**
 * @author zhouwei
 * @Description: 申论材料缓存
 * @create 2017-12-17 下午5:38
 **/
public class GuavaKeyConstant {
    //缓存题目id对应的材料列表
    public static final Cache<Long, List<EssayMaterialVO>> materialListCache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(1, TimeUnit.HOURS)//缓存时间
                    .maximumSize(2)
                    .build();
    
    /**
     * 课件绑定试题缓存
     */
    public static final Cache<String, List<ExercisesItemVO>> EXERCISESITEMVOCACHE =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(30, TimeUnit.MINUTES)//缓存时间
                    .maximumSize(50)
                    .build();


}
