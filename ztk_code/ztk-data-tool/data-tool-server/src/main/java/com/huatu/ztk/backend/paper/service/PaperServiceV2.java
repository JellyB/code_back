package com.huatu.ztk.backend.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paper.dao.MatchDao;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.backend.util.FunFileUtils;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.backend.util.UploadFileUtil;
import com.huatu.ztk.commons.SubjectType;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.EstimatePaper;
import com.huatu.ztk.paper.bean.Match;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.paper.common.PaperRedisKeys;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by huangqp on 2018\4\2 0002.
 */
@Service
public class PaperServiceV2 {
    private static final Logger logger = LoggerFactory.getLogger(PaperService.class);

    @Autowired
    private CreatePaperWordServiceV2 createPaperWordService;
    @Autowired
    private PaperService paperService;
    @Autowired
    UploadFileUtil uploadFileUtil;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    MatchDao matchDao;
    @Autowired
    PaperDao paperDao;
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 创建试卷试题word文档
     * @param paperIds
     * @param isReNew
     * @throws Exception
     */
    public String createFile(String paperIds, Integer isReNew) throws Exception {
        List<String> names = Lists.newArrayList();
        if (StringUtils.isNotBlank(paperIds)) {
            String[] paperIdArr = paperIds.split(",");
            for (String paperId : paperIdArr) {
                try{
                    Paper paper = paperService.findPaperById(Integer.parseInt(paperId));
                    insertAll(paper);
                    //生成word文件整体版
                    String fileDocName = FuncStr.replaceDiagonal(paper.getName()) + ".doc";
                    File file = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + fileDocName);
                    if (!FunFileUtils.fileExists(file)||isReNew.intValue()==1) {
                        boolean flag= createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_ALL, file.getName(), file);
                        if(flag){
                            names.add(FuncStr.replaceDiagonal(paper.getName()));
                        }
                    }else{
                        names.add(FuncStr.replaceDiagonal(paper.getName()));
                    }
                }catch (Exception e){
                    logger.info("不存在的paperId:{}",paperId);
                    e.printStackTrace();
                    continue;
                }
            }
        }
        logger.info("要压缩的文件有：{}",names);
        return unzipFile(names);
    }

    /**
     * 将试卷中的试题重新存入库中
     * @param paper
     */
    private void insertAll(Paper paper) {
        List<Integer> questions = Lists.newArrayList();
        questions.addAll(paper.getQuestions());
        if(CollectionUtils.isEmpty(questions)){
            return;
        }
        Integer paperId = paper.getId();
        List<Integer> questionList =  questionDao.findExportQuestion(paperId);
        if(CollectionUtils.isEmpty(questionList)){
            questionDao.insertExportQuestion(paperId,questions);
            return;
        }
        Collection<Integer> collects =  CollectionUtils.intersection(questionList,questions);
        questionList.removeIf(i->collects.contains(i));
        questions.removeIf(i->collects.contains(i));
        if(CollectionUtils.isNotEmpty(questions)){
            questionDao.insertExportQuestion(paperId,questions);
        }
        if(CollectionUtils.isNotEmpty(questionList)){
            questionDao.deleteExportQuestion(paperId,questions);
        }

    }

    private String unzipFile(List<String> names) throws Exception {

        String zipName = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        boolean bln = FunFileUtils.unzipFile(zipName, 1, names);
        File fileZip = new File(FunFileUtils.TMP_WORD_SOURCE_FILEPATH + zipName + ".zip");
        if (bln) {
            //ftp上传文件  ?  windows 系统 ，上次服务器放开注释
            uploadFileUtil.ftpUploadFile(fileZip, new String(fileZip.getName().getBytes("UTF-8"), "iso-8859-1"), FunFileUtils.WORD_FILE_SAVE_PATH);
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            //下载地址
            return FunFileUtils.WORD_FILE_SAVE_URL + zipName + ".zip";
        } else {
            //删除临时文件
            FunFileUtils.deleteFile(fileZip);
            throw new BizException(ErrorResult.create(1000107, "下载试卷失败"));
        }
    }

    public void movePaperDB2Mongo(Integer matchId, Integer paperId) {
        Match match = matchDao.findById(matchId);
        if(match==null){
            logger.error("暂无模考大赛信息");
        }
        Paper paper = paperDao.findById(paperId);
        if(paper==null){
            logger.info("试卷copy对象不存在");
        }
        paper.setId(matchId);
        paper.setType(9);
        paper.setStatus(match.getStatus());
        paper.setTime(7200);
        paper.setScore(100);
        paper.setArea(-9);
        paper.setName(match.getName());
        paper.setYear(2019);
        if(paper instanceof EstimatePaper){
            ((EstimatePaper) paper).setStartTime(match.getStartTime());
            ((EstimatePaper) paper).setEndTime(match.getEndTime());
        }
        paperDao.update(paper);
    }

    /**
     * 清除缓存
     * @param paperId
     */
    public void clearPaperInfoCache(Integer paperId) {
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        String key = "paper-web-server."+ PaperRedisKeys.getPaperKey(paperId);
        String result = valueOperations.get(key);
        logger.info("result={}",result);
        redisTemplate.delete(key);
    }

    /**
     * 将公基和职测两个科目下的试卷，合成一张试卷
     * @param matchId
     * @param ids
     */
    public void unionPaper(Integer matchId, List<Integer> ids) {
        Paper paper = paperDao.findById(matchId);
        List<Paper> paperList = paperDao.findByIds(ids);
        Map<Integer,Paper> map = Maps.newHashMap();
        if(CollectionUtils.isNotEmpty(paperList)&&paperList.size()==2){
            for (Paper paper1 : paperList) {
                map.put(paper1.getCatgory(),paper1);
            }
        }
        //模块，试题，大试题组装
        List<Module> moduleList = Lists.newArrayList();
        List<Integer> questions = Lists.newArrayList();
        List<Integer> bigQuestions= Lists.newArrayList();
        //公基部分数据导入
        Paper gPaper = map.get(SubjectType.SYDW_GONGJI);
        Module gModule = Module.builder().category(gPaper.getCatgory()).name("公基部分").qcount(gPaper.getQcount()).build();
        moduleList.add(gModule);
        questions.addAll(gPaper.getQuestions());
        bigQuestions.addAll(gPaper.getBigQuestions());
        //职测部分
        Paper zPaper = map.get(SubjectType.SYDW_XINGCE);
        Module zModule = Module.builder().category(zPaper.getCatgory()).name("职测部分").qcount(zPaper.getQcount()).build();
        moduleList.add(zModule);
        questions.addAll(zPaper.getQuestions());
        bigQuestions.addAll(zPaper.getBigQuestions());
        //塞入数据
        paper.setModules(moduleList);
        paper.setQuestions(questions);
        paper.setQcount(questions.size());
        paperDao.update(paper);
        clearPaperInfoCache(matchId);

    }
}
