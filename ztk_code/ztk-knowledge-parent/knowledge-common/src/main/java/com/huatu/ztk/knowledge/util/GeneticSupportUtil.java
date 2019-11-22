package com.huatu.ztk.knowledge.util;

import com.huatu.ztk.knowledge.bean.PaperRequire;
import com.huatu.ztk.knowledge.bean.PaperUnit;
import com.huatu.ztk.knowledge.bean.QuestionGeneticBean;
import com.huatu.ztk.knowledge.bean.QuestionGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-12  18:29 .
 */
public class GeneticSupportUtil {
    private static final Logger logger = LoggerFactory.getLogger(GeneticSupportUtil.class);

    /**
     * 计算数列中第几项满足条件，找出第几项后数列和大于等于total
     * @param start 数列第一项
     * @param comDif 公差
     * @param size 项数
     * @param total 输入值
     * @return
     */
    public int computeSeries(int start,int comDif,int size,int total){
        if(total<start){
            return 0;
        }else {
            for(int i=1;i<size;i++){
                int num = (start+start-comDif*i)*i/2;
                if(num>=total){
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 根据参数生成新的paperunit
     * @param id
     * @param unitQuestions
     * @param paperRequire
     * @param difficultyWeight
     * @param moduleCoverageWeight
     * @param yearWeight
     * @return
     */
    public PaperUnit produceUnit(int id,List<QuestionGeneticBean> unitQuestions, PaperRequire paperRequire,double difficultyWeight,double moduleCoverageWeight,double yearWeight){
        double difficulty = 0.0;
        double year = 0.0;
        List<Integer> modeles = new ArrayList<>();
        int size = unitQuestions.size();
        for(int i=0;i<size;i++){
            QuestionGeneticBean question = unitQuestions.get(i);
            difficulty += question.getDifficulty();
            year += question.getYear();
            modeles.add(question.getModuleId());
        }
        difficulty /= size;
        year /= size;

        //去重
        HashSet h = new HashSet(modeles);
        modeles.clear();
        modeles.addAll(h);

        double moduleCoverage = modeles.size() * 1.00 / paperRequire.getModuleIds().size();
        double adaptationDegree = 1 - (1 - moduleCoverage) * moduleCoverageWeight - Math.abs(difficulty - paperRequire.getDifficulty()) /10* difficultyWeight
                -Math.abs(year - paperRequire.getYear()) * yearWeight*0.1;//适应度计算公式

        PaperUnit unit = PaperUnit.builder()
                .id(id)
                .year(year)
                .difficulty(difficulty)
                .qNum(size)
                .questions(unitQuestions)
                .adaptationDegree(adaptationDegree)
                .moduleCoverage(moduleCoverage)
                .build();

        return unit;
    }

    /**
     * 计算单个种群的模块覆盖率
     * @param i 特殊problem，不计算其模块
     * @param unitQuestions
     * @param paperRequire
     * @return
     */
    public double getModuleCoverage(int i,List<QuestionGeneticBean> unitQuestions,PaperRequire paperRequire){
        List<Integer> modeles = new ArrayList<>();
        for(int j=0,size=unitQuestions.size();j<size;j++){
            if(j!=i){
                modeles.add(unitQuestions.get(j).getModuleId());
            }
        }
        //个体所有题目知识点并集跟期望试卷知识点交集
        HashSet h = new HashSet(modeles);
        modeles.clear();
        modeles.addAll(h);

        modeles.retainAll(paperRequire.getModuleIds());
        return modeles.size() * 1.00 / paperRequire.getModuleIds().size();
    }


    /**
     * @param papers
     * @param paper
     * @return
     */
    public boolean containUnit(List<PaperUnit> papers,PaperUnit paper){
        for(int i=0,size=papers.size();i<size;i++){
            List<QuestionGeneticBean> question = new ArrayList<>();
            question.addAll(paper.getQuestions());
            List<QuestionGeneticBean> questionTemp = new ArrayList<>();
            questionTemp.addAll(papers.get(i).getQuestions());
            int len = questionTemp.size();
            int len1 = question.size();
            questionTemp.removeAll(question);
            question.addAll(questionTemp);
            if(question.size()==len){
                return true;
            }
        }
        return false;
    }

    /**
     * 选择该模块下的所有试题
     * @param questionGroups
     * @param moduleId
     * @param questionsOld
     * @return
     */
    public List<QuestionGeneticBean> selectQuestions(List<QuestionGroup> questionGroups,int moduleId,List<QuestionGeneticBean> questionsOld){
        List<QuestionGeneticBean> questions = new ArrayList<>();
        List<QuestionGeneticBean> questionsAll = new ArrayList<>();
        for(int i=0,size=questionGroups.size();i<size;i++){
            QuestionGroup questionGroup = questionGroups.get(i);
            List<QuestionGeneticBean> questionsTemp =  questionGroup.getQuestions();
            //questionsTemp.removeAll(questionsOld);
            if(questionGroup.getModuleId()==moduleId){
                questions.addAll(questionsTemp);
            }
            questionsAll.addAll(questionsTemp);
        }
        if(questions.size()<=1){//说明只有该模块下只有1道题，则返回所有试题
            return questionsAll;
        }else {
            return questions;
        }
    }

    /**
     * 选择该模块下的所有试题群
     * @param questionGroups
     * @param moduleId
     * @return
     */
    public List<QuestionGroup> selectQuestionGroups(List<QuestionGroup> questionGroups,int moduleId){
        List<QuestionGroup> questionGroupsSelect = new ArrayList<>();
        for(int i=0,size=questionGroups.size();i<size;i++){
            QuestionGroup questionGroup = questionGroups.get(i);
            if(questionGroup.getModuleId()==moduleId){
                questionGroupsSelect.add(questionGroup);
            }
        }
        return questionGroupsSelect;
    }


    public List<PaperUnit> resultUnit(List<PaperUnit> unitList, double endcondition){
        List<PaperUnit> units = new ArrayList<>();
        if (unitList.size() > 0)
        {
            for (int i = 0,size = unitList.size(); i < size; i++)
            {
                PaperUnit unit = unitList.get(i);
                if (unit.getAdaptationDegree() >= endcondition)
                {
                    units.add(unit);
                    break;
                }
            }
        }
        return units;
    }


    public void showResult(List<PaperUnit> unitList, double expand)
    {
        for (PaperUnit u:unitList) {
            System.out.println("第几套\t题目数量\t知识点分布\t难度系数\t年份\t适应度");
            System.out.println(u.getId() + "\t\t"+u.getQNum() + "\t\t" + u.getModuleCoverage() + "\t\t" + u.getDifficulty()+ "\t\t" + u.getYear() + "\t\t" + u.getAdaptationDegree()+"\n\n");
        }
    }

    public void showUnit(PaperUnit u)
    {
        DecimalFormat df   = new DecimalFormat("######0.00");
        System.out.println("第几套\t知识点分布\t难度系数\t年份\t适应度");
        System.out.println( u.getId() + "\t\t"+df.format(u.getModuleCoverage()) + "\t\t" + df.format(u.getDifficulty())+"\t" + u.getYear()+"\t" + u.getAdaptationDegree()+"\t");
        System.out.println("--------------------------------------------");
    }
}
