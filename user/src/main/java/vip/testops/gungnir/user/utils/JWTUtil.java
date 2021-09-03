package vip.testops.gungnir.user.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.security.Key;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtil {
    public static String getSecret(String key){
        String secret = null;
        try {
            Key secretKey = AESUtil.convertToKey(AESUtil.generateKey(key));

            secret = AESUtil.encrypt("cherry-x", secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return secret;
    }

    public static String createToken(Map<String, Object> claim, String secret, int expire) {
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.SECOND, expire);
        Date expireDate = nowTime.getTime();
        Map<String, Object> map = new HashMap<>();
        map.put("alg", "HS256");
        map.put("typ", "JWT");
        return JWT.create().withHeader(map)
                .withClaim("userInfo", claim)
                .withIssuedAt(new Date())
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public static Claim verifyToken(String token, String secret) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        DecodedJWT jwt;
        try {
            jwt = verifier.verify(token);
        } catch (Exception e){
            throw new RuntimeException("token is expired");
        }
        return jwt.getClaim("userInfo");
    }

    public static Map<String, Claim> parseToken(String token){
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getClaims();
    }

}
