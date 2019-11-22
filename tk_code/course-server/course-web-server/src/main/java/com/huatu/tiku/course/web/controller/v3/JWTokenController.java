package com.huatu.tiku.course.web.controller.v3;

import com.huatu.common.jwt.util.JWTUtil;
import com.huatu.common.utils.code.UUIDTools;
import com.huatu.common.utils.date.DateUtil;
import com.huatu.springboot.web.version.mapping.annotation.VersionMapping;
import com.huatu.tiku.common.bean.user.UserSession;
import com.huatu.tiku.springboot.users.support.Token;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author hanchao
 * @date 2018/3/23 18:21
 */
@RestController
@VersionMapping(value = "/jwt", apiVersion = "v3")
public class JWTokenController {
    public static final String ISSUER = "ht-course-server";
    public static final long DATE_BETWEEN = 1000 * 60 * 60 * 24;//1d


    /**
     * 拿着旧的jwtoken来换取新的
     *
     * @param oldToken
     * @param userSession
     * @return
     */
    @PostMapping
    public Object getToken(@RequestParam(value = "token", required = false) String oldToken,
                           @Token UserSession userSession) {
        //只要通过token验证，可以直接颁发jwt token
        String result = null;
        do {
            if (StringUtils.isBlank(oldToken)) {
                break;
            }
            try {
                Jwt parse = Jwts.parser().setSigningKey(JWTUtil.JWT_KEY).parse(oldToken);
                if (!(parse.getBody() instanceof DefaultClaims)) {
                    break;
                }
                Date expiration = ((DefaultClaims) parse.getBody()).getExpiration();
                if ((expiration.getTime() - System.currentTimeMillis()) > DATE_BETWEEN){//日期合适，直接返回旧数据即可
                    result = oldToken;
                }
                parse = null;
            } catch (Exception e) {
                //parse error,do nothing
            }
        } while (false);
        if(result == null){
            JwtBuilder jwtBuilder = Jwts.builder()
                    .addClaims(JWTUtil.createPayload(userSession))
                    .setExpiration(DateUtil.addDay(7))
                    .setIssuedAt(new Date())
                    .setIssuer(ISSUER)
                    .setId(UUIDTools.create())
                    .signWith(SignatureAlgorithm.HS256,JWTUtil.JWT_KEY);
            result = jwtBuilder.compact();
            jwtBuilder = null;
        }
        return result;
    }


}
