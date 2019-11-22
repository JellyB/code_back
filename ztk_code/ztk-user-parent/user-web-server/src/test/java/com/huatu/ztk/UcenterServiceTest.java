package com.huatu.ztk;

import com.huatu.ztk.user.common.UcenterConfig;
import com.huatu.ztk.user.service.UcenterService;
import org.junit.Test;


/**
 * 描述：
 *
 * @author biguodong
 * Create time 2019-03-04 2:13 PM
 **/
public class UcenterServiceTest extends BaseTest{

    private UcenterService ucenterService;



    @Test
    public void sync(){
        String regip = "117.136.97.106";
        String passWord = "123456";
        String[] userName = new String[]{"htwxsongxy", "htwxwangxufei", "htwxdongx", "htwxwangez", "htwxhanl", "htwxzhengpf", "htwxshy", "htwxliuq", "htwxyanglu", "htwxliya"};
        for (String s : userName) {
            ucenterService.saveMember(s, passWord,regip, UcenterConfig.UCENTER_MEMBERS_APPID,false);
        }

    }
}
