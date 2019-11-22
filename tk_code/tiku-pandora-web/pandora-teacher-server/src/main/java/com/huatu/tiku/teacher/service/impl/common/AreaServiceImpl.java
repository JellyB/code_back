package com.huatu.tiku.teacher.service.impl.common;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huatu.tiku.constants.cache.RedisKeyConstant;
import com.huatu.tiku.entity.common.Area;
import com.huatu.tiku.response.area.AreaTreeResp;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.service.common.AreaService;
import com.huatu.tiku.teacher.util.personality.PersonalityAreaUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by x6 on 2018/5/8.
 */
@Service
public class AreaServiceImpl extends BaseServiceImpl<Area> implements AreaService {
    @Autowired
    private RedisTemplate redisTemplate;

    public AreaServiceImpl() {
        super(Area.class);
    }

    @Override
    public List<AreaTreeResp> areaList() {
        String areaTreeKey = RedisKeyConstant.getAreaTreeKey();
        List<AreaTreeResp> areaTree = (List<AreaTreeResp>) redisTemplate.opsForValue().get(areaTreeKey);
        if (CollectionUtils.isEmpty(areaTree)) {
            List<Area> areas = selectAll();
            checkAreaTree(areas);
            areaTree = getAreaTree(0L, areas);
            for (AreaTreeResp area : areaTree) {
                List<AreaTreeResp> subAreaTree = getAreaTree(area.getId(), areas);
                area.setSubAreaList(subAreaTree);
            }

            //数据不为空，放缓存(过期时间：5分钟)
            if (CollectionUtils.isNotEmpty(areaTree)) {
                redisTemplate.opsForValue().set(areaTreeKey, areaTree);
                redisTemplate.expire(areaTreeKey, 7, TimeUnit.DAYS);
            }
        }
        return areaTree;
    }

    private void checkAreaTree(List<Area> areas) {
        Set<Long> lookedIds = Sets.newHashSet();
        checkAreaTree(areas, lookedIds, 0L);
    }

    private void checkAreaTree(List<Area> areas, Set<Long> lookedIds, long parent) {
        List<Area> children = getChildren(areas, parent);
        if (CollectionUtils.isNotEmpty(children)) {
            checkLookedIdAndSet(lookedIds, children);
            for (Area child : children) {
                checkAreaTree(areas, lookedIds, child.getId());
            }
        }
    }

    private void checkLookedIdAndSet(Set<Long> lookedIds, List<Area> children) {
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        for (Area child : children) {
            if (lookedIds.contains(child)) {
                throwBizException("错误的地区信息:" + child.getName() + "|" + child.getId());
            }
        }

    }

    private List<Area> getChildren(List<Area> areas, long parent) {
        if (CollectionUtils.isEmpty(areas)) {
            return Lists.newArrayList();
        }
        List<Area> collect = areas.parallelStream().filter(i -> i.getPId().longValue() == parent)
                .collect(Collectors.toList());
        return collect;
    }

    //知识点树
    public List<AreaTreeResp> getAreaTree(Long parentId, List<Area> areas) {
        LinkedList<AreaTreeResp> areaTree = null;
        if (CollectionUtils.isEmpty(areas)) {
            return areaTree;
        }
        List<Area> areaList = areas.stream().filter(i -> i.getPId().equals(parentId)).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(areaList)) {
            areaTree = new LinkedList<>();
            areaList.sort(Comparator.comparing(i -> i.getSort()));
            for (Area area : areaList) {
                AreaTreeResp areaTreeResp = AreaTreeResp.builder()
                        .id(area.getId())
                        .name(area.getName())
                        .build();

                List<AreaTreeResp> subAreaList = getAreaTree(area.getId(), areas);
                areaTreeResp.setSubAreaList(subAreaList);
                areaTree.add(areaTreeResp);
            }
        }
        return areaTree;
    }

    @Override
    public List<AreaTreeResp> findAreaBySubject(Long subject) {
        List<AreaTreeResp> areaTreeResps = areaList();

        PersonalityAreaUtil.filterAndChangeName(areaTreeResps,subject);
        return areaTreeResps;
    }

    @Override
    public List<String> findNameByIds(List<Long> areaIds) {
        List<Area> areas = findByIds(areaIds);
        if (CollectionUtils.isEmpty(areas)) {
            return Lists.newArrayList();
        }
        return areas.stream().map(i -> i.getName()).collect(Collectors.toList());
    }

    @Override
    public List<Area> findByIds(List<Long> areaIds) {
        Example example = new Example(Area.class);
        example.and().andIn("id", areaIds);
        List<Area> areas = selectByExample(example);
        return areas;
    }
}
