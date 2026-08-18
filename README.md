> [!CAUTION]
> [灵感回路 idealoop.top](https://idealoop.top)
>
> 灵感回路💡: 一个面向 AI 时代独立开发者、创业者与副业探索者的 Idea 发现、评估与讨论社区。

> [!TIP]
> **投出去只是上半场, 面试才是决定 Offer 的下半场。**
>
> [beacon 灯塔](https://github.com/yangfeng20/beacon): 同作者的 AI 面试助手, Windows 桌面端装完就能用。面试全程记录 + 自动复盘报告 + 拿你自己的简历做模拟面试。

<br/>

---


# 🎉 AI 工作猎手 (AI Job Hunting) - 正式开源！

**找工作，用 AI 工作猎手！让 AI 成为您的求职分身，24/7 在线助您斩获 Offer。**

本项目现已正式开源！您可以选择：
1. **个人自用**: 免费部署后端，通过 AI 自动投递、智能回复，大幅提升找工作效率。
2. **商业运营 (赚米)**: 内置完整的支付宝支付系统，您可以部署后作为服务商，为广大求职者提供 AI 赋能服务。

> **🚀 快速开始**: 请阅读 [部署指南v1.md](./部署指南v1.md) 进行后端部署。部署完成后，在 UI 插件的“服务器配置”中修改 API 地址即可使用。

> 配置服务器地址,点击测试连接配置到新的服务器
![](file/server_add.png)

---

## 🏗️ 项目架构

本项目采用前后端分离架构，专为浏览器环境深度定制：

```text
[ 浏览器 (Boss直聘) ] <--- 注入 --- [ UI 脚本 (Vue 3 + Vite) ]
                                          |
                                   (HTTPS 请求 / SSE)
                                          |
                                   [ Nginx 反向代理 ]
                                          |
                                   [ 后端 Server (Spring Boot 3) ]
                                     /      |      \
                              [ Spring AI ] [ MySQL ] [ 支付宝 ]
                                (Kimi/GPT)
```

- **前端 (UI)**: 基于 `vite-plugin-monkey` 构建的高级油猴脚本，完美嵌入 Boss 直聘界面。
- **后端 (Server)**: 基于 Spring Boot 3，集成 Spring AI 框架，支持多模型池化管理。
- **协议层**: 自研 WebSocket Hook，实时拦截并解析 Boss 直聘的 Protobuf 通讯协议。
- 页面ui脚本说明
  - 根目录[ai-job-hunting.user.js](ai-job-hunting.user.js)脚本为依赖cdn版本，部分网络可能不可用
  - 根目录[ai-job-hunting.bundle.user.js](ai-job-hunting.bundle.user.js)为包含依赖版本，无需cdn下载依赖，如果页面反复刷新也不显示ui，可以切换为当前脚本


> 随便提一句，云厂商的策略都是老用户与狗不允许入内,加上token的消耗，还不够续费服务器的
> 所以服务器将从2026年4月13左右关停，现在已经支持本地投递了
> 如果需要使用ai相关功能，请切换到其他人部署的服务器，或者仔细部署。
![示例](/file/server.png)
---

## AI工作猎手
<br/>

- **`找工作，用AI工作猎手！让AI帮您找工作！`** AI坐席：【DeepSeek+ChatGpt】赋能，ai助理作为您的求职者分身24小时 * 7在线找工作，并结合您的简历信息定制化回复。批量投递，自动发送简历，交换联系方式。hr拒绝挽留。高意向邮件通知，让您不错过每一份工作机会。
  <br/>
---

![示例](/file/ai_seat.png)
![示例](/file/home.png)
![示例1](/file/ai_config.png)


### 效果展示
- \- 点击下方链接观看效果演示。
- \- <a href="https://www.bilibili.com/video/BV1y6PjesEvi" target="_blank">AI工作猎手效果演示</a>

### 视频教程
- \- 点击下方链接观看视频教程。
- \- <a href="https://www.bilibili.com/video/BV1HKAyebESp" target="_blank">AI工作猎手使用教程</a>

## 安装使用
- [greasyfork搜索:(AI工作猎手)](https://greasyfork.org/zh-CN/scripts/527733)
- 或者下载项目中的`ai-job-hunting.user.js`文件，通过油猴本地脚本导入。
- 或者打开浏览器输入地址油猴自动安装：[https://gitee.com/yangfeng20/ai-job/raw/master/ai-job-hunting.user.js](https://gitee.com/yangfeng20/ai-job/raw/master/ai-job-hunting.user.js)
- 记得打开浏览器的开发者模式，新版本油猴需要打开开发者模式才能运行脚本。
- Boss首页没有功能面板，要在工作列表页面才有功能面板：[https://www.zhipin.com/web/geek/job](https://www.zhipin.com/web/geek/job)


## 功能介绍

### AI坐席
- \- 让AI作为您的求职者分身，帮助您快速找到工作。
- \- 智能回复HR的消息,结合您的简历信息进行定制化回答。
- \- 预设问题支持，根据场景只能匹配您的预设问题，进行智能回答。
- \- AI快捷回复发送简历，交换 wx、联系方式。
- \- HR拒绝挽留，当hr拒绝您时，可触发拒绝挽留。主动发送简历，并发送自定义的挽留语。

<br/>

### 工作通知
- \- 支持AI坐席与HR的每轮沟通，发送邮件通知。
- \- 高意向职位邮件通知，通过设置的关键字或者对话轮数，发送高意向职位的通知。

<br/>

### 投递工具
- \- 批量投递简历。自定义单次投递数量。
- \- 发送自定义招呼语，充分展现您的优势。
- \- 自定义筛选过滤，根据您的需求筛选公司，职位，薪资...。

<br/>

### AI坐席使用
- \- 购买ai坐席之后，可在AI助手中开启全局AI坐席功能。
- \- 开启全局AI坐席功能后，HR的消息将会自动转发给AI坐席进行智能回复。
- \- 可随时打断AI坐席的回复，当在web端或app端自己回复HR之后，当前会话的AI坐席将会自动停止。
- \- 停止后，可在web端的消息列表页面中点击【重启当前会话AI坐席】按钮，重新开启当前会话的AI坐席。
- \- 也可在web端通过快捷指令【start】输入到聊天框并发送，开启当前会话的AI坐席。boss端并不会收到当前消息。
- \- 当hr拒绝您时，可触发拒绝挽留。主动发送简历，并发送自定义的挽留语。
- \- 当hr通过boss向你交换联系方式时，ai助手自动交换。
- \- 可在偏好设置中设置预设问题，ai坐席根据场景智能匹配您的预设问题，进行智能回答。

<br/>


### 常见问题
- \- 在boss更新简历之后，请重新导入简历。
- \- 脚本未运行，请尝试刷新页面。

---


### 更新日志

#### 2025.03.13
- 新增ai招呼语功能。
- 产品支持试用。
- 页面优化。


---

## 🔗 相关项目

### beacon 灯塔 - 让每一场面试, 都变成下一场的准备

> 面试完当天还记得七七八八, 三天后只剩一句「好像问了个分布式锁」。

AI 工作猎手负责把你送进面试间, [**beacon 灯塔**](https://github.com/yangfeng20/beacon) 负责让你从面试间里带走点东西:

- **全程记录**: 面试对话实时转写, 结束后自动生成复盘报告, 不再靠印象复盘
- **模拟面试**: 拿你自己的简历和项目练, 而不是背通用题库
- **简历素材整理**: 让 AI 读一遍你的代码仓库, 把三年前那个只剩一句「负责订单模块」的项目还原成能写进简历的细节
- **提示词不是黑盒**: 生成建议、写复盘、整理素材的每一处提示词都摆在那里, 全文可见也能改成自己的
- **录音留在自己电脑上**: 面试录音只存本机不上传, 声纹是最敏感的一类个人信息

Windows 桌面应用, [下载即用](https://github.com/yangfeng20/beacon#下载), 不需要部署服务端。

[![beacon 复盘报告](https://raw.githubusercontent.com/yangfeng20/beacon/main/images/review-report.png)](https://github.com/yangfeng20/beacon)

### 灵感回路

面向 AI 时代独立开发者、创业者与副业探索者的 Idea 发现、评估与讨论社区: [idealoop.top](https://idealoop.top)
