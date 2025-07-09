package cn.xryder.base.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
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
 * @author wrm
 */
@Slf4j
@Component
public class JwtService {

    // Token类型常量
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String AI_ACCESS_TOKEN = "aiAccessToken";

    // Token有效期常量（分钟）
    @Value("${jwt.access-token-validity:30}")
    private long ACCESS_TOKEN_VALIDITY;
    @Value("${jwt.refresh-token-validity:21600}")
    private long REFRESH_TOKEN_VALIDITY;
    @Value("${jwt.ai-token-validity:21600}")
    private long AI_TOKEN_VALIDITY;

    // 存储RefreshToken映射：username -> Set<RefreshTokenInfo> (支持多设备登录)
    private final Map<String, Set<RefreshTokenInfo>> userRefreshTokenMap = new ConcurrentHashMap<>();
    // RefreshToken黑名单 (存储被撤销的token JTI)
    private final Set<String> revokedRefreshTokens = ConcurrentHashMap.newKeySet();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.refresh-secret}")
    private String refreshTokenSecret;

    public String extractUsername(String token) {
        try {
            Claims claims = extractClaimsIntelligently(token);
            return claims != null ? claims.getSubject() : null;
        } catch (Exception e) {
            log.warn("提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            Claims claims = extractClaimsIntelligently(token);
            return claims != null ? claims.getExpiration() : null;
        } catch (Exception e) {
            log.warn("提取过期时间失败: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaimsIntelligently(token);
            if (claims == null)
                return true;
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.before(new Date());
        } catch (Exception e) {
            log.warn("检查Token过期状态失败: {}", e.getMessage());
            return true;
        }
    }

    public boolean isTokenValid(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            return username.equals(tokenUsername) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractClaimsIntelligently(token);
        return claims != null ? claimsResolver.apply(claims) : null;
    }

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

    public String getTokenType(String token) {
        try {
            return extractClaim(token, claims -> claims.get("grantType", String.class));
        } catch (Exception e) {
            log.warn("获取Token类型失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证RefreshToken是否有效
     */
    public boolean isValidRefreshToken(String token) {
        try {
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

            if (revokedRefreshTokens.contains(jti)) {
                log.warn("RefreshToken {} 已被撤销", jti);
                return false;
            }

            Set<RefreshTokenInfo> tokenInfos = userRefreshTokenMap.get(username);
            if (tokenInfos == null || tokenInfos.isEmpty()) {
                return false;
            }

            return tokenInfos.stream()
                    .anyMatch(tokenInfo -> !tokenInfo.isExpired() &&
                            tokenInfo.getJti().equals(jti));
        } catch (Exception e) {
            log.warn("RefreshToken验证失败: {}", e.getMessage());
            return false;
        }
    }

    public String extractJti(String token) {
        try {
            return extractClaim(token, claims -> claims.get("jti", String.class));
        } catch (Exception e) {
            log.warn("提取JTI失败: {}", e.getMessage());
            return null;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            String grantType = getTokenType(token);
            return ACCESS_TOKEN.equals(grantType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("AccessToken检查失败: {}", e.getMessage());
            return false;
        }
    }

    public boolean isAiAccessToken(String token) {
        try {
            String grantType = getTokenType(token);
            return AI_ACCESS_TOKEN.equals(grantType) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("AI AccessToken检查失败: {}", e.getMessage());
            return false;
        }
    }

    public String generateToken(String username, String authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", ACCESS_TOKEN);
        claims.put("scope", authorities);
        return createToken(claims, username, ACCESS_TOKEN_VALIDITY);
    }

    public String generateAiChatToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", AI_ACCESS_TOKEN);
        return createToken(claims, username, AI_TOKEN_VALIDITY);
    }

    /**
     * 生成刷新Token - 支持多设备登录
     */
    public String generateRefreshToken(String username, String deviceInfo, String clientIp) {
        cleanExpiredRefreshTokens();
        String jti = UUID.randomUUID().toString();
        Map<String, Object> claims = new HashMap<>();
        claims.put("grantType", REFRESH_TOKEN);
        claims.put("jti", jti);
        claims.put("device", deviceInfo);
        claims.put("ip", clientIp);

        String refreshToken = createRefreshToken(claims, username, REFRESH_TOKEN_VALIDITY);
        Date expireTime = new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY * 60 * 1000);

        RefreshTokenInfo tokenInfo = new RefreshTokenInfo(jti, refreshToken, expireTime, deviceInfo, clientIp);
        userRefreshTokenMap.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(tokenInfo);

        log.info("为用户 {} 生成RefreshToken，设备: {}, IP: {}", username, deviceInfo, clientIp);
        return refreshToken;
    }

    public String generateRefreshToken(String username) {
        return generateRefreshToken(username, "unknown", "unknown");
    }

    public void cleanExpiredRefreshTokens() {
        userRefreshTokenMap.forEach((key, tokens) -> {
            tokens.removeIf(RefreshTokenInfo::isExpired);
            if (tokens.isEmpty()) {
                userRefreshTokenMap.remove(key);
            }
        });
    }

    public void revokeAllRefreshTokens(String username) {
        Set<RefreshTokenInfo> tokens = userRefreshTokenMap.remove(username);
        if (tokens != null) {
            tokens.forEach(token -> revokedRefreshTokens.add(token.getJti()));
            log.info("撤销用户 {} 的所有RefreshToken", username);
        }
    }

    public void revokeRefreshToken(String username, String jti) {
        Set<RefreshTokenInfo> tokens = userRefreshTokenMap.get(username);
        if (tokens != null) {
            tokens.removeIf(token -> token.getJti().equals(jti));
            revokedRefreshTokens.add(jti);
            log.info("撤销用户 {} 的RefreshToken: {}", username, jti);
        }
    }

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

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

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
                .toList();
    }

    public boolean isRefreshTokenRevoked(String jti) {
        return revokedRefreshTokens.contains(jti);
    }

    public int getActiveRefreshTokenCount(String username) {
        Set<RefreshTokenInfo> tokens = userRefreshTokenMap.get(username);
        if (tokens == null) {
            return 0;
        }
        return (int) tokens.stream().filter(token -> !token.isExpired()).count();
    }

    public void cleanRevokedTokens() {
        log.debug("清理撤销令牌黑名单，当前大小: {}", revokedRefreshTokens.size());
    }

    // 兼容性方法 - 为保持API兼容性
    public String extractUsernameFromExpiredToken(String token) {
        return extractUsername(token);
    }

    public boolean isTokenExpiredSafely(String token) {
        return isTokenExpired(token);
    }

    public String getTokenTypeSafely(String token) {
        return getTokenType(token);
    }

    public String extractUsernameFromRefreshToken(String refreshToken) {
        return extractUsername(refreshToken);
    }

    public String extractJtiFromRefreshToken(String refreshToken) {
        return extractJti(refreshToken);
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
     * RefreshToken信息内部类
     */
    @Getter
    private static class RefreshTokenInfo {
        private final String jti; // JWT ID - 唯一标识
        private final Date expireTime; // 过期时间
        private final String deviceInfo; // 设备信息
        private final String clientIp; // 客户端IP
        private final Date createdTime; // 创建时间
        private final String refreshToken;

        public RefreshTokenInfo(String jti, String refreshToken, Date expireTime,
                String deviceInfo, String clientIp) {
            this.jti = jti;
            this.expireTime = expireTime;
            this.deviceInfo = deviceInfo;
            this.refreshToken = refreshToken;
            this.clientIp = clientIp;
            this.createdTime = new Date();
        }

        public boolean isExpired() {
            return new Date().after(expireTime);
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
}
