package com.huatu.ztk.backend.paperUpload.service;

import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paperUpload.bean.PaperUploadError;
import com.huatu.ztk.backend.paperUpload.dao.LogIterator;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import com.huatu.ztk.knowledge.bean.Module;
import com.huatu.ztk.paper.bean.Paper;
import com.itextpdf.text.BadElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lenovo on 2017/5/15.
 */
@Service
public class PaperFrame extends LogIterator{
    private static Logger logger = LoggerFactory.getLogger(PaperFrame.class);

    @Autowired
    private BatchAddQuestion batchAddQuestion;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private InitPaperService initPaperService;
    @Autowired
    private QuestionsBuildService questionsBuildService;

    //初始化试卷信息
    public List<Map> initPaperInfo(LinkedList<String> eleList, int uid) throws Exception {
        initPaperService.clearLoggerList();
        List<Map> tempList = initPaperService.initPaperInfo(eleList,uid);
        checkLoggerError();
        return tempList;
    }

    /**
     *添加模块信息
     * @param paperId
     * @param moduleAttrList
     */
    private void addMoudleList(int paperId, LinkedList moduleAttrList) {
        Paper paper = paperDao.findById(paperId);
        //可判断试卷是否已有模块
        List<Module> modules = new ArrayList<Module>();
        if(CollectionUtils.isEmpty(moduleAttrList)){
            return;
        }
        for(Object tmp:moduleAttrList){
            if(tmp instanceof Map){
                Module module = Module.builder()
                        .category((int)(((Map) tmp).get("mid")))
                        .name((String)(((Map) tmp).get("name")))
                        .qcount(0)
                        .build();
                modules.add(module);
                paper.setModules(modules);
            }
        }
        paperDao.update(paper);
    }
    public Map<String,LinkedList> dealAndAddQuestionPre(Map paper, LinkedList<String> eleList, int uid) throws Exception{
        questionsBuildService.clearThreadLocal();
        questionsBuildService.clearLoggerList();
        try {
            Map<String, LinkedList> mapData = questionsBuildService.dealAndAddQuestionPre(paper, eleList, uid);
            checkLoggerError();
            return mapData;
        }catch (BizException e){
            questionsBuildService.clearThreadLocal();
            throw e;
        }finally {
            this.addLoggerList(questionsBuildService.getLoggerList());
        }
    }
    public void dealAndAddQuestionExecute(Map paper, Map<String,LinkedList> mapData, int uid) throws BadElementException, BizException, IOException {
        int paperId = Integer.parseInt(String.valueOf(paper.get("id")));
        LinkedList questionList = mapData.get("questionList");
        LinkedList moduleAttrList = mapData.get("moduleAttrList");
        addMoudleList(paperId,moduleAttrList);
        paper.put("qcount",questionsBuildService.getQuestionCount());
        logger.info("cone:count:1:"+questionsBuildService.getQuestionCount());
        questionsBuildService.clearThreadLocal();
        batchAddQuestion.addQuestionList(paper,uid,questionList);
        setLoggerList(PaperUploadError.builder().errorType("success")
                .errorMsg("试卷信息处理成功").floor(logger.getName())
                .errorFlag(paper).build());
    }
    public Map dealAndAddQuestionExecuteNoPaper(Map paper, Map<String,LinkedList> mapData, int uid) throws BadElementException, BizException, IOException {
        LinkedList questionList = mapData.get("questionList");
        paper.put("qcount",questionsBuildService.getQuestionCount());
        questionsBuildService.clearThreadLocal();
        Map tempMap = batchAddQuestion.addQuestionList(paper,uid,questionList);
        setLoggerList(PaperUploadError.builder().errorType("success")
                .errorMsg("试卷信息处理成功").floor(logger.getName())
                .errorFlag(paper).build());
        return tempMap;
    }
    public void delPaper(List<Map> paperList) {
        List list = paperList.stream().map(map -> map.get("id")).collect(Collectors.toList());
        if(list!=null&&!list.isEmpty()&&list.get(0)!=null){
            paperDao.updateBatchPaperStatus(list,BackendPaperStatus.DELETED);
        }
    }
    public void showLoggerList(){
        LinkedList<PaperUploadError> list =getLoggerList();
        String name = logger.getName();
        for(PaperUploadError error:list){
            System.out.println(name+">>>>>"+error.getErrorType()+":"+error.getErrorMsg());
        }
    }
    private void sortErrorsList() {
        LinkedList<PaperUploadError> errorList = getLoggerList();
        errorList.sort((a,b)->(a.getErrorType().compareTo(b.getErrorType())));
        errorList.sort((a,b)->(a.getErrorMsg().compareTo(b.getErrorMsg())));
    }
    private void checkLoggerError() throws BizException {
        sortErrorsList();
        List<PaperUploadError> errorsList = getLoggerList().stream()
                .filter((i)->(i.getErrorType().equals("error")))
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(errorsList)){
            logger.info("cone2");
            String errorMsg = "";
            for(PaperUploadError errors:errorsList){
                errorMsg += errors.getErrorMsg()+"\n";
            }
            ErrorResult result = ErrorResult.create(10001,errorMsg);
            result.setData(errorsList);
            throw new BizException(result);
        }
    }
}
