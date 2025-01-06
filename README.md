# X.Ryder
## 简介
这是 **X.Ryder** 的后台程序，使用Java和Spring Boot开发，具备登录、系统管理模块和ai问答功能。
搭配 [xryder-web](https://github.com/pipijoe/xryder-web) 这个基于react的前端程序,可快速构建一个中后台系统的基础代码。

演示系统地址：[X.Ryder](https://xryder.cn)  
账号：admin  
密码：admin123  

## 技术栈
- Java 21
- Spring Boot
- Spring Security
- Spring Data Jpa
- Spring AI

## 如何使用
1. 执行根目录下sql文件夹下的sql文件，创建响应的表和导入数据。
2. 配置zhipu ai的appkey环境变量（或者替换为open ai 的appkey）
3. 启动项目（注意，本项目使用的是java21，启动前需要确保机器已正确配置jdk21）
4. 自动初始化管理员账号。cn.xryder.base.config.DataInitializer为账户初始化类，可以查看管理员账号密码。首次运行会在管理控制台输出管理员账号密码。

# 📬 联系方式

你可以通过这些方式跟我联系：

- Email:  cutesimba@163.com

感谢你在我的互联网角落停留片刻！ 💫