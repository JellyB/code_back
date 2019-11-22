package com.huatu.ztk.backend.paper.controller;

import com.huatu.ztk.backend.paper.service.PaperServiceV2;
import com.huatu.ztk.commons.exception.SuccessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by huangqp on 2018\4\2 0002.
 * 所有科目试题数据下载，提供试题ID
 */
@RestController
@RequestMapping(value = "/paper/v2")
public class PaperControllerV2 {
    private static final Logger logger = LoggerFactory.getLogger(PaperControllerV2.class);

    @Autowired
    private PaperServiceV2 paperServiceV2;
    /**
     * 生成id试卷的下载文档
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createPaperFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createPaperFile(@RequestParam String id,@RequestParam Integer isReNew) throws Exception {
        String url = paperServiceV2.createFile(id,isReNew);
        return SuccessMessage.create(url);
    }

    /**
     * 下载试卷带答案
     * @param ids
     * @param isReNew
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/createPaperFiles", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object createPaperFiles(@RequestParam String ids,@RequestParam Integer isReNew) throws Exception {
        String url = paperServiceV2.createFile(ids, isReNew);
        return SuccessMessage.create(url);
    }


    /**
     * 迁移mysql数据到mongo
     * @param matchId
     * @param paperId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/estimate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object movePaperDB2Mongo(@RequestParam Integer matchId,@RequestParam Integer paperId) throws Exception {
        paperServiceV2.movePaperDB2Mongo(matchId,paperId);
        return SuccessMessage.create("迁移成功");
    }


    /**
     * 删除试卷信息app缓存
     * @param paperIds
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/clearCache", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object clearPaperInfoCache(@RequestParam String paperIds) throws Exception {
        List<Integer> collect = Arrays.stream(paperIds.split(",")).map(i -> Integer.parseInt(i)).collect(Collectors.toList());
        for (Integer paperId : collect) {
            paperServiceV2.clearPaperInfoCache(paperId);
        }
        return SuccessMessage.create("删除成功");
    }

    /**
     * 跨科目合并试卷（只适用于事业单位公基和职测部分）
     * @param matchId
     * @param paperIds
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/union/paper", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object unionPaper(@RequestParam Integer matchId,@RequestParam String paperIds) throws Exception {
        List<Integer> ids = Arrays.stream(paperIds.split(",")).map(i->Integer.parseInt(i)).collect(Collectors.toList());
        paperServiceV2.unionPaper(matchId,ids);
        return SuccessMessage.create("迁移成功");
    }

}
