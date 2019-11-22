package com.huatu.common.jwt.util;

import com.alibaba.fastjson.JSON;
import com.huatu.common.jwt.bean.JWTUser;
import com.huatu.common.utils.reflect.BeanUtil;
import com.huatu.tiku.common.bean.user.UserSession;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;

import java.util.Map;

/**
 * @author hanchao
 * @date 2018/3/26 15:45
 */
public class JWTUtil {
    public static final byte[] JWT_KEY = new byte[]{2, 0, 1, 8, 0, 3, 2, 6};//base64编码后是AgABCAADAgY=
    public static JWTUser createJWTUser(UserSession userSession) {
        return new JWTUser()
                .setSsoId(userSession.getSsoId())
                .setId(userSession.getId())
                .setNick(userSession.getNick())
                .setUname(userSession.getUname());
    }
    public static Map<String, Object> createPayload(UserSession userSession){
        JWTUser jwtUser = createJWTUser(userSession);
        return BeanUtil.toMap(jwtUser);
    }

    public static JWTUser parse(String token){
        return parse(token,true);
    }

    public static JWTUser parse(String token,boolean verify){
        String body = null;
        if(verify){
            Jwt parse = Jwts.parser().setSigningKey(JWTUtil.JWT_KEY).parse(token);
            if ((parse.getBody() instanceof DefaultClaims)) {
                body = JSON.toJSONString(parse.getBody());
            }else if(parse.getBody() instanceof String){
                body = (String) parse.getBody();
            }
        }else{
            String[] parts = splitToken(token);
            body = parts[1];
        }
        if(body != null){
            return JSON.parseObject(body,JWTUser.class);
        }
        return null;
    }

    static String[] splitToken(String token) throws IllegalArgumentException {
        String[] parts = token.split("\\.");
        if (parts.length == 2 && token.endsWith(".")) {
            parts = new String[]{parts[0], parts[1], ""};
        }
        if (parts.length != 3) {
            throw new IllegalArgumentException(String.format("The token was expected to have 3 parts, but got %s.", parts.length));
        }
        return parts;
    }

}
