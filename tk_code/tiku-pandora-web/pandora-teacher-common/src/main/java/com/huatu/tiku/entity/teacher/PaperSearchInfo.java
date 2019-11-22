package com.huatu.tiku.entity.teacher;

import com.huatu.tiku.entity.question.PaperQuestionSimpleInfo;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by lijun on 2018/8/10
 */
@Data
@Builder
public class PaperSearchInfo {

    /**
     * 试卷ID
     */
    private Long id;

    /**
     * 试卷名称
     */
    private String name;

    /**
     * 试题总数量
     */
    private Integer questionCount;

    /**
     * 总分数
     */
    private Double totalScore;

    /**
     * 试卷模块信息
     */
    private List<ModuleInfo> moduleInfo;

    /**
     * 模块字符串
     */
    private String moduleInfoStr;

    /**
     * 初始化试题总数量
     */
    public void initQuestionCount() {
        this.questionCount = 0;
        if (null != moduleInfo) {
            moduleInfo.stream()
                    .flatMap(moduleInfoData -> moduleInfoData.getList().stream())
                    .forEach(paperQuestionSimpleInfo -> {
                        if (CollectionUtils.isNotEmpty(paperQuestionSimpleInfo.getChildren())) {
                            this.questionCount += paperQuestionSimpleInfo.getChildren().size();
                        } else {
                            this.questionCount += 1;
                        }
                    });
        }
    }

    /**
     * 初始化总分数
     */
    public Double iniTotalScore() {
        this.totalScore = 0d;
        if (null != moduleInfo) {
            moduleInfo.stream()
                    .flatMap(moduleInfoData -> moduleInfoData.getList().stream())
                    .forEach(paperQuestionSimpleInfo -> {
                        this.totalScore += paperQuestionSimpleInfo.getScore() == null ? 0d : paperQuestionSimpleInfo.getScore();
                        if (CollectionUtils.isNotEmpty(paperQuestionSimpleInfo.getChildren())) {
                            paperQuestionSimpleInfo.getChildren().stream()
                                    .forEach(children -> this.totalScore += children.getScore() == null ? 0d : children.getScore());
                        }
                    });
        }
        return this.totalScore;
    }


    @Data
    @NoArgsConstructor
    public static class ModuleInfo extends PaperModuleInfo {

        /**
         * 模块下对应的试题信息
         */
        private List<PaperQuestionSimpleInfo> list;

        public ModuleInfo(Integer id, String name, List<PaperQuestionSimpleInfo> list) {
            super(id, name);
            this.list = list;
        }
    }
}
