package cn.xryder.base.common;

import java.util.UUID;


/**
 * 常用工具类
 *
 * @author wrm244
 */

public class CommonUtil {
    private CommonUtil() {
        throw new IllegalStateException("Utility class");
    }

    // 简单密码校验逻辑（增强安全性）
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        int length = password.length();
        if (length < 8 || length > 16) {
            return false;
        }
        if (!Character.isLetter(password.charAt(0))) {
            return false;
        }

        // 密码必须满足：
        // - 至少一个大写字母
        // - 至少一个小写字母
        // - 至少一个数字
        // - 至少一个指定的特殊字符
        String regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).+$";
        return password.matches(regex);
    }

    // 生成由 UUID 和时间戳组成的唯一 ID
    public static String generateUuidWithTimestampId(int uuidLength, int timestampLength) {
        // 参数合法性校验
        if (uuidLength < 0 || uuidLength > 32) {
            throw new IllegalArgumentException("uuidLength must be between 0 and 32");
        }
        if (timestampLength < 0 || timestampLength > 13) {
            throw new IllegalArgumentException("timestampLength must be between 0 and 13");
        }

        // 生成 UUID 并截取指定长度
        String uuidPart = UUID.randomUUID().toString().replace("-", "");
        if (uuidLength > 0) {
            uuidPart = uuidPart.substring(0, uuidLength);
        } else {
            uuidPart = "";
        }

        // 获取当前时间戳并截取后几位
        String timestamp = String.valueOf(System.currentTimeMillis());
        int len = Math.min(timestampLength, timestamp.length());
        String timestampPart = timestamp.substring(timestamp.length() - len);

        // 拼接结果
        return uuidPart + timestampPart;
    }
}
