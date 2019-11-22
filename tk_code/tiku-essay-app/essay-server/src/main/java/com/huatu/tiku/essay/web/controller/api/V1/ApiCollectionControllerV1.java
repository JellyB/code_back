package com.huatu.tiku.essay.web.controller.api.V1;

/**
 * Created by x6 on 2018/1/30.
 */

import com.huatu.common.spring.web.MediaType;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.essay.essayEnum.EssayAnswerCardEnum;
import com.huatu.tiku.essay.vo.resp.EssayUpdateVO;
import com.huatu.tiku.essay.service.UserCollectionService;
import com.huatu.tiku.essay.util.LogPrint;
import com.huatu.tiku.springboot.users.support.Token;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by x6
 * 题目收藏相关
 */
@RestController
@Slf4j
@RequestMapping("api/v1/collect")
public class ApiCollectionControllerV1 {

    @Autowired
    UserCollectionService userCollectionService;

    /**
     * 收藏题目、试卷
     * @return
     */
    @LogPrint
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object saveCollection(@Token UserSession userSession,
                               @RequestHeader int terminal,
                               @RequestHeader String cv,
                               @RequestParam (name = "type", defaultValue = "0")int type,
                               @RequestParam (name = "baseId", defaultValue = "0")long baseId,
                               @RequestParam (name = "similarId", defaultValue = "0")long similarId) {
        return userCollectionService.saveCollection(userSession.getId(), type, baseId, similarId);

    }

//    @LogPrint
//    @PostMapping(value="test",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public Boolean test(         @RequestParam int uid,
//                                                      @RequestParam int type ,
//                                                      @RequestParam long baseId,
//                                                      @RequestParam long similarId) {
//        return userCollectionService.saveCollection(uid, type,baseId,similarId);
//
//    }


    /**
     * 取消收藏题目
     *
     * @return
     */
    @LogPrint
    @DeleteMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object delCollection(@Token UserSession userSession,
                              @RequestHeader int terminal,
                              @RequestHeader String cv,
                              @RequestParam (name = "type", defaultValue = "0")int type,
                              @RequestParam (name = "baseId", defaultValue = "0")long baseId,
                              @RequestParam (name = "similarId", defaultValue = "0")long similarId) {
        return userCollectionService.delCollection(userSession.getId(), baseId, type,similarId);
    }
//    @DeleteMapping(value="del")
//    public void delTest(Integer userId,Long collectId,Integer type){
//        userCollectionService.delCollection(userId,collectId,type);
//    }
//
//
//

    /**
     * 查询收藏题目
     * 单题0  套题1 议论文2
     * @return
     */
    @LogPrint
    @GetMapping(value = "list/{type}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object list(@Token UserSession userSession,
                   @RequestHeader int terminal,
                   @RequestHeader String cv,
                   @PathVariable int type,
                   @RequestParam (name = "page", defaultValue = "1")int page,
                   @RequestParam (name = "pageSize", defaultValue = "20") int pageSize) {

        if(cv.compareTo("7.0") >= 0){
            return  userCollectionService.listV2(userSession, type,page,pageSize, EssayAnswerCardEnum.ModeTypeEnum.NORMAL);
        }
       return  userCollectionService.listV1(userSession, type,page,pageSize,EssayAnswerCardEnum.ModeTypeEnum.NORMAL);

    }

//    @GetMapping(value="list")
//    public List listTest(Integer userId,Integer type) throws InvocationTargetException, IllegalAccessException {
//        return userCollectionService.list(userId,type);
//    }
//
//
//    /**
//     * 批量查询题目
//     * @return
//     */
//    @LogPrint
//    @GetMapping(value="batch",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public List<EssayUserCollectionVO> batch(@Token UserSession userSession,
//                                             @RequestHeader int terminal,
//                                             @RequestHeader String cv,
//                                             @RequestParam int type,
//                                             @RequestParam List<Long> list) {
//
//        List<EssayUserCollectionVO> voList = userCollectionService.batch(userSession.getId(), type,list);
//        return voList;
//    }

    /**
     * 校验试题or试卷是否收藏
     */
    @LogPrint
    @GetMapping(value = "check", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object check(@Token UserSession userSession,
                               @RequestHeader int terminal,
                               @RequestHeader String cv,
                               @RequestParam (name = "type", defaultValue = "0")int type,
                               @RequestParam (name = "baseId", defaultValue = "0")long baseId,
                               @RequestParam (name = "similarId", defaultValue = "0")long similarId) {

        return  userCollectionService.check(userSession, baseId, type,similarId);
    }



    /**
     * 校验题目是否上线
     */
    @LogPrint
    @GetMapping(value = "status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public EssayUpdateVO status(@Token UserSession userSession,
                               @RequestHeader int terminal,
                               @RequestHeader String cv,
                               @RequestParam (name = "type", defaultValue = "0")int type,
                               @RequestParam (name = "baseId", defaultValue = "0")long baseId,
                               @RequestParam (name = "similarId", defaultValue = "0")long similarId) {

        return  userCollectionService.status(userSession, baseId, type,similarId);

    }

}
