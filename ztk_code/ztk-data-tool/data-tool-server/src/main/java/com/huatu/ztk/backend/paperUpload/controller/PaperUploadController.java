package com.huatu.ztk.backend.paperUpload.controller;

import com.huatu.ztk.backend.paperUpload.bean.PaperUploadError;
import com.huatu.ztk.backend.paperUpload.bean.PaperUploadRedisKeys;
import com.huatu.ztk.backend.paperUpload.service.PaperFrame;
import com.huatu.ztk.backend.paperUpload.service.PaperUploadService;
import com.huatu.ztk.backend.paperUpload.service.RecommendStatus;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.ErrorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lenovo on 2017/4/20.
 */
@RestController
@RequestMapping("/paperUpload")
public class PaperUploadController {
    private static final Logger logger = LoggerFactory.getLogger(PaperUploadController.class);
    private static final String filePath = "disconf\\download\\";
    @Autowired
    private PaperUploadService paperUploadService;
    @Autowired
    private PaperFrame paperFrame;
    @Autowired
    private RecommendStatus recommendStatus;
    /**
     *
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value = "/uploadpre", method = RequestMethod.POST,consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object addPaperByWord(@RequestParam(value = "file") MultipartFile file,
                       @RequestParam long time,
                       HttpServletRequest request) throws Exception  {
        long start = System.currentTimeMillis();
        String key = PaperUploadRedisKeys.paper_recommend_hashMap_key;
        if (file != null) {
            //获取文件数据
            paperFrame.clearLoggerList();
            paperUploadService.clearLoggerList();
            //判断是否有两次交易的时间点一致（低概率事件）
            if(!recommendStatus.getRedisStatus(time+"",key).isEmpty()){
                throw new BizException(ErrorResult.create(1000001,"时间冲突，请重新上传"));
            }
            recommendStatus.putAllRedisStatus(time+"","0","接收word文档...",key);
            File dest = new File( filePath+UUID.randomUUID()+file.getOriginalFilename());
            file.transferTo(dest);
            dest.deleteOnExit();
            final HttpSession session = request.getSession(true);
            User user = (User) session.getAttribute("user");
            int uid = 1;
            if(user!=null){
                uid = user.getId();
            }
            recommendStatus.putAllRedisStatus(time+"","10","解析word文档...",key);
            //得到html文件路径
            String htmlPath = paperUploadService.docToHtml(dest);
            recommendStatus.putAllRedisStatus(time+"","30","分析html文档...",key);
            //解析HTML标签得到所有<p>标签内容
            LinkedList<String> eleList = paperUploadService.anayleHtml(htmlPath);
            recommendStatus.putAllRedisStatus(time+"","50","分析创建试卷...",key);
            //解析试卷相关的<p>标签内容，创建试卷
            List<Map> paperList = paperFrame.initPaperInfo(eleList,uid);
            recommendStatus.putAllRedisStatus(time+"","70","分析试题...",key);
            final Map<String,LinkedList> mapData ;
            //解析模块和试题信息
            try{
                mapData = paperFrame.dealAndAddQuestionPre(paperList.get(0),eleList,uid);
                recommendStatus.putAllRedisStatus(time+"","95","创建试题...",key);
            }catch (BizException e){
                paperFrame.delPaper(paperList);
                return e.getErrorResult();
            }
            for(Map paper:paperList ){
                //组装试题对象，并添加模块和试题
                paperFrame.dealAndAddQuestionExecute(paper,mapData,uid);
            }
            recommendStatus.putAllRedisStatus(time+"","100","成功",key);
        } else {
            logger.info("文件录入失败");
            return ErrorResult.create(10000,"文件录入失败");

        }
        long end = System.currentTimeMillis();
        System.out.println("试卷录入时间为："+(end-start));
        PaperUploadError success = paperFrame.getSuccessInfo();
        ErrorResult result = ErrorResult.create(1000000,success.getErrorMsg());
        result.setData(success.getErrorFlag());
        recommendStatus.deleteRedisStatus(time+"",key);
        return result;
    }
    /**
     * 查看进度
     */
    @RequestMapping(value = "/status", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object addRecommend(@RequestParam String time) throws BizException {
        String key =PaperUploadRedisKeys.paper_recommend_hashMap_key;
        Map<String,Object> mapData = recommendStatus.getRedisStatus(key);
        String code_key = "code_"+time;
        String code_value = "message_"+time;
        if(mapData.get(code_key)==null){
            ErrorResult result = ErrorResult.create(0,"");
            return result;
        }
        int status = Integer.parseInt(String.valueOf(mapData.get(code_key)));
        ErrorResult result = ErrorResult.create(status,mapData.get(code_value).toString());
        if(status==100){
            recommendStatus.deleteRedisStatus(time,key);
        }
        return result;
    }
}
