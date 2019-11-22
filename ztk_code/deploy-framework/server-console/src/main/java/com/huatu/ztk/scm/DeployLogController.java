package com.huatu.ztk.scm;

import com.huatu.ztk.scm.base.BaseController;
import com.huatu.ztk.scm.common.OperationResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
@RequestMapping("/log")
public class DeployLogController extends BaseController {

    /**
     * 回显
     * @return
     * @throws IOException
     * @throws GitAPIException
     */
    @RequestMapping("echo.do")
    @ResponseBody
    public String echoLog(@RequestParam("loggerName")String loggerName
    ) throws IOException, GitAPIException{
        return OperationResult.get(loggerName);
    }
}
