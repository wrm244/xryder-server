/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.150.203
 Source Server Type    : MySQL
 Source Server Version : 80031 (8.0.31)
 Source Host           : 192.168.150.203:3306
 Source Schema         : ryder

 Target Server Type    : MySQL
 Target Server Version : 80031 (8.0.31)
 File Encoding         : 65001

 Date: 15/10/2024 19:56:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `scope` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `creator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, '部门管理', 'system', NULL, NULL, NULL);
INSERT INTO `sys_permission` VALUES (2, '用户管理', 'system', NULL, NULL, NULL);
INSERT INTO `sys_permission` VALUES (3, '角色管理', 'system', NULL, NULL, NULL);
INSERT INTO `sys_permission` VALUES (4, '通知公告', 'system', NULL, NULL, NULL);
INSERT INTO `sys_permission` VALUES (5, '操作日志', 'system', NULL, NULL, NULL);
INSERT INTO `sys_permission` VALUES (6, '登录日志', 'system', NULL, NULL, NULL);
INSERT INTO `sys_permission` VALUES (7, '职位管理', 'system', NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
