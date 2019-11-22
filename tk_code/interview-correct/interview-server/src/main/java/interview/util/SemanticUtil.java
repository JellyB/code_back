package interview.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import interview.bean.HownetPrimitive;
import interview.bean.HownetWord;
import interview.bean.HownetWordRelation;
import interview.common.HownetParameter;
import interview.common.WordHownetType;
import interview.dao.hownet.HownetDao;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-05  11:11 .
 */
@Component
public class SemanticUtil {
    private static final Logger logger = LoggerFactory.getLogger(SemanticUtil.class);

    @Autowired
    private HownetDao hownetDao;

    //储存所有的义原
    Cache<Long, HownetPrimitive> PRIMITIVE_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100000)
                    .build();

    //储存所有的hownet词语
    Cache<String, List<HownetWord>> HOWNET_WORD_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100000)
                    .build();



    /**
     * 加载数据，包括义原，具体词
     */
    @PostConstruct
    public void loadData(){
        long primitiveCacheSize = PRIMITIVE_CACHE.size();
        long hownetWordCacheSize = HOWNET_WORD_CACHE.size();
        if(primitiveCacheSize<=0){
            initPrimitiveCache();
        }
        if(hownetWordCacheSize<=0){
            initHownetWordCache();
        }
    }

    /**
     * 初始化义原缓存
     */
    public void initPrimitiveCache(){
        long primitiveCacheSize = PRIMITIVE_CACHE.size();
        logger.info("变化前primitive缓存中的数据量={}",primitiveCacheSize);
        synchronized(PRIMITIVE_CACHE){
            List<HownetPrimitive> primitives = hownetDao.findPrimitive();
            if(CollectionUtils.isNotEmpty(primitives)){
                for(HownetPrimitive primitive:primitives){
                    PRIMITIVE_CACHE.put(primitive.getId(),primitive);
                    //logger.info("义原id={}，义原={}",primitive.getId(),primitive);
                }
            }
            logger.info("变化前primitive缓存中的数据量={}，变化偶缓存中的数据量={}",primitiveCacheSize,PRIMITIVE_CACHE.size());
        }
    }

    /**
     * 初始化hownet词语缓存
     */
    public void initHownetWordCache(){
        long hownetWordCacheSize = HOWNET_WORD_CACHE.size();
        logger.info("变化前hownet词语缓存中的数据量={}",hownetWordCacheSize);
        synchronized(HOWNET_WORD_CACHE){
            List<HownetWord> hownetWords = hownetDao.findHownetWord();
            List<HownetWordRelation> hownetWordRelations = hownetDao.findHownetWordRelation();
            HashMultimap<Long, HownetWordRelation> hownetWordRelationMap = HashMultimap.create();//根据分组存储近义词
            for(HownetWordRelation hownetWordRelation:hownetWordRelations){
                hownetWordRelationMap.put(hownetWordRelation.getHownetWordId(),hownetWordRelation);
            }
            for(HownetWord hownetWord:hownetWords){
                Set<HownetWordRelation> hownetWordRelationsSet = hownetWordRelationMap.get(hownetWord.getId());
                if(CollectionUtils.isNotEmpty(hownetWordRelationsSet)){
                    List<Long> wordId = new ArrayList<>();//具体词
                    List<Long> primitives = new ArrayList<>();//基本义原
                    Map<Long,List<String>> relationPrimitives = new HashMap<>();//关系义原，义原
                    Map<Long,List<String>> relationPrimitivesWord = new HashMap<>();//关系义原，具体词
                    Map<String,List<Long>> symbolPrimitives = new HashMap<>();//符号，义原
                    Map<String,List<Long>> symbolWords = new HashMap<>();//符号，具体词
                    for(HownetWordRelation hownetWordRelation:hownetWordRelationsSet){
                        //1为具体词，2为基本义原，3为关系义原--义原对，4为关系义原--具体词对，5为符号--义原对，6为符号--具体词对
                        switch (hownetWordRelation.getType()){
                            case 1:wordId.add(hownetWordRelation.getPairId());break;
                            case 2:primitives.add(hownetWordRelation.getPairId());break;
                            case 3:addMap1(hownetWordRelation,relationPrimitives);break;
                            case 4:addMap1(hownetWordRelation,relationPrimitivesWord);break;
                            case 5:addMap(hownetWordRelation,symbolPrimitives);break;
                            case 6:addMap(hownetWordRelation,symbolWords);break;
                        }
                    }
                    hownetWord.setWordId(wordId);
                    hownetWord.setPrimitives(primitives);
                    hownetWord.setRelationPrimitives(relationPrimitives);
                    hownetWord.setRelationPrimitivesWord(relationPrimitivesWord);
                    hownetWord.setSymbolPrimitives(symbolPrimitives);
                    hownetWord.setSymbolWords(symbolWords);
                }
            }
            Map<String,List<HownetWord>> hownetWordsMap = new HashMap<>();
            if(CollectionUtils.isNotEmpty(hownetWords)){
                for(HownetWord hownetWord:hownetWords){
                    List<HownetWord> hownetWords1 = hownetWordsMap.get(hownetWord.getItem());
                    if(hownetWords1==null||hownetWords1.isEmpty()){
                        hownetWords1 = new ArrayList<>();
                        hownetWords1.add(hownetWord);
                        hownetWordsMap.put(hownetWord.getItem(),hownetWords1);
                    }else{
                        hownetWords1.add(hownetWord);
                        hownetWordsMap.put(hownetWord.getItem(),hownetWords1);
                    }
                    //logger.info("词内容={}，词数量={},hownetWord={}",hownetWord.getItem(),hownetWords1.size(),hownetWord);
                }
                HOWNET_WORD_CACHE.putAll(hownetWordsMap);
            }
            logger.info("变化前hownet词语缓存中的数据量={}，变化偶缓存中的数据量={}",hownetWordCacheSize,HOWNET_WORD_CACHE.size());
        }
    }

    private void addMap(HownetWordRelation hownetWordRelation, Map<String,List<Long>> map){
        List<Long> findId = map.get(hownetWordRelation.getPairStr());
        if(CollectionUtils.isNotEmpty(findId)){
            findId.add(hownetWordRelation.getPairId());
            map.put(hownetWordRelation.getPairStr(),findId);
        }else {
            findId = new ArrayList<>();
            findId.add(hownetWordRelation.getPairId());
            map.put(hownetWordRelation.getPairStr(),findId);
        }
    }

    private void addMap1(HownetWordRelation hownetWordRelation, Map<Long,List<String>> map){
        List<String> findStr = map.get(hownetWordRelation.getPairId());
        if(CollectionUtils.isNotEmpty(findStr)){
            findStr.add(hownetWordRelation.getPairStr());
            map.put(hownetWordRelation.getPairId(),findStr);
        }else {
            findStr = new ArrayList<>();
            findStr.add(hownetWordRelation.getPairStr());
            map.put(hownetWordRelation.getPairId(),findStr);
        }
    }


    /**
     * 两个词向量相似度
     * @param words1
     * @param words2
     * @return
     */
    public double simWordsStr(List<String> words1,List<String> words2){
        double result = 0.0;
        if(!CollectionUtils.isNotEmpty(words1)||!CollectionUtils.isNotEmpty(words2)){
            return 0;
        }
        double simMax1 = simMaxStr(words1,words2);
        double simMax2 = simMaxStr(words2,words1);
        if(words1.size()>=3&&words2.size()>=3){
            //result = simMax1>simMax2?simMax1:simMax2;
            result = (simMax1+simMax2)/2;
        }else if(words1.size()>0||words2.size()>0){
            result = (simMax1*words1.size()+simMax2*words2.size())/(words1.size()+words2.size());
        }
        return result;
    }


    public double simMaxStr(List<String> words1,List<String> words2){
        double sum1 = 0.0;
        int words1Num = 0;
        List<String> words1New = refineWords(words1);
        List<String> words2New = refineWords(words2);
        for (int i=0,size1=words1New.size();i<size1;i++){
            double max = 0.0;
            String word = words1New.get(i);
            for(int j=0,size2=words2New.size();j<size2;j++){
                String word2 = words2New.get(j);
                double sim = simWord(word,word2);
                if(sim>max){
                    max = sim;
                    if(max==1){
                        break;
                    }
                }
            }
            words1Num ++;
            sum1 += max;
        }
        return sum1/words1Num;
    }

    /**
     * 处理未收入词
     * @param words
     * @return
     */
    public List<String> refineWords(List<String> words){
        List<String> words1 = new ArrayList<>();
        for(String word:words){
            int pos = word.length();
            if(wordCacheContainWord(word)&&(pos==3||pos==4)&&isContainCharOrNum(word)){
                String word1 = word.substring(0,pos/2);
                String word2 = word.substring(pos/2,pos);
                if(wordCacheContainWord(word1)||wordCacheContainWord(word2)){
                    words1.add(word1);
                    words1.add(word1);
                }else {
                    words1.add(word);
                }
            }else{
                words1.add(word);
            }
        }
        return words1;
    }

    public boolean wordCacheContainWord(String word){
        loadData();
        List<HownetWord> hownetWords = HOWNET_WORD_CACHE.getIfPresent(word);
        if(hownetWords==null||hownetWords.isEmpty()){
            return false;
        }else {
            return true;
        }
    }

    /**
     * 判断是否包含英文或者数字
     * @param word
     * @return
     */
    private boolean isContainCharOrNum(String word){
        String input = "[0-9]|[a-z]|[A-Z]";

        Pattern p = Pattern.compile(input);
        Matcher m = p.matcher(word);

        while(m.find()) {
            return false;
        }
        return true;
    }


    /**
     * 两个词相似度
     * @param word1
     * @param word2
     * @return
     */
    public double simWord(String word1,String word2){
        if(word1.equals(word2)){//词语相同，相似度为1
            return 1.0;
        }
        //// TODO: 10/26/17 需重点考虑未收入词的情况
        if(wordCacheContainWord(word1)&&wordCacheContainWord(word2)){
            List<HownetWord> words1 = HOWNET_WORD_CACHE.getIfPresent(word1);
            List<HownetWord> words2 = HOWNET_WORD_CACHE.getIfPresent(word2);
            double max = 0;
            for (HownetWord w1 : words1) {
                for (HownetWord w2 : words2) {
                    double sim = simWord(w1, w2);
                    max = (sim > max) ? sim : max;
                    if(sim==1){
                        break;
                    }
                }
                if(max==1){
                    break;
                }
            }
            return max;
        }else {
            String str = "";
            if(!wordCacheContainWord(word1)){
                str += ("\t"+word1);
            }
            if(!wordCacheContainWord(word2)){
                str += ("\t"+word2);
            }
            //logger.info("有词没有被收入={}",str);
            return 0.0;
        }
    }

    /**
     * 词相似度
     * @param word1
     * @param word2
     * @return
     */
    public double simWord(HownetWord word1, HownetWord word2){
        //实词和虚词的相似度为0
        if(word1.getType()!=word2.getType()){
            return 0.0;
        }
        //虚词和虚词
        if(word1.getType()== WordHownetType.EMPTY&&word2.getType()==WordHownetType.EMPTY){
            List<Long> primitives1 = word1.getPrimitives();
            List<Long> primitives2 = word2.getPrimitives();
            return simList(primitives1,primitives2,1);
        }
        //实词和实词
        if(word1.getType()==WordHownetType.NOTIONAL&&word2.getType()==WordHownetType.NOTIONAL){
            List<Long> primitives1 = word1.getPrimitives();
            List<Long> primitives2 = word2.getPrimitives();
            double sim1 = 0;
            double sim2 = 0;
            if(CollectionUtils.isNotEmpty(primitives1)&&CollectionUtils.isNotEmpty(primitives2)){
                sim1 = simPrimitive(primitives1.get(0),primitives2.get(0));
                sim2 = simList(primitives1.subList(1,primitives1.size()),primitives2.subList(1,primitives2.size()),1);
            }

            List<Long> wordId1 = word1.getWordId();
            List<Long> wordId2 = word2.getWordId();
            double sim3 = 0;
            if(CollectionUtils.isNotEmpty(wordId1)&&CollectionUtils.isNotEmpty(wordId2)){
                sim3 = simList(wordId1,wordId2,2);
            }

            Map<Long,List<String>> relationPrimitives1 = word1.getRelationPrimitives();
            Map<Long,List<String>> relationPrimitives2 = word2.getRelationPrimitives();
            double sim4 = simMap(relationPrimitives1,relationPrimitives2,1);

            Map<Long,List<String>> relationPrimitivesWord1 = word1.getRelationPrimitivesWord();
            Map<Long,List<String>> relationPrimitivesWord2 = word2.getRelationPrimitivesWord();
            double sim5 = simMap(relationPrimitivesWord1,relationPrimitivesWord2,2);

            Map<String,List<Long>> symbolPrimitives1 = word1.getSymbolPrimitives();
            Map<String,List<Long>> symbolPrimitives2 = word2.getSymbolPrimitives();
            double sim6 = simMapStr(symbolPrimitives1,symbolPrimitives2,1);

            Map<String,List<Long>> symbolWords1 = word1.getSymbolWords();
            Map<String,List<Long>> symbolWords2 = word2.getSymbolWords();
            double sim7 = simMapStr(symbolWords1,symbolWords2,2);

            double product = sim1;
            double sum = HownetParameter.BETA1*product;
            product *= sim2;
            sum += HownetParameter.BETA2*product;
            product *= (sim4>sim5?sim4:sim5);
            sum += HownetParameter.BETA4*product;
            product *= (sim6>sim7?sim6:sim7);
            sum += HownetParameter.BETA6*product;
            sum += HownetParameter.BETA3*(sim1*sim2*sim3);
            //logger.info("id1={},id2={},sim1={},sim2={},sim3={},sim4={},sim5={},sim6={},sim7={}",word1.getId(),word2.getId(),sim1,sim2,sim3,sim4,sim5,sim6,sim7);
            if(sum>1){
                return 1;
            }else {
                return sum;
            }
        }
        return 0.0;
    }

    public double simMap(Map<Long,List<String>> map1,Map<Long,List<String>> map2,int type){
        if (map1.isEmpty() && map2.isEmpty()) {
            return 1;
        }
        int total =map1.size() + map2.size();
        double sim = 0;
        int count = 0;
        for (long key : map1.keySet()) {
            if (map2.containsKey(key)) {
                List<String> list1 = map1.get(key);
                List<String> list2 = map2.get(key);
                sim += simListStr(list1, list2,type);
                count++;
            }
        }
        return (sim + HownetParameter.DELTA * (total-2*count))
                / (total-count);
    }

    public double simMapStr(Map<String,List<Long>> map1,Map<String,List<Long>> map2,int type){
        if (map1.isEmpty() && map2.isEmpty()) {
            return 1;
        }
        int total =map1.size() + map2.size();
        double sim = 0;
        int count = 0;
        for (String key : map1.keySet()) {
            if (map2.containsKey(key)) {
                List<Long> list1 = map1.get(key);
                List<Long> list2 = map2.get(key);
                sim += simList(list1, list2,type);
                count++;
            }
        }
        return (sim + HownetParameter.DELTA * (total-2*count))
                / (total-count);
    }

    /**
     * 字符串列表相似度计算
     * @param list1
     * @param list2
     * @param type 1为义原，2为具体词
     * @return
     */
    public double simListStr(List<String> list1, List<String> list2, int type){
        if (list1.isEmpty() && list2.isEmpty())
            return 1;
        int m = list1.size(),n = list2.size();
        int big = m > n ? m : n;
        int N = (m < n) ? m : n;
        int count = 0;
        int index1 = 0, index2 = 0;
        double sum = 0;
        double max = 0;
        while (count < N) {
            max = 0;
            for (int i = 0; i < list1.size(); i++) {
                for (int j = 0; j < list2.size(); j++) {
                    double sim = 0;
                    if(type==1){
                        sim = simPrimitiveStr(list1.get(i), list2.get(j));
                    }else {
                        sim = simConcreteWordStr(list1.get(i), list2.get(j));
                    }
                    if (sim > max) {
                        index1 = i;
                        index2 = j;
                        max = sim;
                    }
                }
            }
            sum += max;
            list1.remove(index1);
            list2.remove(index2);
            count++;
        }
        return (sum + HownetParameter.DELTA * (big - N)) / big;
    }

    /**
     * 列表相似度
     * @param list1
     * @param list2
     * @param type 1为义原，2为具体词，3为义原--具体词
     * @return
     */
    public double simList(List<Long> list1, List<Long> list2, int type){
        if (list1.isEmpty() && list2.isEmpty()){
            if(type==1||type==3){
                return 1;
            }else{
                return 0;
            }
        }

        int m = list1.size();
        int n = list2.size();
        int big = m > n ? m : n;
        int N = (m < n) ? m : n;
        int count = 0;
        int index1 = 0, index2 = 0;
        double sum = 0;
        double max = 0;
        //TODO 该算法存在改进空间 ，后期可以考虑匈牙利算法
        List<Long> listTemp1 = new ArrayList<>();
        List<Long> listTemp2 = new ArrayList<>();
        listTemp1.addAll(list1);
        listTemp2.addAll(list2);
        while (count < N) {
            max = 0;
            for (int i = 0; i < listTemp1.size(); i++) {
                for (int j = 0; j < listTemp2.size(); j++) {
                    double sim = 0;
                    if(type==1){
                        sim = simPrimitive(listTemp1.get(i), listTemp2.get(j));
                    }else if(type==2){
                        sim = simConcreteWord(listTemp1.get(i), listTemp2.get(j));
                    }else {
                        sim = simPrimitiveWord(listTemp1.get(i), listTemp2.get(j));
                    }
                    if (sim > max) {
                        max = sim;
                        index1 = i;
                        index2 = j;
                        if(sim==1){
                            break;
                        }
                    }
                }
                if(max==1){
                    break;
                }
            }
            sum += max;
            listTemp1.remove(index1);
            listTemp2.remove(index2);
            count++;
        }
        return (sum + HownetParameter.DELTA * (big - N)) / big;
    }

    /**
     * @param primitive1
     * @param primitive2
     * @return
     */
    public double simPrimitive(long primitive1, long primitive2) {
        int dis = disPrimitive(primitive1, primitive2);
        return HownetParameter.ALPHA / (dis + HownetParameter.ALPHA);
    }

    public double simPrimitiveStr(String primitive1,String primitive2){
        int index1 = findIndex(primitive1);
        int index2 = findIndex(primitive2);

        if(index1==-1||index2==-1){
            return 0;
        }else {
            if(!primitive1.substring(0,index1).equals(primitive2.substring(0,index2))){
                return 0;
            }else {
                int primitiveId1 = Integer.parseInt(primitive1.substring(index1));
                int primitiveId2 = Integer.parseInt(primitive2.substring(index2));
                return simPrimitive(primitiveId1,primitiveId2);
            }
        }
    }

    private int findIndex(String content){
        String input = "[0-9]{1,}";

        Pattern p = Pattern.compile(input);
        Matcher m = p.matcher(content);

        if(m.find()){
            return m.start();
        }
        return -1;
    }

    /**
     * 义原与具体词的相似度默认为一个常数
     * @param primitive
     * @param word
     * @return
     */
    public double simPrimitiveWord(long primitive,long word){
        //TODO 该算法可以之后考虑
        return HownetParameter.GAMMA;
    }

    /**
     * 具体词相似度
     * @param wordId1
     * @param wordId2
     * @return
     */
    public double simConcreteWord(long wordId1,long wordId2){
        //TODO 具体词相似度算法也可以改进
        if(wordId1==wordId2){
            return 1;
        }else {
            return 0;
        }
    }

    public double simConcreteWordStr(String word1,String word2){
        if(word1.equals(word2)){
            return 1;
        }else {
            return 0;
        }
    }

    /**
     * 义原距离
     * @param primitive1
     * @param primitive2
     * @return
     */
    public int disPrimitive(long primitive1, long primitive2) {
        List<Long> list1 = getParentPrimitives(primitive1);
        List<Long> list2 = getParentPrimitives(primitive2);
        for (int i = 0; i < list1.size(); i++) {
            long id1 = list1.get(i);
            if (list2.contains(id1)) {
                int index = list2.indexOf(id1);
                return index + i;
            }
        }
        return HownetParameter.DEFAULT_PRIMITIVE_DIS;
    }

    /**
     * 得到所有父节点
     * @param primitiveId
     * @return
     */
    public List<Long> getParentPrimitives(long primitiveId){
        List<Long> list = new ArrayList<>();

        // get the id of this primitive
        HownetPrimitive primitive = PRIMITIVE_CACHE.getIfPresent(primitiveId);

        if (primitive != null) {
            list.add(primitiveId);
            long parentId = primitive.getParentId();
            while (parentId!=primitiveId) {
                list.add(parentId);
                primitive = PRIMITIVE_CACHE.getIfPresent(parentId);
                primitiveId = primitive.getId();
                parentId = primitive.getParentId();
            }
        }
        return list;
    }
}
