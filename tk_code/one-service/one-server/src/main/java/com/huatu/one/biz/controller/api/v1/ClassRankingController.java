package com.huatu.one.biz.controller.api.v1;

import com.huatu.one.biz.service.ClassRankingService;
import com.huatu.one.biz.service.UsageRecordService;
import com.huatu.one.biz.service.UserService;
import com.huatu.one.biz.vo.DefaultOptionVo;
import com.huatu.one.biz.vo.OptionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 课程排名
 *
 * @author geek-s
 * @date 2019-09-12
 */
@RestController
@RequestMapping("/v1/classRanking")
public class ClassRankingController {

    @Autowired
    private ClassRankingService courseRankingService;

    @Autowired
    private UserService userService;

    @Autowired
    private UsageRecordService usageRecordService;

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 获取默认日期
     *
     * @return 默认日期
     */
    @GetMapping("defaultOptions")
    public Object defaultDates(@RequestHeader String openid) {
        Assert.isTrue(!StringUtils.isEmpty(openid), "未授权");

        LocalDate now = LocalDate.now();

        List<Long> examTypeIds = userService.selectCourseRankingByOpenid(openid);

        return DefaultOptionVo.builder()
                .dateBegin(dtf.format(now.minusDays(1)))
                .dateEnd(dtf.format(now))
                .examTypeId(examTypeIds.isEmpty() ? 0L : examTypeIds.get(0))
                .build();
    }

    /**
     * 获取考试类型
     *
     * @return 考试类型
     */
    @GetMapping("examTypes")
    public Object examTypes(@RequestHeader String openid) {
        Assert.isTrue(!StringUtils.isEmpty(openid), "未授权");

        return courseRankingService.getExamTypes();
    }

    /**
     * 获取考试类型
     *
     * @return 考试类型
     */
    @GetMapping("examTypeOptions")
    public Object examTypeOptions(@RequestHeader String openid) {
        Assert.isTrue(!StringUtils.isEmpty(openid), "未授权");

        return courseRankingService.getExamTypeOptions(openid);
    }

    /**
     * 获取考试类型
     *
     * @return 考试类型
     */
    @GetMapping("customExamTypes")
    public Object customExamTypes(@RequestHeader String openid) {
        Assert.isTrue(!StringUtils.isEmpty(openid), "未授权");

        List<OptionVo> optionVos = courseRankingService.getCustomExamTypes(openid);

        if (optionVos.isEmpty()) {
            optionVos.add(courseRankingService.getDefaultExamType());
        }

        return optionVos;
    }

    /**
     * 添加考试类型
     */
    @PostMapping("addExamTypes")
    public void addExamTypes(@RequestParam Long examTypeId, @RequestHeader String openid) {
        Assert.isTrue(!StringUtils.isEmpty(openid), "未授权");

        courseRankingService.addExamTypes(examTypeId, openid);
    }

    /**
     * 删除考试类型
     */
    @DeleteMapping("delExamTypes")
    public void delExamTypes(@RequestParam Long examTypeId, @RequestHeader String openid) {
        Assert.isTrue(!StringUtils.isEmpty(openid), "未授权");

        courseRankingService.delExamTypes(examTypeId, openid);
    }

    /**
     * 获取排名数据
     *
     * @return 考试类型
     */
    @GetMapping
    public Object list(Long examType, Integer orderBy, String date, @RequestParam(defaultValue = "20") Integer rowcount, String showzeroPrice, @RequestHeader String openid) {
        Assert.isTrue(!StringUtils.isEmpty(openid), "未授权");

        usageRecordService.saveRecord(openid, 3);

        examType = examType == 0 ? null : examType;

        return courseRankingService.getClassRanking(examType, orderBy, date, rowcount, showzeroPrice);
    }
}
