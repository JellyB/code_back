package com.huatu.tiku.position.biz.controller;

import com.google.common.collect.Lists;
import com.huatu.tiku.position.biz.domain.Specialty;
import com.huatu.tiku.position.biz.enums.Education;
import com.huatu.tiku.position.biz.service.SpecialtyService;
import com.huatu.tiku.position.biz.vo.SpecialtyVo;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**专业
 * @author wangjian
 **/
@RestController
@RequestMapping("specialty")
public class SpecialtyController {

    private final SpecialtyService specialtyService;


    public SpecialtyController(SpecialtyService specialtyService ) {
        this.specialtyService = specialtyService;
    }

    //专业层级树
    @GetMapping("getSpecialtys")
    public Object getSpecialtys(Education education){
        List<Specialty> majors= specialtyService.findByEducation(education);
        List<Specialty> oneMajors=majors.stream().filter(specialty->specialty.getType().equals(1)).collect(Collectors.toList());
        List<Specialty> twoMajors=majors.stream().filter(specialty->specialty.getType().equals(2)).collect(Collectors.toList());
        List<Specialty> threeMajors=majors.stream().filter(specialty->specialty.getType().equals(3)).collect(Collectors.toList());
        List<SpecialtyVo> majorVos= Lists.newArrayList();
        oneMajors.forEach(major->{
            SpecialtyVo vo=new SpecialtyVo();
            vo.setValue(major.getId());
            vo.setLabel(major.getName());
            majorVos.add(vo);
            List<SpecialtyVo> specialtyVos = vo.getChildren();
            Iterator<Specialty> iterator = twoMajors.iterator();
            while(iterator.hasNext()){
                Specialty next = iterator.next();
                if(!next.getParentId().equals(major.getId())){
                    continue;
                }
                SpecialtyVo twoVo=new SpecialtyVo();
                twoVo.setValue(next.getId());
                twoVo.setLabel(next.getName());
                specialtyVos.add(twoVo);
                List<SpecialtyVo> twoVos = twoVo.getChildren();
                Iterator<Specialty> threeIterator = threeMajors.iterator();
                while(threeIterator.hasNext()){
                    Specialty threeNext = threeIterator.next();
                    if(!threeNext.getParentId().equals(next.getId())){
                        continue;
                    }
                    SpecialtyVo threeVo=new SpecialtyVo();
                    threeVo.setValue(threeNext.getId());
                    threeVo.setLabel(threeNext.getName());
                    twoVos.add(threeVo);
                    threeIterator.remove();
                }
                iterator.remove();
            }
        });
        return majorVos;
    }
}
