package com.mxio.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author mxio
 */

@Component
@Slf4j
public class JwtUtil {

    @Value("${emos.jwt.secret}")
    private String secret;

    @Value("${emos.jwt.expire}")
    private int expire;

    // 生成令牌方法
    public String createToken(int userId) {
        //1. 计算过期日期是哪一天
        Date date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, 5);
        //2. 将密钥封装成加密算法对象
        Algorithm algorithm = Algorithm.HMAC256(secret);
        //3. 创建内部类对象
        JWTCreator.Builder builder = JWT.create();
        //4. 生成token
        String token = builder.withClaim("userId", userId).withExpiresAt(date).sign(algorithm);
        return token;
    }

    // 从令牌中反向获得用户id的方法
    public int getUserId(String token) {
        // 对令牌字符串进行解码
        DecodedJWT jwt = JWT.decode(token);
        int userId = jwt.getClaim("userId").asInt();
        return userId;
    }

    // 验证令牌字符串有效性的方法
    public void verifierToken(String token) {
        //1. 创建算法对象
        Algorithm algorithm = Algorithm.HMAC256(secret);
        //2. 调用算法对象进行解密
        JWTVerifier verifier = JWT.require(algorithm).build();
        //3. 调用验证方法
        verifier.verify(token);
    }

}
