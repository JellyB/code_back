package interview.util;

import interview.bean.*;
import interview.common.AnswerPhraseStatus;
import interview.dao.result.ResultDao;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: xuhuiqiang
 * Time: 2018-08-20  19:51 .
 */
@Component
public class CorrectUtil {
    private static final Logger logger = LoggerFactory.getLogger(CorrectUtil.class);

    @Autowired
    private CommonUtil commonUtil;
    @Autowired
    private SemanticUtil semantic;
    @Autowired
    private ResultDao resultDao;

    public List<String> correctInterview(String userAnswer,long answerCardId,List<InterviewScoreDescription> scoreDescs,int questionRecordType) throws Exception {
        logger.info("这时传输进来的答题卡id={}",answerCardId);
        List<String> result = new ArrayList<>();
        List<AnswerSentence> sentences = new ArrayList<>();
        List<InterviewAnswerPhraseCard> phraseCards = new ArrayList<>();
        assembleUserAnswer(userAnswer,sentences);
        userAnswerAndStandardAnswer(scoreDescs,sentences,phraseCards);
        insertResult(phraseCards,result,answerCardId,questionRecordType);
        return result;
    }

    public void insertResult(List<InterviewAnswerPhraseCard> phraseCards,List<String> result,long answerCardId,int questionRecordType){
        double scoreLast = 0.0;
        List<String> scorePointIds = new ArrayList<>();
        Map<Long,Double> desScoreMap = new HashMap<>();
        for(InterviewAnswerPhraseCard phraseCard:phraseCards){
            double descFullMark = phraseCard.getDescFullMark();
            double actualScore = phraseCard.getActualScore();
            long descId = phraseCard.getDescId();
            long scorePointId = phraseCard.getScorePointId();
            List<String> phraseStrsList = phraseCard.getAnswerPhrases();
            String phraseStrs = "";
            int similarity = Integer.parseInt(new java.text.DecimalFormat("0").format(phraseCard.getSim()*100));
            for(String phraseStr:phraseStrsList){
                phraseStrs += phraseStr+"，";
            }
            phraseStrs = phraseStrs.substring(0,phraseStrs.length()-1);
            if(desScoreMap.containsKey(descId)){
                double descScore = desScoreMap.get(descId);
                if(descScore<descFullMark){
                    double descScoreAdd = descFullMark-descScore>=actualScore?actualScore:descFullMark-descScore;
                    descScore += descScoreAdd;
                    scoreLast += descScoreAdd;
                    desScoreMap.put(descId,descScore);
                    logger.info("最终得分情况：标答={}，总分={}，无处理实际得分={}，处理后得分={}，匹配到的句子={}",phraseCard.getContent(),
                            phraseCard.getScore(),phraseCard.getActualScore(),descScoreAdd,phraseStrs);
                    resultDao.insertResult(phraseCard.getContent(),phraseStrs,answerCardId,phraseCard.getActualScore(),descScoreAdd,similarity,questionRecordType);
                }
            }else {
                if(actualScore>descFullMark){
                    double descScoreAdd = descFullMark;
                    scoreLast += descScoreAdd;
                    desScoreMap.put(descId,descScoreAdd);
                    logger.info("最终得分情况：标答={}，总分={}，无处理实际得分={}，处理后得分={}，匹配到的句子={}",phraseCard.getContent(),
                            phraseCard.getScore(),phraseCard.getActualScore(),descScoreAdd,phraseStrs);
                    resultDao.insertResult(phraseCard.getContent(),phraseStrs,answerCardId,phraseCard.getActualScore(),descScoreAdd,similarity,questionRecordType);
                }else {
                    desScoreMap.put(descId,actualScore);
                    scoreLast += actualScore;
                    logger.info("最终得分情况：标答={}，总分={}，无处理实际得分={}，处理后得分={}，匹配到的句子={}",phraseCard.getContent(),
                            phraseCard.getScore(),phraseCard.getActualScore(),actualScore,phraseStrs);
                    resultDao.insertResult(phraseCard.getContent(),phraseStrs,answerCardId,phraseCard.getActualScore(),actualScore,similarity,questionRecordType);
                }
            }
            scorePointIds.add(""+scorePointId);
        }
        result.add(""+scoreLast);
        result.addAll(scorePointIds);
    }

    public void assembleUserAnswer(String userAnswer, List<AnswerSentence> sentences){
        List<AnswerParagraph> paragraphs = commonUtil.cutParagraph(userAnswer);
        for(AnswerParagraph paragraph:paragraphs){
            String content = paragraph.getContent();
            List<String> sentenceStrs = commonUtil.cutSentences(content,1);
            produceSentence(sentenceStrs,sentences,paragraph);
        }
    }


    /**
     * 用户答案短语和标准答案短语进行语义匹配
     * @param standardAnswerDecs
     * @param userSentences 用于计算的的用户语句
     * @param phraseCards
     * @throws Exception
     */
    public void userAnswerAndStandardAnswer(List<InterviewScoreDescription> standardAnswerDecs, List<AnswerSentence> userSentences,
                                                    List<InterviewAnswerPhraseCard> phraseCards) throws Exception {
        //一一匹配
        for (InterviewScoreDescription standardAnswerDec : standardAnswerDecs){
            List<InterviewScorePoint> scorePoints = standardAnswerDec.getScorePoints();
            for(InterviewScorePoint scorePoint:scorePoints){
                findMostSimilarPhraseV1(userSentences,scorePoint,phraseCards,standardAnswerDec.getScoreDescId(),standardAnswerDec.getScore());
            }
        }

        //二一匹配
        for (InterviewScoreDescription standardAnswerDec : standardAnswerDecs){
            List<InterviewScorePoint> scorePoints = standardAnswerDec.getScorePoints();
            for(InterviewScorePoint scorePoint:scorePoints){
                findMostSimilarPhraseV2(userSentences,scorePoint,phraseCards,standardAnswerDec.getScoreDescId(),standardAnswerDec.getScore());
            }
        }
    }

    /**
     * 用户答案短语和标准答案短语进行语义二一匹配
     * @param userSentences
     * @param scorePoint
     * @param phraseCards
     * @param descId
     * @param descScore
     */
    public void findMostSimilarPhraseV2( List<AnswerSentence> userSentences,InterviewScorePoint scorePoint,
                                         List<InterviewAnswerPhraseCard> phraseCards,long descId,double descScore) {
        double standardScore = scorePoint.getScore();
        double score = 0.0;
        double sim = 0.0;
        int sPos = 0;//记录用户句子的位置
        int pPos1 = 0;//记录短语在用户句子的位置
        int pPos2 = 0;//记录短语在用户句子的位置
        for(int i=0,size=userSentences.size();i<size;i++){
            AnswerSentence userSentence = userSentences.get(i);
            List<AnswerPhrase> userAnswerPhrases = userSentence.getPhrases();
            int size1 = userAnswerPhrases.size();
            for(int m = 0; m<size1; m++){
                AnswerPhrase userAnswerPhrase1 = userAnswerPhrases.get(m);
                if(userAnswerPhrase1.getStatus() == AnswerPhraseStatus.HAVE_MATCH){
                    continue;
                }else{
                    List<String> userPhraseWords1 = userAnswerPhrase1.getWords();
                    int size2 = m+4>size1?size1:(m+4);//只组合匹配后3句
                    for(int n=m+1;n<size2;n++){
                        AnswerPhrase userAnswerPhrase2 = userAnswerPhrases.get(n);
                        if(userAnswerPhrase2.getStatus() == AnswerPhraseStatus.HAVE_MATCH){
                            continue;
                        }else{
                            List<String> userPhraseWords2 = userAnswerPhrase2.getWords();
                            List<String> userPhraseWords = new ArrayList<>();
                            userPhraseWords.addAll(userPhraseWords1);
                            userPhraseWords.addAll(userPhraseWords2);
                            String userPhraseContent =  userAnswerPhrase1.getContent()+" "+userAnswerPhrase2.getContent();
                            List<Double> temps = simTwoPhrase(scorePoint,userPhraseWords,userPhraseContent);
                            double scoreTemp = temps.get(1);
                            double simTemp = temps.get(0);
                            if(simTemp>sim&&scoreTemp>0){
                                score = scoreTemp;
                                sPos = i;
                                pPos1 = m;
                                pPos2 = n;
                                sim = simTemp;
                            }
                        }
                    }
                }
            }
        }
        if(score>0){
            AnswerPhrase userAnswerPhrase1 = userSentences.get(sPos).getPhrases().get(pPos1);
            AnswerPhrase userAnswerPhrase2 = userSentences.get(sPos).getPhrases().get(pPos2);
            userAnswerPhrase1.setStatus(AnswerPhraseStatus.HAVE_MATCH);
            userAnswerPhrase2.setStatus(AnswerPhraseStatus.HAVE_MATCH);
            String phraseContent1 = userAnswerPhrase1.getContent();
            String phraseContent2 = userAnswerPhrase2.getContent();
            int start1 = userAnswerPhrase1.getStart(),start2 = userAnswerPhrase2.getStart();
            int end1 = start1+phraseContent1.length(),end2 = start2+phraseContent2.length();
            List<Integer> starts = new ArrayList<Integer>(){{add(start1);add(start2);}};
            List<Integer> ends = new ArrayList<Integer>(){{add(end1);add(end2);}};
            List<String> answerPhraseStrs = new ArrayList<String>(){{add(phraseContent1);add(phraseContent2);}};
            InterviewAnswerPhraseCard phraseCard = InterviewAnswerPhraseCard.builder()
                    .starts(starts)
                    .ends(ends)
                    .sim(sim)
                    .answerPhrases(answerPhraseStrs)
                    .content(scorePoint.getScorePointContent())
                    .score(standardScore)
                    .actualScore(score)
                    .scorePointId(scorePoint.getScorePointId())
                    .descId(descId)
                    .descFullMark(descScore)
                    .build();
            updateCards(phraseCards,phraseCard,userSentences);
            logger.info("二一匹配标答短句={}，用答短句={}，此时得分={}，这时的相似度={}",phraseCard.getContent(),phraseCard.getAnswerPhrases().get(0)+","+phraseCard.getAnswerPhrases().get(1),phraseCard.getActualScore(),phraseCard.getSim());
        }
    }

    /**
     * 用户答案短语和标准答案短语进行语义一一匹配
     * @param userSentences
     * @param scorePoint
     * @param phraseCards
     * @param descId
     * @param descScore
     */
    public void findMostSimilarPhraseV1(List<AnswerSentence> userSentences,InterviewScorePoint scorePoint,
                                    List<InterviewAnswerPhraseCard> phraseCards,long descId,double descScore){
        double standardScore = scorePoint.getScore();
        double score = 0.0;
        double sim = 0.0;
        int sPos = 0;//记录用户句子的位置
        int pPos = 0;//记录短语在用户句子的位置
        for(int i=0,size1=userSentences.size();i<size1;i++){
            AnswerSentence userSentence = userSentences.get(i);
            List<AnswerPhrase> userAnswerPhrases = userSentence.getPhrases();
            for(int j = 0, size2 = userAnswerPhrases.size(); j<size2; j++){
                AnswerPhrase userPhrase = userAnswerPhrases.get(j);
                if(userPhrase.getStatus() == AnswerPhraseStatus.HAVE_MATCH){//表示该短句已经被匹配了
                    continue;
                }else{
                    List<Double> temps = simTwoPhrase(scorePoint,userPhrase.getWords(),userPhrase.getContent());
                    double scoreTemp = temps.get(1);
                    double simTemp = temps.get(0);
                    if(simTemp>sim&&scoreTemp>0){
                        score = scoreTemp;
                        sPos = i;
                        pPos = j;
                        sim = simTemp;
                    }
                }
            }
        }
        double ratio = score/standardScore;
        if(ratio>0.5){
            AnswerPhrase userAnswerPhrase = userSentences.get(sPos).getPhrases().get(pPos);
            userAnswerPhrase.setStatus(AnswerPhraseStatus.HAVE_MATCH);
            String phraseContent = userAnswerPhrase.getContent();
            int start = userAnswerPhrase.getStart();
            int end = start+phraseContent.length();
            List<Integer> starts = new ArrayList<Integer>(){{add(start);}};
            List<Integer> ends = new ArrayList<Integer>(){{add(end);}};
            List<String> answerPhraseStrs = new ArrayList<String>(){{add(phraseContent);}};
            InterviewAnswerPhraseCard phraseCard = InterviewAnswerPhraseCard.builder()
                    .starts(starts)
                    .ends(ends)
                    .sim(sim)
                    .answerPhrases(answerPhraseStrs)
                    .content(scorePoint.getScorePointContent())
                    .score(standardScore)
                    .actualScore(score)
                    .scorePointId(scorePoint.getScorePointId())
                    .descId(descId)
                    .descFullMark(descScore)
                    .build();
            updateCards(phraseCards,phraseCard,userSentences);
            logger.info("一一标答短句={}，用答短句={}，此时的得分为={}，这时的相似度={}",phraseCard.getContent(),phraseCard.getAnswerPhrases().get(0),phraseCard.getActualScore(),phraseCard.getSim());
        }
    }


    public List<Double> simTwoPhrase(InterviewScorePoint scorePoint,List<String> userWords,String userPhraseContent){
        List<Double> temp = new ArrayList<>();
        List<String> standardWords = commonUtil.cutWord(scorePoint.getScorePointContent(),1);
        double simPhraseTemp = 0;
        if(standardWords!=null){
            simPhraseTemp = semantic.simWordsStr(standardWords,userWords);
        }
        if(Double.isNaN(simPhraseTemp)){
            simPhraseTemp = 0;
        }
        double scoreTemp = commonUtil.scoreSim(scorePoint.getScore(),simPhraseTemp);
        double keyScore = matchWord(scorePoint.getKeyWords(),userPhraseContent);
        /*if(standardWords==null){
            keyScore = scorePoint.getScore();
        }*/
        scoreTemp = scoreTemp>keyScore?scoreTemp:keyScore;
        double simTemp = simPhraseTemp+keyScore/ scorePoint.getScore();
        if(CollectionUtils.isNotEmpty(temp)){
            if(simTemp>temp.get(0)){
                temp.set(0,simTemp);
                temp.set(1,scoreTemp);
            }
        }else {
            temp.add(simTemp);
            temp.add(scoreTemp);
        }
        return temp;
    }

    /**
     * 匹配词
     * @param keyWords
     * @param phrase
     * @return
     */
    public double matchWord(List<InterviewKeyWord> keyWords,String phrase){
        if(CollectionUtils.isEmpty(keyWords)){
            return 0.0;
        }
        double score = 0;
        for(InterviewKeyWord keyWord:keyWords){
            if(matchKeyWord(keyWord,phrase)){//匹配关键词及替换词
                score += keyWord.getScore();
            }
        }
        return score;
    }

    /**
     * 匹配关键词
     * @param keyWord
     * @param phrase
     * @return
     */
    public boolean matchKeyWord(InterviewKeyWord keyWord,String phrase){
        List<String> splitWords = keyWord.getSplitWords();
        String keyWordItem = keyWord.getKeyWord();
        String pattern = producePattern(keyWordItem,splitWords,0);
        return matchPattern(pattern,phrase);
    }

    /**
     * 正则匹配
     * @param pattern
     * @param phrase
     * @return
     */
    public boolean matchPattern(String pattern,String phrase){
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(phrase);
        while(m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 生成正则
     * @param item
     * @param splitWords
     * @type 0代表词和拆分词需要找近义词，1代表仅仅词需要找近义词，2代表仅仅拆分词需要找近义词，3不需要近义词
     * @return
     */
    public String producePattern(String item,List<String> splitWords,int type){
        String pattern = "(?=.*?("+item+"))";
        if(type==0||type==1){
            List<String> synonyms = commonUtil.findSynonymByWord(item);
            if(CollectionUtils.isNotEmpty(synonyms)){
                for (int j=0,size=synonyms.size();j<size;j++){
                    pattern += ("|"+synonyms.get(j));
                }
            }
        }

        String patternSplits = "";//所有切分词的正则，模式为(?=.*?(X|X))(?=.*?(X|X))(?=.*?(X|X))或者
        if(type==0||type==2){
            if(CollectionUtils.isNotEmpty(splitWords)){
                for(String splitWord:splitWords){
                    patternSplits += "(?=.*?("+splitWord;
                    List<String> synonymsSplit = commonUtil.findSynonymByWord(splitWord);
                    if(CollectionUtils.isNotEmpty(synonymsSplit)){
                        for (int j=0,size=synonymsSplit.size();j<size;j++){
                            patternSplits += ("|"+synonymsSplit.get(j));
                        }
                    }
                    patternSplits += "))";
                }
            }
        }else{
            if(CollectionUtils.isNotEmpty(splitWords)){
                for(String splitWord:splitWords){
                    patternSplits += "(?=.*?"+splitWord+")";
                }
            }
        }
        if(patternSplits.length()>0){
            pattern += "|"+patternSplits;
        }
        return pattern;
    }

    /**
     * 修改答题卡
     * @param phraseCards
     * @param phraseCard
     * @param userAllSentences
     */
    private void updateCards(List<InterviewAnswerPhraseCard> phraseCards, InterviewAnswerPhraseCard phraseCard, List<AnswerSentence> userAllSentences){
        int sign = 0;
        for(int i=0,size=phraseCards.size();i<size;i++){
            InterviewAnswerPhraseCard card = phraseCards.get(i);
            if(card.getContent().equals(phraseCard.getContent())){
                if(phraseCard.getScore()>card.getScore()||(phraseCard.getScore()==card.getScore()&&phraseCard.getSim()>card.getSim())){
                    List<Integer> starts = card.getStarts();
                    List<Integer> ends = card.getEnds();
                    phraseCards.set(i,phraseCard);
                    for(int j=0,size1=starts.size();j<size1;j++){
                        int start = starts.get(j);
                        int end = ends.get(j);
                        userPhraseToNoMatch(start,end,userAllSentences);
                    }
                }else{
                    List<Integer> starts1 = card.getStarts();
                    List<Integer> ends1 = card.getEnds();
                    List<Integer> starts = phraseCard.getStarts();
                    List<Integer> ends = phraseCard.getEnds();
                    starts.removeAll(starts1);
                    ends.removeAll(ends1);
                    for(int j=0,size1=starts.size();j<size1;j++){
                        int start = starts.get(j);
                        int end = ends.get(j);
                        userPhraseToNoMatch(start,end,userAllSentences);
                    }
                }
                sign = 1;
                break;
            }
        }
        if(sign==0){
            phraseCards.add(phraseCard);
        }
    }

    /**
     * 标记用户答案是否已经找到
     * @param start
     * @param end
     * @param userAllSentences
     */
    private void userPhraseToNoMatch(int start,int end,List<AnswerSentence> userAllSentences){
        for(AnswerSentence userSentence:userAllSentences){
            int temp = 0;//标记是否已经找到，1代表已经找到，0未找到
            List<AnswerPhrase> userPhrases = userSentence.getPhrases();
            for(int i=0,size=userPhrases.size();i<size;i++){
                AnswerPhrase userPhrase = userPhrases.get(i);
                int phraseStart = userPhrase.getStart();
                int phraseEnd = phraseStart+userPhrase.getContent().length();
                if(phraseStart==start&&phraseEnd==end){
                    userPhrase.setStatus(AnswerPhraseStatus.NO_MATCH);
                    temp = 1;
                    break;
                }
            }
            if(temp==1){
                break;
            }
        }
    }

    /**
     * 生成句子
     * @param userSentencesContent
     * @param userSentences
     * @param paragraph
     */
    private void produceSentence(List<String> userSentencesContent,List<AnswerSentence> userSentences,AnswerParagraph paragraph){
        List<Integer> sentenceStarts = new ArrayList<>();
        List<Integer> sentenceEnds = new ArrayList<>();
        for(int j=0,size1=userSentencesContent.size();j<size1;j++){
            String userSentenceContent = userSentencesContent.get(j);
            int sentenceStart = commonUtil.findStart(paragraph.getStart(),paragraph.getContent(),userSentenceContent,sentenceStarts,sentenceEnds);
            int sentenceEnd = sentenceStart+userSentenceContent.length();
            sentenceStarts.add(sentenceStart);
            sentenceEnds.add(sentenceEnd);
            List<String> phrasesContent = commonUtil.cutSentences(userSentenceContent,2);
            List<AnswerPhrase> answerPhrases = new ArrayList<>();
            List<Integer> phraseStarts = new ArrayList<>();
            List<Integer> phraseEnds = new ArrayList<>();
            for(int k=0,size2=phrasesContent.size();k<size2;k++){
                //logger.info("需要切词的短句为={}",phrasesContent.get(k));
                int phraseStart = commonUtil.findStart(sentenceStart,userSentenceContent,phrasesContent.get(k),phraseStarts,phraseEnds);
                int phraseEnd = phraseStart+phrasesContent.get(k).length();
                phraseStarts.add(phraseStart);
                phraseEnds.add(phraseEnd);

                List<String> phraseWords = commonUtil.cutWord(phrasesContent.get(k),1);

                AnswerPhrase answerPhrase = AnswerPhrase.builder()
                        .content(phrasesContent.get(k))
                        .words(phraseWords)
                        .start(phraseStart)
                        .build();
                answerPhrases.add(answerPhrase);
            }
            AnswerSentence userSentence = AnswerSentence.builder()
                    .sentence(userSentenceContent)
                    .phrases(answerPhrases)
                    .start(sentenceStart)
                    .build();
            userSentences.add(userSentence);
        }
    }
}
