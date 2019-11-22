package interview.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.HashMultimap;
import interview.bean.AnswerParagraph;
import interview.bean.SynonymStop;
import interview.dao.hownet.SynonymStopDao;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: xuhuiqiang
 * Time: 2018-07-02  15:43 .
 */
@Component
public  class CommonUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);

    @Autowired
    private SynonymStopDao synonymStopDao;

    //储存所有的停用词
    Cache<String, String> STOP_WORD_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(10000)
                    .build();

    //储存所有的近义词
    Cache<String, List<String>> SYNONYM_WORD_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(100000)
                    .build();

    /**
     * 初始化数据
     */
    @PostConstruct
    public void loadData(){
        long stopWordCacheSize = STOP_WORD_CACHE.size();
        long synonymWordCacheSize = SYNONYM_WORD_CACHE.size();
        if(stopWordCacheSize==0){
            initStopWordCache();
        }
        if(synonymWordCacheSize==0){
            initSynonymWordCache();
        }
    }

    /**
     * 初始化停用词缓存
     */
    public void initStopWordCache(){
        long stopWordCacheSize = STOP_WORD_CACHE.size();
        logger.info("变化前停用词缓存中的数据量={}",stopWordCacheSize);
        synchronized (STOP_WORD_CACHE){
            try{
//                CommonUtil.class.getResourceAsStream("/data/stop.txt");
//                BufferedReader reader = new BufferedReader(new FileReader("data/stop.txt"));
//                String line = reader.readLine();
//                int lineNum = 0;
//                while (line != null){
//                    logger.info("第={}个停用词为={}",lineNum,line);
//                    String stopWord = line.trim();
//                    if(stopWord.length()>=1){
//                        STOP_WORD_CACHE.put(stopWord,stopWord);
//                    }
//                    lineNum++;
//                    line = reader.readLine();
//                }
                InputStream inputStream = CommonUtil.class.getResourceAsStream("/data/stop.txt");
                byte[] b = new byte[10240];
                inputStream.read(b);
                String[] stopAllStrs =  new String(b).split("\\s");
                inputStream.close();
                for(String stopWord:stopAllStrs){
                    String stopWordNew = stopWord.trim();
                    if(stopWordNew.length()>=1){
                        STOP_WORD_CACHE.put(stopWordNew,stopWordNew);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            logger.info("变化前停用词缓存中的数据量={}，变化偶缓存中的数据量={}",stopWordCacheSize,STOP_WORD_CACHE.size());
        }
    }


    /**
     * 初始化近义词缓存
     */
    public void initSynonymWordCache(){
        long synonymWordCacheSize = SYNONYM_WORD_CACHE.size();
        logger.info("变化前近义词缓存中的数据量={}",synonymWordCacheSize);
        synchronized (SYNONYM_WORD_CACHE){
            List<SynonymStop> aalSynonyms = synonymStopDao.findSynonym(0);
            if(CollectionUtils.isNotEmpty(aalSynonyms)){
                HashMultimap<Integer, String> groupSynonym = HashMultimap.create();//根据分组存储近义词
                HashMultimap<String, Integer> synonymGroup = HashMultimap.create();//近义词所属的分组，近义词可属于多个分组
                for(int i=0,size=aalSynonyms.size();i<size;i++){
                    SynonymStop synonym = aalSynonyms.get(i);
                    groupSynonym.put(synonym.getGroupId(),synonym.getItem());
                    synonymGroup.put(synonym.getItem(),synonym.getGroupId());
                }
                for(String synonym:synonymGroup.keySet()){
                    Set<Integer> groupIds = synonymGroup.get(synonym);
                    Set<String> synonymsSet = new HashSet<>();
                    for(int groupId:groupIds){
                        Set<String> synonyms1 = groupSynonym.get(groupId);
                        synonymsSet.addAll(synonyms1);
                    }
                    List<String> synonymsList =  new ArrayList<>();
                    synonymsList.addAll(synonymsSet);
                    SYNONYM_WORD_CACHE.put(synonym,synonymsList);
                }
            }
            logger.info("变化前近义词缓存中的数据量={}，变化偶缓存中的数据量={}",synonymWordCacheSize,SYNONYM_WORD_CACHE.size());
        }
    }

    /**
     * 切段落
     * @param userAnswer
     * @return
     */
    public List<AnswerParagraph> cutParagraph(String userAnswer){
        List<AnswerParagraph> paragraphs = new ArrayList<>();
        AnswerParagraph paragraph = new AnswerParagraph();
        int[] charAscii = {160,8232,12288};
        char ch = (char) charAscii[0];
        String regex = ch+"";
        for(int i=1,len=charAscii.length;i<len;i++){
            regex += ("|"+(char)charAscii[i]);
        }
        String content = userAnswer.replaceAll(regex, " ");
        String[] paragraphStrs = content.split("\\n{1,}|\\s{2,}");
        List<Integer> starts = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        for(int i=0,len=paragraphStrs.length;i<len;i++){
            if(paragraphStrs[i].trim().length()>=2){
                int start = findStart(0,content,paragraphStrs[i].trim(),starts,ends);
                int end = start+paragraphStrs[i].trim().length();
                starts.add(start);
                ends.add(end);
                logger.info("第={}段，内容为：={}",i,content.substring(start,end));
                paragraph.setContent(paragraphStrs[i].trim());
                paragraph.setStart(start);
                paragraphs.add(paragraph);
                paragraph = new AnswerParagraph();
            }
        }
        return paragraphs;
    }

    /**
     * @param firstStart 初始位置
     * @param firstStr
     * @param secondStr
     * @param starts
     * @param ends
     * @return
     */
    public int findStart(int firstStart,String firstStr,String secondStr,List<Integer> starts,List<Integer> ends){
        int len = secondStr.length();
        int len1 = firstStr.length();
        int start = firstStr.indexOf(secondStr)+firstStart;
        int end = start+len;
        while (isContainStart(start,end,starts,ends)&&(start-firstStart)<len1){
            start = firstStr.indexOf(secondStr,end-firstStart)+firstStart;
            end = start+len;
        }
        return start;
    }

    /**
     * 是否该起始终止位置已经被用
     * @param start
     * @param starts
     * @param ends
     * @return
     */
    private boolean isContainStart(int start,int end,List<Integer> starts,List<Integer> ends){
        boolean result = false;
        for(int i=0,size=starts.size();i<size;i++){
            int start1 =  starts.get(i);
            int end1 = ends.get(i);
            if((start1<=start&&end1>start)||(start1<end&&end1>=end)){
                result = true;
            }
        }
        return result;
    }

    /**
     * 断句
     * @param userAnswer
     * @type 1为句子，2为短句
     * @return
     */
    public List<String> cutSentences(String userAnswer,int type){


        List<Character> punctuations = new ArrayList<>();//断句标点
        if(type==1){
            punctuations.addAll(Arrays.asList('。','.',';','；','…','!','?','！','？'));
        }else {//细化成只要，或者,也是一句话
            punctuations.addAll(Arrays.asList('。','.',';','；','…','!','?','？',',','，','！',':','：'));
        }
        return cutSentencesNextLowerNoQuotation(userAnswer,punctuations);
    }

    public List<String> cutSentencesNextLowerNoQuotation(String userAnswer,List<Character> punctuations){
        List<String> sentences = new ArrayList<>();
        StringBuffer sentence = new StringBuffer();
        for(int i=0,len=userAnswer.length();i<len;i++){
            char ch = userAnswer.charAt(i);
            sentence.append(ch);
            if(punctuations.contains(ch)){
                if(ch=='.'){//出现.但是前面出现1,2等分条标记，不做断句
                    String senStr = String.valueOf(sentence).trim();
                    if(senStr.length()<2){
                        sentence = new StringBuffer();
                        continue;
                    }else {
                        String lastChar = senStr.substring(senStr.length()-2,senStr.length()-1);
                        if(isEnd(lastChar)){
                            continue;
                        }
                    }
                }
                String senStr = String.valueOf(sentence.substring(0,sentence.length()-1)).trim();
                if(senStr.length()>0){
                    sentences.add(senStr);
                }
                sentence = new StringBuffer();//重新定义句子
            }
            if(i==len-1){
                String senStr = String.valueOf(sentence).trim();
                if(senStr.length()>0){
                    sentences.add(senStr);
                }
            }
        }
        return sentences;
    }

    public boolean isEnd(String sentence){
        String input = "[0-9a-zA-Z一二三四五六七八九][）)\\s]{0,1}";
        Pattern p = Pattern.compile(input);
        Matcher m = p.matcher(sentence);
        while(m.find()) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param text
     * @param type 0为允许重复，1为不允许重复,2为不去除词性
     * @return
     * @throws IOException
     */
    public List<String> cutWord(String text,int type){
        List<String> words = new ArrayList<>();
        List<Term> parses = NlpAnalysis.parse(text).getTerms();

        if(CollectionUtils.isNotEmpty(parses)){
            for (int i=0,size=parses.size();i<size;i++){
                Term parse = parses.get(i);
                String pos = parse.getNatureStr();
                String item = parse.getName();
                if(type==2){
                    if(!(pos.equals("null")||pos.startsWith("w"))){
                        words.add(item);
                    }
                }else {
                    if(!(pos.equals("null")||pos.startsWith("w")||pos.startsWith("u")||pos.startsWith("e")||pos.startsWith("y")||pos.startsWith("o")
                            ||pos.startsWith("p")||pos.startsWith("m"))&&isvValidWord(item)){//过滤无词性词、标点、助词、叹词、语气词、拟声词、介词、量词、停用词、重复词
                        //logger.info("第几个词={}，词名称={}，词性={}",i,item,pos);

                        if(type==1){
                            if(!words.contains(item)){
                                words.add(item);
                            }
                        }else {
                            words.add(item);
                        }
                    }
                }
            }
        }
        return words;
    }

    /**
     * 是否为无效词
     * @param word
     * @return
     */
    public boolean isvValidWord(String word){
        if(word.length()<2||word.length()>10){//若长度为1或超过10的词语，直接无效
            return false;
        }
        String stop = STOP_WORD_CACHE.getIfPresent(word);
        if (stop==null||stop.length()==0) {//未找到表示不是停用词
            return true;
        }else{//若为停用词，无效
            return false;
        }
    }

    public List<String> findSynonymByWord(String word){
        List<String> synonms = SYNONYM_WORD_CACHE.getIfPresent(word);
        return synonms;
    }


    /**
     * 根据分数，相似度返回最终结果
     * @param score
     * @param sim
     * @return
     */
    public double scoreSim(double score,double sim){
        if(sim<0.55){
            return 0;
        }
        int times = (int) Math.floor(sim*20);
        if(times>17){
            return score;
        }
        return 0.5*Math.round(score*formula(1.0*times)*2);
    }

    public double formula(double num){
        return -0.0007789*Math.pow(num,3)+0.03349*Math.pow(num,2)- 0.4173*Math.pow(num,1)+ 2.183;
    }
}
