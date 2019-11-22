package com.huatu.ztk.knowledge.util;

import com.huatu.ztk.knowledge.bean.PaperRequire;
import com.huatu.ztk.knowledge.bean.PaperUnit;
import com.huatu.ztk.knowledge.bean.QuestionGeneticBean;
import com.huatu.ztk.knowledge.bean.QuestionGroup;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Author: xuhuiqiang
 * Time: 2017-09-04  11:01 .
 * 遗传算法
 */
public class GeneticAlgorithmUtil {
    private static final Logger logger = LoggerFactory.getLogger(GeneticAlgorithmUtil.class);

    private double difficultyWeight;//难度权重
    private double moduleCoverageWeight;//模块覆盖权重
    private double yearWeight;//年份权重

    private GeneticSupportUtil geneticSupportUtil = new GeneticSupportUtil();

    public GeneticAlgorithmUtil(){
        this.difficultyWeight  = 0.3;
        this.moduleCoverageWeight  = 0.1;
        this.yearWeight = 0.6;
    }

    public GeneticAlgorithmUtil(double difficultyWeight,double moduleCoverageWeight,double yearWeight){
        this.difficultyWeight = difficultyWeight;
        this.moduleCoverageWeight = moduleCoverageWeight;
        this.yearWeight = yearWeight;
    }

    /**
     * 种群初始化
     * @param count 种群内部个体数量
     * @param paperRequire 试卷要求（即为种群内部个体要求)
     * @param questionGroupMap 预处理后的试题列表(有顺序)
     * @return
     */
    public List<PaperUnit> initialPopulation(int count, PaperRequire paperRequire, List<QuestionGroup> questionGroupMap){
        long stime1 = System.currentTimeMillis();
        List<PaperUnit> unitList = new ArrayList<>();
        List<Integer> eachModuleCount = paperRequire.getEachTypeCount();
        long stime2 = System.currentTimeMillis();
        logger.info("step6.1用时={}，第几个={}，",stime2-stime1);
        Random rand = new Random();
        for(int i=0;i<count;i++){
            long stime21 = System.currentTimeMillis();
            List<QuestionGeneticBean> unitQuestions = new ArrayList<>();
            for(int j=0,size=eachModuleCount.size();j<size;j++){

                List<QuestionGeneticBean> questions = new ArrayList<>();
                for(int k=0,len=eachModuleCount.get(j);k<len;k++){
                    long stime211 = System.currentTimeMillis();
                    List<QuestionGroup> questionGroups = geneticSupportUtil.selectQuestionGroups(questionGroupMap,paperRequire.getModuleIds().get(j));
                    long stime212 = System.currentTimeMillis();
                    logger.info("step6.2.1用时={}，第几个={}，",stime212-stime211,j);
                    //排在前面的questionGroup抽中的概率最大，采用（grouSzie-questionGroup下标）为首项，公差为2的等差数列，然后在总和范围中随机一个数字
                    int grouSize = questionGroups.size();
                    int totalDistance = grouSize*grouSize;
                    List<QuestionGeneticBean> groupQuestion = new ArrayList<>();//QuestionGroup存放的题；

                    while(groupQuestion.size()==0){//若为0，则不断寻找试题，确保一定能找到题
                        int rNum = rand.nextInt(totalDistance)+1;
                        int m = geneticSupportUtil.computeSeries(grouSize,2,grouSize,rNum);
                        groupQuestion = questionGroups.get(m).getQuestions();
                        groupQuestion.removeAll(questions);
                        if(groupQuestion.size()==0){//questionGroupMap中该组groupQuestion
                            questionGroups.remove(m);
                            grouSize = questionGroups.size();
                            totalDistance = grouSize*grouSize;
                        }
                    }
                    long stime213 = System.currentTimeMillis();
                    logger.info("step6.2.2用时={}，第几个={}，",stime213-stime212,j);
                    int index = rand.nextInt(groupQuestion.size());
                    questions.add(groupQuestion.get(index));
                }
                unitQuestions.addAll(questions);
            }
            unitList.add(geneticSupportUtil.produceUnit(i,unitQuestions,paperRequire,difficultyWeight,moduleCoverageWeight,yearWeight));
            long stime22 = System.currentTimeMillis();
            logger.info("step6.2---1用时={}，第几个={}，",stime22-stime21,i);
        }
        long stime3 = System.currentTimeMillis();
        logger.info("step6.2用时={}，第几个={}，",stime3-stime2);
        return unitList;
    }


    /**
     * 种群选择
     * @param unitList
     * @param count
     * @return
     */
    public List<PaperUnit> selectPopulation(List<PaperUnit> unitList, int count)
    {
        List<PaperUnit> selectedUnitList = new ArrayList<>();
        //种群个体适应度和
        double AllAdaptationDegree = 0;
        for (PaperUnit unit:unitList) {
            AllAdaptationDegree += unit.getAdaptationDegree();
        }

        Random rand = new Random();
        while (selectedUnitList.size() != count){
            //选择一个0—1的随机数字
            double degree = 0.00;
            double randDegree = (rand.nextInt(99)+1) * 0.01 * AllAdaptationDegree;

            //选择符合要求的个体
            for (int j = 0,size = unitList.size(); j < size; j++){
                PaperUnit unit = unitList.get(j);
                degree += unit.getAdaptationDegree();
                if (degree >= randDegree){
                    selectedUnitList.add(unit);
                    //被选择后，不参与下次随机选择
                    AllAdaptationDegree -= unit.getAdaptationDegree();
                    unitList.remove(j);
                    break;
                }
            }
        }
        return selectedUnitList;
    }

    /**
     * 交叉计算
     * @param unitList
     * @param count
     * @param paperRequire
     * @return
     */
    public List<PaperUnit> crossPopulation(List<PaperUnit> unitList, int count, PaperRequire paperRequire)
    {
        List<PaperUnit> crossedUnitList = new ArrayList<>();
        Random rand = new Random();
        int cycleTimes = 0;
        while (crossedUnitList.size() != count && cycleTimes<100){
            cycleTimes++;
            //随机选择两个个体
            int indexOne = rand.nextInt(unitList.size());
            int indexTwo = rand.nextInt(unitList.size());

            if (indexOne != indexTwo){
                PaperUnit unitOne = unitList.get(indexOne);
                PaperUnit unitTwo = unitList.get(indexTwo);

                //随机选择一个交叉位置
                int crossPosition = rand.nextInt(unitOne.getQNum() - 2);

                List<QuestionGeneticBean> questionsOne = new ArrayList<>();
                List<QuestionGeneticBean> questionsTwo = new ArrayList<>();
                questionsOne.addAll(unitOne.getQuestions());
                questionsTwo.addAll(unitTwo.getQuestions());

                //交换交叉位置后面两道题
                for (int i = crossPosition; i < crossPosition + 2; i++){
                    QuestionGeneticBean questionOne = questionsOne.get(i);
                    QuestionGeneticBean questionTwo = questionsTwo.get(i);
                    questionsOne.set(i,questionTwo);
                    questionsTwo.set(i,questionOne);
                }

                DecimalFormat df   = new DecimalFormat("######0.00");
                String unitOneKvConverage =  df.format(geneticSupportUtil.getModuleCoverage(-1,questionsOne,paperRequire));
                String unitTwoKvConverage =  df.format(geneticSupportUtil.getModuleCoverage(-1,questionsTwo,paperRequire));

                HashSet questionsOneSet = new HashSet(questionsOne);
                HashSet questionsTwoSet = new HashSet(questionsTwo);

                //保证交叉的题目模块全覆盖，并确保交叉后试题不会出现重复
                if (crossedUnitList.size() < count && unitOneKvConverage.equals("1.00") && questionsOneSet.size() == paperRequire.getQNum()){
                    //两个新个体
                    PaperUnit unitNewOne = geneticSupportUtil.produceUnit(crossedUnitList.size(),questionsOne,paperRequire,difficultyWeight,moduleCoverageWeight,yearWeight);
                    //添加到新种群集合中
                    if (!geneticSupportUtil.containUnit(crossedUnitList,unitNewOne)) {
                        crossedUnitList.add(unitNewOne);
                    }
                }
                //保证交叉的题目模块全覆盖，并确保交叉后试题不会出现重复
                if (crossedUnitList.size() < count && unitTwoKvConverage.equals("1.00") && questionsTwoSet.size() == paperRequire.getQNum()){
                    //两个新个体
                    PaperUnit unitNewTwo = geneticSupportUtil.produceUnit(crossedUnitList.size(),questionsTwo,paperRequire,difficultyWeight,moduleCoverageWeight,yearWeight);
                    //添加到新种群集合中
                    if (!geneticSupportUtil.containUnit(crossedUnitList,unitNewTwo)) {
                        crossedUnitList.add(unitNewTwo);
                    }
                }
            }
        }
        return crossedUnitList;
    }

    /**
     * 变异计算
     * @param unitList
     * @param questionGroupMap
     * @param paperRequire
     * @return
     */
    public List<PaperUnit> changePopulation(List<PaperUnit> unitList, List<QuestionGroup> questionGroupMap, PaperRequire paperRequire)
    {
        List<PaperUnit> changeUnits = new ArrayList<>();
        Random rand = new Random();
        int index = 0;
        for (int i=0,size=unitList.size();i<size;i++) {
            long stime1 = System.currentTimeMillis();
            PaperUnit unit = unitList.get(i);
            List<QuestionGeneticBean> unitQuestions = unit.getQuestions();
            //随机选择一道题
            index = rand.nextInt(unitQuestions.size());
            QuestionGeneticBean temp = unitQuestions.get(index);
            long stime2 = System.currentTimeMillis();
            logger.info("step7.1用时={}，第几个={}，",stime2-stime1,i);
            DecimalFormat df   = new DecimalFormat("######0.00");
            String unitKvConverage =  df.format(geneticSupportUtil.getModuleCoverage(index,unit.getQuestions(),paperRequire));
            int moduleId = 0;
            long stime3 = System.currentTimeMillis();
            logger.info("step7.2用时={}，第几个={}，",stime3-stime2,i);

            if(unitKvConverage.equals("1.00")){//说明去除该题，不影响模块覆盖率
                int indexModule = rand.nextInt(paperRequire.getModuleIds().size());
                moduleId = paperRequire.getModuleIds().get(indexModule);
            }else{//移除该题，会影响模块覆盖率
                moduleId = temp.getModuleId();
            }
            long stime4 = System.currentTimeMillis();
            logger.info("step7.3用时={}，第几个={}，",stime4-stime3,i);

            //优先选择年份比较大的题
            List<QuestionGroup> questionGroups = geneticSupportUtil.selectQuestionGroups(questionGroupMap,moduleId);
            long stime5 = System.currentTimeMillis();
            logger.info("step7.4用时={}，第几个={}，",stime5-stime4,i);
            int grouSize = questionGroups.size();
            int totalDistance = grouSize*grouSize;
            int indexQuestions = rand.nextInt(totalDistance)+1;
            int m = geneticSupportUtil.computeSeries(grouSize,2,grouSize,indexQuestions);
            QuestionGroup selectGroup = questionGroupMap.get(m);
            int n = rand.nextInt(selectGroup.getQuestions().size());
            QuestionGeneticBean selectQ = selectGroup.getQuestions().get(n);
            int cycelTime = 0;
            //循环10次还没有结果就不循环了
            while(unitQuestions.contains(selectQ)&&cycelTime<10){
                int indexof = unitQuestions.indexOf(selectQ);
                logger.info("unitQuestions.get(indexof)={}",unitQuestions.get(indexof));
                logger.info("questionsSelect.get(m)={}",selectQ);
                indexQuestions = rand.nextInt(totalDistance)+1;
                m = geneticSupportUtil.computeSeries(grouSize,2,grouSize,indexQuestions);
                selectGroup = questionGroupMap.get(m);
                n = rand.nextInt(selectGroup.getQNum())+1;
                selectQ = selectGroup.getQuestions().get(n);
            }
            long stime6 = System.currentTimeMillis();
            logger.info("step7.5用时={}，第几个={}，",stime6-stime5,i);
            //超过10次还没有结果，就不进行变异了
            if(cycelTime==10){
                selectQ = unitQuestions.get(index);
            }
            List<QuestionGeneticBean> questionsNew = new ArrayList<>();
            questionsNew.addAll(unit.getQuestions());
            questionsNew.set(index,selectQ);
            unit.setQuestions(questionsNew);
            changeUnits.add(unit);
            long stime7 = System.currentTimeMillis();
            logger.info("step7.5用时={}，第几个={}，",stime7-stime6,i);
        }
        return changeUnits;
    }

}
