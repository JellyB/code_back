package com.huatu.tiku.interview.service.impl;

import com.huatu.tiku.interview.entity.po.Area;
import com.huatu.tiku.interview.entity.po.ClassInfo;
import com.huatu.tiku.interview.entity.vo.response.AreaClassVO;
import com.huatu.tiku.interview.repository.AreaRepository;
import com.huatu.tiku.interview.repository.ClassInfoRepository;
import com.huatu.tiku.interview.service.ClassInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author ZhenYang
 * @Date Created in 2018/1/25 20:03
 * @Description
 */
@Service
public class ClassInfoServiceImpl implements ClassInfoService {

    @Autowired
    private ClassInfoRepository classInfoRepository;
    @Autowired
    private AreaRepository areaRepository;

    @Override
    public List<ClassInfo> getList() {
        return classInfoRepository.findAll();
    }

    @Override
    public List<ClassInfo> getListByTime(Date date) {
        return classInfoRepository.findByTime(date);
    }

    @Override
    public ClassInfo getOne(Long id) {
        return classInfoRepository.findOne(id);
    }

    @Override
    public List<ClassInfo> getListByArea(Long areaId) {
        List<ClassInfo> classInfoList = classInfoRepository.findByAreaId(areaId);
        return classInfoList;
    }

    @Override
    public List<AreaClassVO> getClassListWithArea() {
        //查询所有开班的城市
        List<Long> areaIdList = classInfoRepository.findAreaList();
        LinkedList<AreaClassVO> result = new LinkedList<>();
        //遍历地区列表。
        for(Long areaId:areaIdList){
            //1.查询地区名称
            Area area = areaRepository.findOne(areaId);
            //2.查询地区下班级信息
            List<ClassInfo> classInfoList = classInfoRepository.findByAreaId(areaId);
            LinkedList<AreaClassVO> classInfoVOList = new LinkedList<>();
            if(CollectionUtils.isNotEmpty(classInfoList)){
                for(ClassInfo classInfo:classInfoList){
                    AreaClassVO classInfoVO =  AreaClassVO.builder()
                            .id(classInfo.getId())
                            .name(classInfo.getName())
                            .build();
                    classInfoVOList.add(classInfoVO);
                }
            }
            //3.填充地区信息
            AreaClassVO areaClassVO = AreaClassVO.builder()
                    .id(areaId)
                    .classList(classInfoVOList)
                    .name(area.getName())
                    .build();
            result.add(areaClassVO);
        }

        return result;
    }
}
