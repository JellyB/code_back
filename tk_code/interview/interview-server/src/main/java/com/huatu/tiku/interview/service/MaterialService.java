package com.huatu.tiku.interview.service;


import com.huatu.tiku.interview.entity.material.MaterialList;

import java.io.File;

/**
 * Created by Administrator on 2016/11/9.
 */
public interface MaterialService {
    public String uploadPermanentMedia2(String accessToken, String title, String introduction) ;
    public String getlist(MaterialList materialList, String accessToken);
    public String uploadPermanentMedia1(String accessToken, String title, String introduction);
}
