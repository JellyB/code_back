package com.huatu.keycloak.util;

import com.google.common.collect.Maps;
import com.huatu.common.utils.encrypt.AESCoder;
import com.huatu.keycloak.consts.KeyCloakAdminConsts;
import com.huatu.keycloak.consts.KeyCloakConsts;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;

import java.util.Map;

import static com.huatu.keycloak.consts.KeyCloakAdminConsts.CLIENT_ID;
import static com.huatu.keycloak.consts.KeyCloakConsts.REALM_MASTER;

/**
 * @author hanchao
 * @date 2017/10/18 17:37
 */
public class KeyCloakUtil {
    private static Map<String,Keycloak> keycloaks = Maps.newHashMap();
    static {
        keycloaks.put(REALM_MASTER,Keycloak.getInstance(KeyCloakConsts.AUTH_URL,
                REALM_MASTER,
                KeyCloakAdminConsts.USERNAME,
                AESCoder.decryptWithHex(KeyCloakAdminConsts.PASSWORD,KeyCloakAdminConsts.ENC_KEY),
                CLIENT_ID));
    }

    public static Keycloak getKeycloak(String realm){
        return keycloaks.get(realm);
    }

    public static Keycloak getKeycloak(){
        return keycloaks.get(REALM_MASTER);
    }

    public static RealmResource getRealm(){
        return getKeycloak().realm(REALM_MASTER);
    }

    public static RealmResource getRealm(String realm){
        return getKeycloak(realm).realm(realm);
    }
}
