package com.huatu.tiku.teacher.service.impl.paper;

import com.huatu.tiku.entity.teacher.PaperArea;
import com.huatu.tiku.enums.PaperInfoEnum;
import com.huatu.tiku.service.impl.BaseServiceImpl;
import com.huatu.tiku.teacher.dao.paper.PaperAreaMapper;
import com.huatu.tiku.teacher.service.paper.PaperAreaService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\6\23 0023.
 */
@Service
public class PaperAreaServiceImpl extends BaseServiceImpl<PaperArea> implements PaperAreaService {

    @Autowired
    PaperAreaMapper paperAreaMapper;

    public PaperAreaServiceImpl() {
        super(PaperArea.class);
    }

    @Transactional
    @Override
    public int savePaperAreaInfo(Long paperId, List<Long> areaIds, PaperInfoEnum.TypeInfo typeInfo) {
        //1.删除原始值
        WeekendSqls<PaperArea> paperAreaWeekendSql = buildWeekendSql(paperId, typeInfo);
        Example example = Example.builder(PaperArea.class)
                .andWhere(paperAreaWeekendSql)
                .build();
        List<PaperArea> paperAreaList = selectByExample(example);
        Supplier<List<Long>> getOldList = () -> paperAreaList.stream().map(PaperArea::getAreaId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(paperAreaList)) {
            List<Long> oldList = getOldList.get();
            oldList.removeAll(areaIds);
            if (CollectionUtils.isNotEmpty(oldList)) {
                paperAreaWeekendSql.andIn(PaperArea::getAreaId, oldList);
                Example deleteExample = Example.builder(PaperArea.class)
                        .andWhere(paperAreaWeekendSql)
                        .build();
                paperAreaMapper.deleteByExample(deleteExample);
            }
        }
        //增加新的值
        areaIds.removeAll(getOldList.get());
        return insertPaperAreaInfo(paperId, areaIds, typeInfo);
    }

    @Override
    public int insertPaperAreaInfo(Long paperId, List<Long> areaIds, PaperInfoEnum.TypeInfo typeInfo) {
        if (null != areaIds && areaIds.size() > 0) {
            List<PaperArea> collect = areaIds.stream()
                    .map(areaId ->
                            PaperArea.builder()
                                    .areaId(areaId)
                                    .paperId(paperId)
                                    .paperType(typeInfo.getCode())
                                    .build()
                    )
                    .collect(Collectors.toList());
            return insertAll(collect);
        }
        return 0;
    }

    @Override
    public int deletePaperAreaInfo(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        WeekendSqls paperAreaWeekendSql = buildWeekendSql(paperId, typeInfo);
        Example example = Example.builder(PaperArea.class)
                .andWhere(paperAreaWeekendSql)
                .build();
        return deleteByExample(example);
    }

    @Override
    public List<PaperArea> list(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        WeekendSqls paperAreaWeekendSql = buildWeekendSql(paperId, typeInfo);
        Example example = Example.builder(PaperArea.class)
                .andWhere(paperAreaWeekendSql)
                .build();
        return selectByExample(example);
    }

    private WeekendSqls buildWeekendSql(Long paperId, PaperInfoEnum.TypeInfo typeInfo) {
        return WeekendSqls.<PaperArea>custom()
                .andEqualTo(PaperArea::getPaperId, paperId)
                .andEqualTo(PaperArea::getPaperType, typeInfo.getCode());
    }
}

