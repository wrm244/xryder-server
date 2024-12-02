package cn.xryder.base.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

/**
 * @Author: joetao
 * @Date: 2024/8/1 14:57
 */
@Service
@Slf4j
public class LoginAttemptService {

    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        super();
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(Duration.ofMinutes(30)).build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(String key) {
        if (key != null) {
            attemptsCache.invalidate(key);
        }
    }

    public void loginFailed(String key) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            log.error("获取缓存失败：{}", e.getMessage());
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            int maxAttempt = 10;
            return attemptsCache.get(key) >= maxAttempt;
        } catch (ExecutionException e) {
            return false;
        }
    }
}
