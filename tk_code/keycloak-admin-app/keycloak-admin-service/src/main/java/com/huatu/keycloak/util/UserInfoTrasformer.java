package com.huatu.keycloak.util;

import com.huatu.common.utils.lang.StrUtil;
import com.huatu.keycloak.bean.UserInfo;
import org.keycloak.representations.idm.UserRepresentation;

/**
 * @author hanchao
 * @date 2017/10/18 17:56
 */
public class UserInfoTrasformer {
    /**
     * 从keycloak域转换用户
     * @param userRepresentation
     * @return
     */
    public static UserInfo tranform(UserRepresentation userRepresentation){
        return UserInfo.builder()
                .id(userRepresentation.getId())
                .username(userRepresentation.getUsername())
                .name(StrUtil.concat(userRepresentation.getFirstName(),userRepresentation.getLastName()))
                .email(userRepresentation.getEmail())
                .enabled(userRepresentation.isEnabled())
                .clientRoles(userRepresentation.getClientRoles())
                .build();
    }
}
