package com.huatu.one.biz.service;

import com.huatu.one.biz.mapper.VersionMapper;
import com.huatu.one.biz.model.Version;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VersionService {
    @Autowired
    private VersionMapper versionMapper;

    public String  getVersion(){
        Version version=versionMapper.selectByPrimaryKey(1L);
        if (version!=null){
            return version.getVersion();
        }
        return null;
    }
}
