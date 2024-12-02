package cn.xryder.base.common;

import java.util.UUID;

/**
 * @Author: joetao
 * @Date: 2024/9/2 10:53
 */
public class CommonUtil {
    // 简单密码校验逻辑
    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        return password.length() >= 8 && password.length() <= 16
                && Character.isLetter(password.charAt(0))
                && password.matches(".*\\d.*")
                && password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }

    public static String generateUniqueID(int uuidLength, int timestampLength) {
        // 生成 UUID 并截取指定的前几位
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, uuidLength);

        // 获取当前时间戳并转换为字符串
        String timestamp = String.valueOf(System.currentTimeMillis());
        // 截取时间戳的后几位
        String timestampPart = timestamp.substring(timestamp.length() - timestampLength);

        // 拼接 UUID 和时间戳部分
        return uuidPart + timestampPart;
    }
}
