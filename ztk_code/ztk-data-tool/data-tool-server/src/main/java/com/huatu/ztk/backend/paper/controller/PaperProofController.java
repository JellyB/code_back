package com.huatu.ztk.backend.paper.controller;

import com.huatu.ztk.backend.paper.service.PaperProofService;
import com.huatu.ztk.backend.user.bean.User;
import com.huatu.ztk.commons.exception.BizException;
import com.huatu.ztk.commons.exception.SuccessMessage;
import com.itextpdf.text.BadElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 试卷校对
 * Created by linkang on 3/9/17.
 */

@RestController
@RequestMapping("/paper")
public class PaperProofController {

    @Autowired
    private PaperProofService paperProofService;


    /**
     * 获取试卷校对数据
     *
     * @return
     */
    @RequestMapping(value = "proof", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object findPaperProof(@RequestParam int id,
                                       @RequestParam int page,
                                       @RequestParam int size,
                                       @RequestParam int moduleId) throws BizException {

        return paperProofService.findPaperProof(id, page, size, moduleId);
    }

    /**
     * 试卷校对页面，删除试题
     *
     * @return
     */
    @RequestMapping(value = "proof", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Object deleteProofQuestion(@RequestParam int paperId,
                                      @RequestParam int questionId, HttpServletRequest request) throws BizException, IOException, BadElementException {
        final HttpSession session = request.getSession(true);
        User user = (User) session.getAttribute("user");
        String account = "";
        int id = -1;
        if(user!=null){
            account = user.getAccount();
            id = (int) user.getId();
        }
        paperProofService.deleteProofQuestion(paperId, questionId,account,id);
        return SuccessMessage.create("删除成功");
    }
}
