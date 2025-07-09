package cn.xryder.base.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT服务类 - 负责JWT令牌的生成、解析和验证
 * 
 * @Author: joetao
 * @Date: 2024/7/31 13:47
 * @Updated: 2025/7/9 优化代码结构和安全性
 */
@Slf4j
@Component
public class JwtService {

    // Token类型常量
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String AI_ACCESS_TOKEN = "aiAccessToken";

    // Token有效期常量（分钟）
    private static final long ACCESS_TOKEN_VALIDITY = 1L;
    private static final long REFRESH_TOKEN_VALIDITY = 60L * 24 * 15; // 15天
    private static final long AI_TOKEN_VALIDITY = 60L * 24 * 15; // 15天

    // JWT密钥 - 建议从配置文件读取
    @Value("${jwt.secret:357638792F423F4428472B4B62L0E5S368566D597133743677397A2443264629}")
    private String jwtSecret;

    // RefreshToken专用密钥 - 增强安全性
    @Value("${jwt.refresh-secret:457638792F423F4428472B4B62L0E5S368566D597133743677397A2443264630}")
    private String refreshTokenSecret;

    // 存储RefreshToken映射：username -> Set<RefreshTokenInfo> (支持多设备登录)
    private final Map<String, Set<RefreshTokenInfo>> userRefreshTokenMap = new ConcurrentHashMap<>();

    // RefreshToken黑名单 (存储被撤销的token JTI)
    private final Set<String> revokedRefreshTokens = ConcurrentHashMap.newKeySet();

    /**
     * RefreshToken信息内部类 - 增强版本
     */
    private static class RefreshTokenInfo {
        private final String jti; // JWT ID - 唯一标识
        private final Date expireTime; // 过期时间
        private final String deviceInfo; // 设备信息
        private final String clientIp; // 客户端IP
        private final Date createdTime; // 创建时间

        public RefreshTokenInfo(String jti, String token, Date expireTime,
                String deviceInfo, String clientIp) {
            this.jti = jti;
            this.expireTime = expireTime;
            this.deviceInfo = deviceInfo;
            this.clientIp = clientIp;
            this.createdTime = new Date();
        }

        public boolean isExpired() {
            return new Date().after(expireTime);
        }

        public String getJti() {
            return jti;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }

        public String getClientIp() {
            return clientIp;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            RefreshTokenInfo that = (RefreshTokenInfo) obj;
            return Objects.equals(jti, that.jti);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jti);
        }
    }

    /**
     * 从Token中提取用户名
     */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException e) {
            log.warn("提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中提取过期时间（安全方法，即使Token过期也能提取）
     */
    public Date extractExpiration(String token) {
        try {
            Claims claims = extractClaimsIntelligently(token);
            return claims != null ? claims.getExpiration() : null;
        } catch (Exception e) {
            log.warn("提取过期时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.warn("检查Token过期状态失败: {}", e.getMessage());
            return true; // 异常情况视为过期
        }
    }

    /**
     * 验证Token是否有效
     */
    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            return username.equals(tokenUsername) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 通用的Claim提取方法
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取Token中的权限信息
     */
    public Set<GrantedAuthority> getAuthoritiesFromToken(String token) {
        try {
            String scope = extractClaim(token, claims -> claims.get("scope", String.class));
            if (StringUtils.isBlank(scope)) {
                return Collections.emptySet();
            }

            return Arrays.stream(scope.split(","))
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("提取权限信息失败: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * 获取Token类型
     */
    public String getTokenType(String token) {
        try {
            return extractClaim(token, claims -> claims.get("grantType", String.class));
        } catch (Exception e) {
            log.warn("获取Token类型失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 提取Token的所有Claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证RefreshToken是否有效 - 增强版本
     */
    public boolean isValidRefreshToken(String token) {
        try {
            // 使用智能Claims提取方法，能够正确处理RefreshToken的专用密钥
            Claims claims = extractClaimsIntelligently(token);
            if (claims == null) {
                return false;
            }

            String grantType = claims.get("grantType", String.class);
            String username = claims.getSubject();
            String jti = claims.get("jti", String.class);

            if (!REFRESH_TOKEN.equals(grantType) || username == null || jti == null) {
                return false;
            }

            // 检查是否在黑名单中
            if (revokedRefreshTokens.contains(jti)) {
                log.warn("RefreshToken {} 已被撤销", jti);
                return false;
            }

            Set<RefreshTokenInfo> tokenInfos = userRefreshTokenMap.get(username);
            if (tokenInfos == null || tokenInfos.isEmpty()) {
                return false;
            }

            // 查找匹配的RefreshToken
            return tokenInfos.stream()
                    .anyMatch(tokenInfo -> !tokenInfo.isExpired() &&
                            tokenInfo.getJti().equals(jti));
        } catch (Exception e) {
            log.warn("RefreshToken验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 提取JWT ID
     */
    public String extractJti(String token) {
        try {
            return extractClaim(token, claims -> claims.get("jti", String.class));
        } catch (Exception e) {
            log.warn("提取JTI失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查是否为AccessToken
     */
    public boolean isAccessToken(String token) {
        try {
            String grantType = getTokenType(token);
            return ACCESS_TOKEN.equals(grantType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("AccessToken检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为AI访问Token
     */
    public boolean isAiAccessToken(String token) {
        try {
            String grantType = getTokenType(token);
            return AI_ACCESS_TOKEN.equals(grantType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("AI AccessToken检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 生成访问Token
     */
    public String generateToken(String username, String authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", ACCESS_TOKEN);
        claims.put("scope", authorities);

        return createToken(claims, username, ACCESS_TOKEN_VALIDITY);
    }

    /**
     * 生成AI聊天Token
     */
    public String generateAiChatToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", AI_ACCESS_TOKEN);

        return createToken(claims, username, AI_TOKEN_VALIDITY);
    }

    /**
     * 生成刷新Token - 增强安全版本
     */
    public String generateRefreshToken(String username, String deviceInfo, String clientIp) {
        // 清理过期的RefreshToken
        cleanExpiredRefreshTokens();

        String jti = UUID.randomUUID().toString(); // 生成唯一ID
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", REFRESH_TOKEN);
        claims.put("jti", jti);
        claims.put("device", deviceInfo);
        claims.put("ip", clientIp);

        String refreshToken = createRefreshToken(claims, username, REFRESH_TOKEN_VALIDITY);
        Date expireTime = new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY * 60 * 1000);

        // 存储RefreshToken信息
        RefreshTokenInfo tokenInfo = new RefreshTokenInfo(jti, refreshToken, expireTime, deviceInfo, clientIp);
        userRefreshTokenMap.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(tokenInfo);

        log.info("为用户 {} 生成RefreshToken，设备: {}, IP: {}", username, deviceInfo, clientIp);
        return refreshToken;
    }

    /**
     * 兼容性方法 - 无设备信息的RefreshToken生成
     */
    public String generateRefreshToken(String username) {
        return generateRefreshToken(username, "unknown", "unknown");
    }

    /**
     * 清理过期的RefreshToken
     */
    public void cleanExpiredRefreshTokens() {
        userRefreshTokenMap.entrySet().forEach(entry -> {
            Set<RefreshTokenInfo> tokens = entry.getValue();
            tokens.removeIf(RefreshTokenInfo::isExpired);
            if (tokens.isEmpty()) {
                userRefreshTokenMap.remove(entry.getKey());
            }
        });
    }

    /**
     * 撤销用户的所有RefreshToken
     */
    public void revokeAllRefreshTokens(String username) {
        Set<RefreshTokenInfo> tokens = userRefreshTokenMap.remove(username);
        if (tokens != null) {
            // 将所有JTI加入黑名单
            tokens.forEach(token -> revokedRefreshTokens.add(token.getJti()));
            log.info("撤销用户 {} 的所有RefreshToken", username);
        }
    }

    /**
     * 撤销特定的RefreshToken
     */
    public void revokeRefreshToken(String username, String jti) {
        Set<RefreshTokenInfo> tokens = userRefreshTokenMap.get(username);
        if (tokens != null) {
            tokens.removeIf(token -> token.getJti().equals(jti));
            revokedRefreshTokens.add(jti);
            log.info("撤销用户 {} 的RefreshToken: {}", username, jti);
        }
    }

    /**
     * 创建Token的核心方法 (AccessToken和AI Token)
     */
    private String createToken(Map<String, Object> claims, String username, Long minutes) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + minutes * 60 * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSignKey())
                .compact();
    }

    /**
     * 创建RefreshToken的方法 (使用专用密钥)
     */
    private String createRefreshToken(Map<String, Object> claims, String username, Long minutes) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + minutes * 60 * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getRefreshTokenSignKey())
                .compact();
    }

    /**
     * 获取AccessToken签名密钥
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取RefreshToken专用签名密钥
     */
    private Key getRefreshTokenSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshTokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 获取用户的所有RefreshToken信息 (用于管理界面)
     */
    public List<Map<String, Object>> getUserRefreshTokens(String username) {
        Set<RefreshTokenInfo> tokens = userRefreshTokenMap.get(username);
        if (tokens == null || tokens.isEmpty()) {
            return Collections.emptyList();
        }

        return tokens.stream()
                .filter(token -> !token.isExpired())
                .map(token -> {
                    Map<String, Object> tokenMap = new HashMap<>();
                    tokenMap.put("jti", token.getJti());
                    tokenMap.put("deviceInfo", token.getDeviceInfo());
                    tokenMap.put("clientIp", token.getClientIp());
                    tokenMap.put("createdTime", token.getCreatedTime());
                    return tokenMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查RefreshToken是否被撤销
     */
    public boolean isRefreshTokenRevoked(String jti) {
        return revokedRefreshTokens.contains(jti);
    }

    /**
     * 获取活跃的RefreshToken数量
     */
    public int getActiveRefreshTokenCount(String username) {
        Set<RefreshTokenInfo> tokens = userRefreshTokenMap.get(username);
        if (tokens == null) {
            return 0;
        }
        return (int) tokens.stream().filter(token -> !token.isExpired()).count();
    }

    /**
     * 清理撤销令牌黑名单中的过期项 (定期调用)
     */
    public void cleanRevokedTokens() {
        // 这里可以根据实际需求实现清理逻辑
        // 例如：只保留最近一段时间内被撤销的token JTI
        log.debug("清理撤销令牌黑名单，当前大小: {}", revokedRefreshTokens.size());
    }

    /**
     * 安全地从Token中提取用户名，即使Token已过期也能提取
     */
    public String extractUsernameFromExpiredToken(String token) {
        try {
            Claims claims = extractClaimsIntelligently(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.warn("提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 安全地检查Token是否过期，不会因为过期而抛出异常
     */
    public boolean isTokenExpiredSafely(String token) {
        try {
            Claims claims = extractClaimsIntelligently(token);
            if (claims == null) {
                return true; // 无法解析Claims，视为过期
            }
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.warn("检查Token过期状态失败: {}", e.getMessage());
            return true; // 异常情况视为过期
        }
    }

    /**
     * 安全地获取Token类型，即使Token过期也能获取
     */
    public String getTokenTypeSafely(String token) {
        try {
            Claims claims = extractClaimsIntelligently(token);
            if (claims == null) {
                return null; // 无法解析Claims
            }
            return claims.get("grantType", String.class);
        } catch (Exception e) {
            log.warn("获取Token类型失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 智能地提取Token的所有Claims，自动选择正确的密钥
     */
    private Claims extractClaimsIntelligently(String token) {
        // 先尝试用AccessToken密钥解析
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            // Token过期但签名正确，返回Claims
            return e.getClaims();
        } catch (Exception e) {
            // 可能是RefreshToken，尝试用RefreshToken密钥解析
            try {
                return Jwts.parser()
                        .verifyWith((SecretKey) getRefreshTokenSignKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (ExpiredJwtException refreshExpired) {
                // RefreshToken过期但签名正确，返回Claims
                return refreshExpired.getClaims();
            } catch (Exception refreshException) {
                // 都不行，抛出原始异常
                throw new JwtException("无法解析Token: " + e.getMessage());
            }
        }
    }

    /**
     * 从RefreshToken中安全提取用户名
     */
    public String extractUsernameFromRefreshToken(String refreshToken) {
        try {
            Claims claims = extractClaimsIntelligently(refreshToken);
            return claims != null ? claims.getSubject() : null;
        } catch (Exception e) {
            log.warn("从RefreshToken提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从RefreshToken中安全提取JTI
     */
    public String extractJtiFromRefreshToken(String refreshToken) {
        try {
            Claims claims = extractClaimsIntelligently(refreshToken);
            return claims != null ? claims.get("jti", String.class) : null;
        } catch (Exception e) {
            log.warn("从RefreshToken提取JTI失败: {}", e.getMessage());
            return null;
        }
    }
}
