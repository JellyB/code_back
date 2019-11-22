package com.huatu.ztk.paper.bean;

import com.huatu.ztk.knowledge.bean.Module;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by lijun on 2018/6/21
 */
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class PracticeForCoursePaper extends PracticePaper{
    private int courseType;
    private Long courseId;
    private Integer type;//类型 1-课中练习 2-课后练习
    private List<Object> breakPointInfoList;//断点信息

    public PracticeForCoursePaper(String name, int qcount, double difficulty, int catgory, int subject, List<Module> modules, List<Integer> questions, int courseType, Long courseId, Integer type, List<Object> breakPointInfoList) {
        super(name, qcount, difficulty, catgory, subject, modules, questions);
        this.courseType = courseType;
        this.courseId = courseId;
        this.type = type;
        this.breakPointInfoList = breakPointInfoList;
    }

    public int getCourseType() {
        return courseType;
    }

    public void setCourseType(int courseType) {
        this.courseType = courseType;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public List<Object> getBreakPointInfoList() {
        return breakPointInfoList;
    }

    public void setBreakPointInfoList(List<Object> breakPointInfoList) {
        this.breakPointInfoList = breakPointInfoList;
    }
}
