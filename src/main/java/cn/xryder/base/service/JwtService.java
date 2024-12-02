package cn.xryder.base.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: joetao
 * @Date: 2024/7/31 13:47
 */
@Component
public class JwtService {
    public static final String SECRET = "357638792F423F4428472B4B62L0E5S368566D597133743677397A2443264629";
    //用于存储用户的刷新token，当用户重复登录时只返回同一个未过期的refreshToken
    private final Map<String, String> userRefreshTokenMap = new ConcurrentHashMap<>();

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 提取权限信息
    public Set<GrantedAuthority> getAuthoritiesFromToken(String token) {
        // 从 token 中提取 authorities
        String scope = extractClaim(token, claims -> claims.get("scope", String.class));
        if (StringUtils.isEmpty(scope)) {
            return Collections.emptySet();
        }
        return Arrays.stream(scope.split(",")).toList().stream()
                .map(SimpleGrantedAuthority::new)  // 将每个 permission 转换为 SimpleGrantedAuthority
                .collect(Collectors.toSet());      // 将结果收集为 Set
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isValidRefreshToken(String token) {
        String grantType = extractAllClaims(token).get("grantType", String.class);
        String username = extractUsername(token);
        return grantType.equals("refreshToken") && userRefreshTokenMap.containsKey(username);
    }

    public Boolean isAccessToken(String token) {
        String grantType = extractAllClaims(token).get("grantType", String.class);
        return grantType.equals("accessToken");
    }

    public String GenerateToken(String username, String authorities){
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", "accessToken");
        // scope存放的是权限信息
        claims.put("scope", authorities);
        //有效期3分钟
        return createToken(claims, username, 3L);
    }

    public String GenerateAiChatToken(String username){
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", "aiAccessToken");
        //有效期3分钟
        return createToken(claims, username, 60 * 24 * 15L);
    }

    public String GenerateRefreshToken(String username){
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", "refreshToken");
        // 有效期15天
        String refreshToken = createToken(claims, username, 60 * 24 * 15L);
        userRefreshTokenMap.put(username, refreshToken);
        return refreshToken;
    }


    private String createToken(Map<String, Object> claims, String username, Long minutes) {

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ 1000 * 60 * minutes))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
