package com.huatu.ztk.backend.paperUpload.service;

import com.huatu.ztk.backend.paper.bean.BackendPaperStatus;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.paper.service.PaperService;
import com.huatu.ztk.backend.paperUpload.bean.PaperAttr;
import com.huatu.ztk.backend.paperUpload.bean.PaperUploadError;
import com.huatu.ztk.backend.paperUpload.bean.UpLoadAttr;
import com.huatu.ztk.backend.paperUpload.dao.LogIterator;
import com.huatu.ztk.backend.subject.bean.SubjectBean;
import com.huatu.ztk.backend.subject.service.SubjectService;
import com.huatu.ztk.commons.Area;
import com.huatu.ztk.commons.AreaConstants;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.paper.bean.Paper;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lenovo on 2017/6/12.
 */
@Service
public class InitPaperService extends LogIterator{
    private static Logger logger = LoggerFactory.getLogger(InitPaperService.class);
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private PaperService paperService;
    @Autowired
    private PaperDao paperDao;
    public List<Map> initPaperInfo(LinkedList<String> eleList, int uid) throws Exception {
        boolean flag = true;
        Map paper = new HashMap();
        int status = 0; //0表示还未开始，1表示开始
        while(flag){
            if(CollectionUtils.isEmpty(eleList)||eleList.size()==0){
                break;
            }
            String eleStr = eleList.getFirst();
            //试卷名称
            if(status==0&&checkPaperName(eleStr,paper)==1){
                eleList.removeFirst();
                status = 1;
                continue;
            }
            //科目
            if(status == 1&&checkSubjectName(eleStr,paper,uid)){
                eleList.removeFirst();
                continue;
            }
            //地区
            if(status == 1&&checkAreaName(eleStr,paper)){
                eleList.removeFirst();
                continue;
            }
            if(status == 1&&checkExtPaperInfo(eleStr,paper)){
                eleList.removeFirst();
                continue;
            }
            //试卷类型
            if(status == 1&&checkPaperType(eleStr,paper)){
                eleList.removeFirst();
                break;
            }
            Pattern pattern = Pattern.compile("【"+PaperAttr.QUESTION_MODULE+"】");
            Matcher matcher = pattern.matcher(eleStr.trim());
            if(matcher.find()){
                break;
            }
            eleList.removeFirst();
        }
        for(String str: UpLoadAttr.paperAttrMap.keySet()){
            if(paper.get(str)==null){
                String error = "试卷属性\""+UpLoadAttr.paperAttrMap.get(str)+"\"未匹配到";
                logger.error(error);
                this.setLoggerList(PaperUploadError.builder()
                        .errorMsg(error).errorType("error")
                        .floor(logger.getName()).errorFlag("paperAttr").build());
            }
        }
        if(isLoggerError("paperAttr")){
            List tempList = new ArrayList();
            tempList.add(paper);
            paper.put("paperId",-1);
            paper.put("id","");
            return tempList;
        }
        paper.put("type",1);
        return creatPaper(paper,uid);
    }
    private boolean checkPaperType(String eleStr, Map paper) {
        Pattern pattern = Pattern.compile("【"+PaperAttr.PAPER_TIME+"】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            paper.put("time",Integer.parseInt(eleStr.trim().substring(matcher.end()).trim()));
            return true;
        }
        return false;
    }

    private boolean checkExtPaperInfo(String eleStr, Map paper) throws Exception{
        //年份
        //总分
        //答题时限
        Pattern pattern = Pattern.compile("【"+PaperAttr.PAPER_YEAR+"】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            paper.put("year",Integer.parseInt(eleStr.trim().substring(matcher.end()).trim()));
            return true;
        }
        pattern = Pattern.compile("【"+PaperAttr.PAPER_SCORE+"】");
        matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            paper.put("score",Integer.parseInt(eleStr.trim().substring(matcher.end()).trim()));
            return true;
        }

        return false;
    }

    private boolean checkAreaName(String eleStr, Map paper) throws Exception {
        Pattern pattern = Pattern.compile("【"+PaperAttr.PAPER_AREA+"】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            String name = eleStr.trim().substring(matcher.end()).trim();
            List<Area> areaList = AreaConstants.getAreas(2);
            String[] names = name.split(",|，");
            String areas = "";
            for(String str :names){
                int l = getAreaId(str,areaList);
                if(l==-1){
                    String error = "地区\""+str+"\"不可查";
                    logger.error(error);
                    this.setLoggerList(PaperUploadError.builder()
                            .floor(logger.getName())
                            .errorType("error").errorMsg(error)
                            .errorFlag("paperAttr").build());
                }
                areas = areas+l+",";
            }
            areas = areas.substring(0,areas.length()-1);
            paper.put("areas",areas);
            return true;
        }
        return false;
    }
    private boolean checkSubjectName(String eleStr, Map paper,int uid) throws Exception {
        Pattern pattern = Pattern.compile("【"+PaperAttr.PAPER_SUBJECT+"】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            List<SubjectBean> subjectList = subjectService.findList(0,uid);
            String name = eleStr.trim().substring(matcher.end()).trim();
            int subjectId = -1;
            for(SubjectBean subject : subjectList){
                if(name.equals(subject.getName())){
                    subjectId = subject.getId();
                    break;
                }
            }
            if(subjectId==-1){
                String error ="科目：\""+name+"\"不存在所选范围内";
                logger.error(error);
                this.setLoggerList(PaperUploadError.builder()
                        .floor(logger.getName()).errorType("error").errorFlag("paperAttr")
                        .errorMsg(error).build());
            }
            paper.put("catgory",subjectId);
            return true;
        }
        return false;
    }


    private int checkPaperName(String eleStr,Map paper) {
        Pattern pattern = Pattern.compile("【"+PaperAttr.PAPER_NAME+"】");
        Matcher matcher = pattern.matcher(eleStr.trim());
        if(matcher.find()){
            paper.put("name", eleStr.trim().substring(matcher.end()).trim());
            return 1;
        }
        return 0;
    }
    private List<Map> creatPaper(Map paper, int uid) throws BizException {
        Paper paperTemp = new Paper();
        paperTemp.setType(Integer.parseInt(String.valueOf(paper.get("type"))));  //指定为真题
        paperTemp.setCatgory(Integer.parseInt(String.valueOf(paper.get("catgory"))));
        paperTemp.setYear(Integer.parseInt(String.valueOf(paper.get("year"))));
        paperTemp.setName(String.valueOf(paper.get("name")));
        paperTemp.setScore(Integer.parseInt(String.valueOf(paper.get("score")).trim()));
        paperTemp.setTime(Integer.parseInt(String.valueOf(paper.get("time")))*60);
        paperTemp.setCreateTime(new Date());
        paperTemp.setStatus(BackendPaperStatus.CREATED);
        paperTemp.setCreatedBy((int) uid);
        paper.put("status",BackendPaperStatus.CREATED);
        int[] area = Arrays.stream(paper.get("areas").toString().split(",")).mapToInt(Integer::valueOf).toArray();
        List<Map> result = new ArrayList<>();
        int paperId = paperDao.findMaxId(area.length);
        for (int i = 0; i < area.length; i++) {
            paperTemp.setArea(area[i]);
            paperService.checkPaper(paperTemp);
            paperTemp.setId(paperId);
            paperDao.createPaper(paperTemp);
            Map paperMap = new HashMap();
            paperMap.putAll(paper);
            paperMap.put("area",area[i]);
            paperMap.put("id",paperId);
            if(i!=0){
                paperMap.put("questionStatus",4);
            }else{
                paperMap.put("questionStatus",1);
            }
            result.add(paperMap);
            paperId++;
        }
        return result;
    }
    private int getAreaId(String str, List<Area> areaList) throws Exception {
        int l = -1;
        for(Area area:areaList){
            if(str.contains(area.getName())){
                l= area.getId();
                return l;
            }
            if(area.getChildren()!=null){
                l = getAreaId(str,area.getChildren());
                if(l!=-1){
                    return l;
                }
            }

        }
        return l;
    }
}
