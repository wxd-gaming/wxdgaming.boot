package wxdgaming.boot.core.str;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JwtUtils {

    static SecretKey secretKey = Keys.hmacShaKeyFor("6b1a678d78e302FD9D264256f17fbec8t".getBytes(StandardCharsets.UTF_8));

    public static void build(String keyString) {
        secretKey = Keys.hmacShaKeyFor(keyString.getBytes(StandardCharsets.UTF_8));
        createJwtBuilder();
        createJwtParser();
        /*调用一次实例化*/
        String jwt = JwtUtils.createJwtBuilder()
                .expiration(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(6)))
                .header().add("token", "3")
                .and()
                .claim("gameId", "1234")
                .claim("account", "admin")
                .compact();
        parseJWT(jwt);
    }

    /** 创建默认过期时间是6天的 */
    public static JwtBuilder createJwtBuilder() {
        return createJwtBuilder(TimeUnit.DAYS.toMillis(6));
    }

    /** 过期时间是毫秒 */
    public static JwtBuilder createJwtBuilder(long duration) {
        return createJwtBuilder(secretKey, duration);
    }

    /** 创建默认过期时间是6天的 */
    public static JwtBuilder createJwtBuilder(String private_key) {
        return createJwtBuilder(private_key, TimeUnit.DAYS.toMillis(6));
    }

    /** 过期时间是毫秒 */
    public static JwtBuilder createJwtBuilder(String private_key, long duration) {
        return createJwtBuilder(Keys.hmacShaKeyFor(private_key.getBytes(StandardCharsets.UTF_8)), duration);
    }

    public static JwtBuilder createJwtBuilder(SecretKey key, long duration) {
        // 生成 HMAC 密钥，根据提供的字节数组长度选择适当的 HMAC 算法，并返回相应的 SecretKey 对象。
        // 设置jwt的body
        return Jwts.builder()
                // 设置签名使用的签名算法和签名使用的秘钥
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .signWith(key)
                .expiration(new Date(System.currentTimeMillis() + duration));
    }

    public static JwtParser createJwtParser() {
        return createJwtParser(secretKey);
    }

    public static JwtParser createJwtParser(String private_key) {
        return createJwtParser(Keys.hmacShaKeyFor(private_key.getBytes(StandardCharsets.UTF_8)));
    }

    public static JwtParser createJwtParser(SecretKey key) {
        // 生成 HMAC 密钥，根据提供的字节数组长度选择适当的 HMAC 算法，并返回相应的 SecretKey 对象。
        // 设置jwt的body
        return Jwts.parser()
                // 设置签名使用的签名算法和签名使用的秘钥
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .verifyWith(key)
                .build();
    }

    public static Jws<Claims> parseJWT(String token) {
        return parseJWT(createJwtParser(secretKey), token);
    }

    public static Jws<Claims> parseJWT(String private_key, String token) {
        return parseJWT(createJwtParser(private_key), token);
    }

    /**
     * Token解密
     *
     * @param jwtParser
     * @param token
     * @return
     * @author: wxd-gaming(無心道, 15388152619)
     * @version: 2024-10-17 19:43
     */
    public static Jws<Claims> parseJWT(JwtParser jwtParser, String token) {
        // 生成 HMAC 密钥，根据提供的字节数组长度选择适当的 HMAC 算法，并返回相应的 SecretKey 对象。
        // 得到DefaultJwtParser
        return jwtParser.parseSignedClaims(token);
    }
}
