package com.huatu.tiku.essay.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.tiku.essay.constant.cache.AreaRedisKeyConstant;
import com.huatu.tiku.essay.entity.EssayBaseArea;
import com.huatu.tiku.essay.entity.EssayQuestionBelongPaperArea;
import com.huatu.tiku.essay.essayEnum.AreaTypeEnum;
import com.huatu.tiku.essay.repository.EssayAreaRepository;
import com.huatu.tiku.essay.repository.EssayBaseAreaRepository;
import com.huatu.tiku.essay.vo.resp.AreaResp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhouwei
 * @Description: 地区查询管理
 * @create 2018-04-03 下午2:24
 **/
@Slf4j
@Service
public class AreaManager {

    static Map<String, Object> cache = new HashMap<>();

    @Resource
    RedisTemplate redisTemplate;
    @Resource
    private EssayBaseAreaRepository areaRepository;

    public List<AreaResp> fetchAreaTree(){
        List<AreaResp> areaResps = (List<AreaResp>)redisTemplate.opsForValue().get(AreaRedisKeyConstant.BASE_AREA_MAP);
        if(areaResps == null){
            areaResps = Lists.newArrayList();
            List<EssayBaseArea> areas = areaRepository.findByParentId(0L);
            List<AreaResp> finalAreaResps = areaResps;
            areas.forEach(essayBaseArea -> {
                AreaResp areaResp = new AreaResp();
                areaResp.setAreaId(essayBaseArea.getId());
                areaResp.setAreaName(essayBaseArea.getName());
                areaResp.setAreas(listAreaResp(essayBaseArea.getId()));
                finalAreaResps.add(areaResp);
            });
            redisTemplate.opsForValue().set(AreaRedisKeyConstant.BASE_AREA_MAP, areaResps);
        }
        return areaResps;
    }

    public List<AreaResp> listAreaResp(Long parentId){
        List<AreaResp> areaResps = Lists.newArrayList();
        List<EssayBaseArea> areas = areaRepository.findByParentId(parentId);
        areas.forEach(essayBaseArea -> {
            AreaResp areaResp = new AreaResp();
            areaResp.setAreaId(essayBaseArea.getId());
            areaResp.setAreaName(essayBaseArea.getName());
            if(AreaTypeEnum.create(essayBaseArea.getType()) != AreaTypeEnum.COUNTRY){
                areaResp.setAreas(listAreaResp(essayBaseArea.getId()));
            }
            areaResps.add(areaResp);
        });
        return areaResps;
    }

    public AreaTree getAreaTree(){

        AreaTree areaTree = (AreaTree) redisTemplate.opsForValue().get(AreaRedisKeyConstant.BASE_AREA_NAME_MAP);
        if(areaTree == null || areaTree.areaIdsMap.isEmpty()){
            areaTree = new AreaTree();
            Map<Long, String> areaNameMap = Maps.newHashMap();
            Map<Long, List<Long>> areaIdMap = Maps.newHashMap();
            areaTree.setAreaNameMap(areaNameMap);
            areaTree.setAreaIdsMap(areaIdMap);
            List<EssayBaseArea> baseAreas = areaRepository.findAll();
            Map<Long, EssayBaseArea>  areaMap = Maps.newHashMap();
            baseAreas.forEach(essayBaseArea -> {
                areaMap.put(essayBaseArea.getId(), essayBaseArea);
            });
            baseAreas.forEach(essayBaseArea -> {
                List<EssayBaseArea> _baseAreas = Lists.newArrayList();
                buildArea(_baseAreas, areaMap, essayBaseArea.getId());
                StringBuffer stringBuffer = new StringBuffer();
                List<Long> areaIds = Lists.newArrayList();
                for(int i=_baseAreas.size()-1; i>=0; i--){
                    stringBuffer.append(_baseAreas.get(i).getName());
                    areaIds.add(_baseAreas.get(i).getId());
                }
                areaNameMap.put(essayBaseArea.getId(), stringBuffer.toString());
                areaIdMap.put(essayBaseArea.getId(), areaIds);
            });
            redisTemplate.opsForValue().set(AreaRedisKeyConstant.BASE_AREA_NAME_MAP, areaTree);
        }
        return areaTree;
    }


    public void buildArea(List<EssayBaseArea> baseAreas, Map<Long, EssayBaseArea> areaMap, Long areaId){
        EssayBaseArea area = areaMap.get(areaId);
        baseAreas.add(area);
        if(area.getParentId() != 0){
            buildArea(baseAreas, areaMap, area.getParentId());
        }
    }

    public void buildAreaName(List<String> areaNames, Long parentId){
        EssayBaseArea baseArea = areaRepository.findOne(parentId);
        areaNames.add(baseArea.getName());
        if(baseArea.getParentId() != 0){
            buildAreaName(areaNames, baseArea.getParentId());
        }
    }









    public static EssayQuestionBelongPaperArea getEssayQuestionBelongPaperArea(EssayAreaRepository essayAreaRepository, long areaId) {
        Object obj = cache.get(String.valueOf(areaId));
        if (obj == null) {
            EssayQuestionBelongPaperArea area = essayAreaRepository.findOne(areaId);
            cache.put(String.valueOf(areaId), area);
            return area;
        } else {
            return (EssayQuestionBelongPaperArea) obj;
        }

    }


    /**
     * 查询所有地区信息（走缓存）
     * @param essayAreaRepository
     * @return
     */
    public static Map<Long,EssayQuestionBelongPaperArea> getAreaMap(EssayAreaRepository essayAreaRepository, RedisTemplate redisTemplate) {

        //从缓存中取地区信息
        String areaMapKey = AreaRedisKeyConstant.getAreaMapKey();
        Map areaMap = (HashMap)redisTemplate.opsForValue().get(areaMapKey);
        if(areaMap == null ||areaMap.isEmpty()){
            areaMap = new HashMap();
            //缓存没有命中，查mysql
            List<EssayQuestionBelongPaperArea> areaList = essayAreaRepository.findAll();
            if(CollectionUtils.isNotEmpty(areaList)){
                for(EssayQuestionBelongPaperArea area:areaList){
                    areaMap.put(area.getId(),area);
                }
                if(!areaMap.isEmpty()){
                    redisTemplate.opsForValue().set(areaMapKey,areaMap);
                }
            }
        }
        return areaMap;
    }

    @Data
    public static class AreaTree implements Serializable {
        private Map<Long, String> areaNameMap;
        private Map<Long, List<Long>> areaIdsMap;
    }
}
