package com.huatu.ztk.backend.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huatu.ztk.backend.paper.bean.PaperBean;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.util.FuncStr;
import com.huatu.ztk.paper.bean.Paper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\2 0002.
 */
@Service
public class PaperServiceV1 {
    private static final Logger logger = LoggerFactory.getLogger(PaperService.class);
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    private PaperDao paperDao;
    @Autowired
    private CreatePaperWordServiceV1 createPaperWordService;
    @Autowired
    private PaperService paperService;
    public List<PaperBean> allDownListV1(String catgory, String area, int sYear, int eYear, int paperType, int id, String name) {
        List<Paper> papers = Lists.newArrayList();
        if(id>0){
            papers.add(paperDao.findById(id));
        }else{
            List<Integer> areas = area.equals("0") ? Lists.newArrayList()
                    : Arrays.stream(area.split(","))
                    .map(Integer::new)
                    .collect(Collectors.toList());
            List<Paper> paperList = paperDao.allDownListV1(paperService.getCatgoryIds(catgory), areas, sYear, eYear,paperType,name);
            papers.addAll(paperList);
        }
        return paperService.checkoutRecommend(papers);
    }

    /**
     * 创建试卷试题word文档
     * @param paperIds
     * @throws Exception
     */
    public Object createFile(String paperIds) throws Exception {
        List<String> urls = Lists.newArrayList();
        if (StringUtils.isNotEmpty(paperIds)) {
            String[] paperIdArr = paperIds.split(",");
            for (String paperId : paperIdArr) {
                Paper paper = paperService.findPaperById(Integer.parseInt(paperId));
                //生成word文件整体版
                String fileDocName = FuncStr.replaceDiagonal(paper.getName()) + ".doc";
//                createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_ALL, fileDocName, null);
                createPaperWordService.createUploadFileWord(paper, ExportType.PAPER_WORD_TYPE_SIDE_STEM, fileDocName, null);
                urls.add("http://tiku.huatu.com/cdn/paper/word/"+fileDocName);
            }
        }
        return urls;
    }

    /**
     * 查询所有的模考真题演练和模考大赛
     * @param subjectId
     */
    public void syncQuestionsBySubject(int subjectId) {
        long start = System.currentTimeMillis();
        Long total = paperDao.countBySubject(subjectId);
        int cursor = 0;
        int size = 100;
        int count = 0;
        List<Integer> questionIds = Lists.newArrayList();
        while(true){
            List<Paper> paperList = paperDao.findQuestionsForPage(cursor,size,subjectId);
            if(CollectionUtils.isEmpty(paperList)){
                logger.info("已无试题需要处理，进程结束");
                break;
            }
            cursor = paperList.get(paperList.size()-1).getId();
            for (Paper paper : paperList) {
                if(paper.getStatus()==4){
                    continue;
                }
                Map map = Maps.newHashMap();
                map.put("id",paper.getId());
                rabbitTemplate.convertAndSend("","sync_question_2_db",map);
            }
            count += paperList.size();
            logger.info("刷新进程：+++++{}/{}",count,total);
        }
        long end = System.currentTimeMillis();
        logger.info("刷新需要时间：{}",(end-start)/1000);
        logger.info("questionIds={}",questionIds);
    }
}
