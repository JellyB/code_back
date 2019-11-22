package com.huatu.tiku.course.service.v1;

import com.huatu.tiku.common.AppVersionEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-26 4:46 PM
 **/

@Service
@Slf4j
public class VersionControlService {
    private static int IosVersion = 7110;
    private static int AndroidVersion = 7111;


    /**
     * 版本检测是否展示学习报告
     * @param terminal
     * @param cv
     * @return
     */
    public boolean checkLearnReportShow(int terminal, String cv){
        int versionCount = Integer.valueOf(StringUtils.rightPad(cv.replaceAll("\\.", ""), 4, '0'));
        if(terminal == AppVersionEnum.TerminalTypeEnum.IOS.getValue() && versionCount >= IosVersion){
            return true;
        }else if((terminal == 4 || terminal == AppVersionEnum.TerminalTypeEnum.ANDROID.getValue()) && versionCount >= AndroidVersion){
            return true;
        }else {
            return false;
        }
    }
}
