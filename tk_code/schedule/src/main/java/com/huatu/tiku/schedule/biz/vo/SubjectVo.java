package com.huatu.tiku.schedule.biz.vo;

import com.huatu.tiku.schedule.biz.domain.Module;
import com.huatu.tiku.schedule.biz.domain.Subject;
import com.huatu.tiku.schedule.biz.enums.ExamType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjian
 **/
@Data
public class SubjectVo implements Serializable{
    private static final long serialVersionUID = -969379167004138574L;

    private Long id;

    private String name;

    private ExamType examType;

    private Boolean showFlag;

    private List<ModuleVo> modules;

    @Data
    private class ModuleVo{//内部模块类
        private Long id;

        private String name;
    }

    public SubjectVo(Subject subject){
        this.id=subject.getId();
        this.name=subject.getName();
        this.examType=subject.getExamType();
        this.showFlag=subject.getShowFlag();
        List<Module> modules = subject.getModules();
        if(null!=modules&&!modules.isEmpty()){
            List<ModuleVo> list=new ArrayList<>();
            modules.forEach(module->{
                ModuleVo moduleVo=new ModuleVo();
                moduleVo.setId(module.getId());
                moduleVo.setName(module.getName());
                list.add(moduleVo);
            });
            this.modules=list;
        }else{
            this.modules=null;
        }
    }
}
