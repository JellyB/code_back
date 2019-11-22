package com.huatu.ztk.backend;//package com.huatu.ztk.backend.paper.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huatu.ztk.backend.metas.service.PracticeMetaService;
import com.huatu.ztk.backend.paper.dao.PaperDao;
import com.huatu.ztk.backend.question.dao.QuestionDao;
import com.huatu.ztk.commons.JsonUtil;
import com.huatu.ztk.paper.bean.Paper;
import com.huatu.ztk.question.bean.Question;
import com.huatu.ztk.question.bean.QuestionExtend;
import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\27 0027.
 */
public class PaperTest extends BaseTestW {
    private final static Logger logger = LoggerFactory.getLogger(PaperTest.class);
    private final static int per_size= 5000;
    @Autowired
    private PracticeMetaService practiceMetaService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private QuestionDao questionDao;
    @Autowired
    private PaperDao paperDao;


    /**
     * @Huangqp 20180508
     * 查询status==0的试题所属的试卷，并对试卷进行分析，做处理方案
     */
    @Test
    public void paperQuestionTest(){
        Query query = new Query(Criteria.where("status").is(0));
        List<Question> questions = mongoTemplate.find(query, Question.class, "ztk_question_new");
        if(CollectionUtils.isEmpty(questions)){
            return;
        }
        List<Integer> qids = questions.stream().map(i->i.getId()).collect(Collectors.toList());
        List<QuestionExtend> extendList = questionDao.findExtendByIds(qids);
        if(CollectionUtils.isEmpty(extendList)){
            return;
        }
        List<List> list = Lists.newArrayList();
        Map<Integer,Float> seqMap = Maps.newHashMap();
        Set<Integer> paperIds = Sets.newHashSet();
        extendList.forEach(i-> {
            paperIds.add(i.getPaperId());
            seqMap.put(i.getQid(),i.getSequence());
        });
        logger.info("paperIds={}",paperIds);
        List<Paper> papers = paperDao.findByIds(paperIds.stream().collect(Collectors.toList()));
        papers.sort(Comparator.comparingInt(Paper::getCatgory));
        Map<Integer,String> paperMap = Maps.newHashMap();
        Map<Integer,List<Integer>> questionMap  = Maps.newHashMap();
        for (Paper paper : papers) {
            if(paper.getStatus()==4){
                continue;
            }
            List<Integer> questionIds  = paper.getQuestions();
            questionIds.removeIf(i->!qids.contains(i));
            if(CollectionUtils.isEmpty(questionIds)){
                continue;
            }
            questionMap.put(paper.getId(),questionIds);
            paperMap.put(paper.getId(),paper.getName());
            System.out.println("需要处理的试卷： "+ paper.getId() + "--" + paper.getName() );
            System.out.println("需要处理的试题： "+ JsonUtil.toJson(questionIds));
            for (Integer questionId : questionIds) {
                List temp = Lists.newArrayList();
                temp.add(paper.getId());
                temp.add(paper.getName());
                temp.add(questionId);
                temp.add(seqMap.get(questionId));
                list.add(temp);
            }
        }
        logger.info("paperMap={}",paperMap);
        logger.info("questionMap={}",questionMap);
        String[] titleRow = {"试卷id","试卷名称","试题id","题序"};
        try {
            writer("C:\\Users\\huangqp\\Desktop\\任务文档","错误试题","xls",list,titleRow);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test1(){
        List<Integer> ids = Lists.newArrayList(3004002,3004004,3004005,3004006,3004007,3004008,3004009,3004012,3004013,3004014,3004015,3004017,3004018,3004019,3004020,3004021,3004022,3004023,3004027,3004028,3004029,3004031,3004032,3004033,3004034,3004035,3004040,3004041,3004045,3004048,3004050,3004054,3004055,3004058,3004088,3004108,3004116,3004125,3004126,3004129,3004141,3004206,3004213,3004223,3004225,3525667,3525668,3525669,3525671,3525672,3525673,3525674,3525675,3525677,3525678,3525685,3525687,3525691,3525692,3525693,3525694,3525695,3525696,3525697,3525698,3525699,3525700,3525701,3525702,3525703,3525704,3525705,3525706,3525707,3525709,3525710,3525711,3525712,3525713,3525714,3525715,3525716,3525740,3525741,3525753,3525775,3525776,3525777,3525778,3525779,3525780,3525791,3525793,3525794,3525796,3525798,3525799,3525801,3525804,3525808,3525809,3525810,3525811,3525812,3525814,3525818,3525819,3525823,3525826,3525827,3525828,3525840,3525842,3525843,3525844,3525846,3525847,3525848,3525850,3525855,3525857,3525859,3525860,3525863,3525865,3525867,3525868,3525870,3525871,3525873,3525874,3525876,3525882,3525883,3525885,3525886,3525888,3525889,3525893,3525896,3525898,3525911,3525913,3525915,3525916,3525917,3525932,3525933,3525934,3525936,3525938,3525940,3525941,3525942,3525944,3525945,3525947,3525952,3525955,3525956,3525965,3525966,3525968,3525971,3525973,3525975,3525977,3525979,3525981,3525982,3525983,3525984,3525991,3526005,3526008,3526009,3526013,3526015,3526095,3526596,3526608,3526613,3526639,3526641,3526642,3526643,3526647,3526648,3526649,3526650,3526651,3526654,3526655,3526656,3526657,3526660,3526661,3526663,3526664,3526695,3526696,3526698,3526703,3526707,3526719,3526724,3526725,3526728,3526732,3526739,3526745,3526746,3526747,3526748,3526749,3526750,3526751,3526752,3526753,3526757,3526759,3526761,3526762,3526763,3526764,3526765,3526766,3526767,3526768,3526769,3526770,3526771,3526772,3526773,3526774,3526775,3526780,3526782,3526783,3526785,3526786,3526788,3526791,3526792,3526796,3526804,3526805,3526809,3526810,3526812,3526814,3526815,3526816,3526818,3526819,3526820,3526822,3526823,3526824,3526825,3526826,3526827,3526828,3526829,3526834,3526835,3526836,3526837,3526838,3526839,3526843,3526856,3526858,3526859,3526860,3526868,3526869,3526870,3526871,3526872,3526873,3526874,3526875,3526877,3526879,3526880,3526892,3526897,3526899,3526900,3526908,3526910,3526921,3526925,3526931);
        List<List> list = Lists.newArrayList();
        List<Paper> papers = paperDao.findByIds(ids);
        for (Paper paper : papers) {
            List<Integer> questions = paper.getQuestions();
            if(CollectionUtils.isEmpty(questions)){
                list.add(Lists.newArrayList(paper.getId(),paper.getName(),0));
            }else{
                list.add(Lists.newArrayList(paper.getId(),paper.getName(),questions.size()));
            }
        }
        String[] titleRow = {"试卷id","试卷名称","题量"};
        try {
            writer("C:\\Users\\x6\\Desktop\\pandora","题量统计","xls",list,titleRow);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writer(String path, String fileName,String fileType,List<List> list,String titleRow[]) throws IOException {
        Workbook wb = null;
        String excelPath = path+ File.separator+fileName+"."+fileType;
        File file = new File(excelPath);
        Sheet sheet =null;
        //创建工作文档对象
        if (!file.exists()) {
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();

            } else if(fileType.equals("xlsx")) {
                wb = new XSSFWorkbook();
            } else {
                logger.error("文件格式不正确");
            }
            //创建sheet对象
            sheet = (Sheet) wb.createSheet("sheet1");
            OutputStream outputStream = new FileOutputStream(excelPath);
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();

        } else {
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();

            } else if(fileType.equals("xlsx")) {
                wb = new XSSFWorkbook();

            } else {
                logger.error("文件格式不正确");
            }
        }
        //创建sheet对象
        if (sheet==null) {
            sheet = (Sheet) wb.createSheet("sheet1");
        }

//
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(HSSFColor.PALE_BLUE.index);
        Row row = sheet.createRow(0);    //创建第二行
        Cell cell ;
        for(int i = 0;i < titleRow.length;i++){
            cell = row.createCell(i);
            cell.setCellValue(titleRow[i]);
            cell.setCellStyle(style); // 样式，居中
            sheet.setColumnWidth(i, 20 * 256);
        }
        row.setHeight((short) 250);

        //循环写入行数据
        int line = 1;
        for (List temp:list) {
            row = (Row) sheet.createRow(line);
            row.setHeight((short) 250);
            for(int i=0;i<temp.size();i++){
                row.createCell(i).setCellValue(temp.get(i).toString());
            }
            line++;
        }

        //创建文件流
        OutputStream stream = new FileOutputStream(excelPath);
        //写入数据
        wb.write(stream);
        //关闭文件流
        stream.close();
    }
}
