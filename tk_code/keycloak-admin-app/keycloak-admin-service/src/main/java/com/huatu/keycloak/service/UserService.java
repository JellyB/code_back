package com.huatu.keycloak.service;

import com.huatu.common.bean.page.Pager;
import com.huatu.keycloak.bean.UserInfo;
import com.huatu.keycloak.util.KeyCloakUtil;
import com.huatu.keycloak.util.UserInfoTrasformer;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hanchao
 * @date 2017/10/18 17:38
 */
@Service
public class UserService {
    public UserInfo get(String id){
        UserRepresentation userRepresentation = KeyCloakUtil.getRealm().users().get(id).toRepresentation();
        return UserInfoTrasformer.tranform(userRepresentation);
    }

    public List<UserInfo> search(String search, Pager pager){
        List<UserRepresentation> results = KeyCloakUtil.getRealm().users().search(search, pager.getFirstResult(), pager.getOnePageSize());
        return results.stream().map(userRepresentation -> UserInfoTrasformer.tranform(userRepresentation)).collect(Collectors.toList());
    }
}
