
## AI工作猎手
<br/>

- **`找工作，用AI工作猎手！让AI帮您找工作！`** AI坐席：【DeepSeek+ChatGpt】赋能，ai助理作为您的求职者分身24小时 * 7在线找工作，并结合您的简历信息定制化回复。批量投递，自动发送简历，交换联系方式。hr拒绝挽留。高意向邮件通知，让您不错过每一份工作机会。
  <br/>
---

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

### 视频教程
- \- 点击下方链接观看视频教程。
- \- <a href="https://www.bilibili.com/video/BV1HKAyebESp" target="_blank">AI工作猎手使用教程</a>

### 常见问题
- \- 在boss更新简历之后，请重新导入简历。
- \- 脚本未运行，请尝试刷新页面。

---

# AI Job Hunting (AI工作猎手)

本项目是一个基于 Vue 3 + Vite 开发的高级油猴脚本（User Script），旨在为“Boss直聘”提供 AI 赋能的求职辅助功能。

## 项目概览 (Project Overview)

*   **目标平台**：Boss直聘 (zhipin.com)
*   **核心功能**：
  *   **AI坐席**：自动回复 HR 消息，结合简历信息进行定制化回答。
  *   **WebSocket Hook**：拦截并解析 Boss 直聘的加密通信协议（Protobuf）。
  *   **自动化流程**：批量投递简历、自动交换微信/联系方式、高意向邮件通知。
  *   **智能过滤**：根据自定义规则筛选职位和公司。
*   **技术栈**：
  *   **框架**：Vue 3 (Composition API)
  *   **状态管理**：Pinia
  *   **UI 组件库**：Element Plus
  *   **构建工具**：Vite + `vite-plugin-monkey`
  *   **协议处理**：Protobuf.js

## 核心架构 (Architecture)

1.  **入口点 (`src/main.ts`)**：负责脚本初始化、平台检测及 UI 挂载。
2.  **WebSocket 劫持 (`src/webSocket/hookMain.ts`)**：通过代理 `window.WebSocket` 拦截流量。
3.  **协议层 (`src/webSocket/protobuf.ts`)**：处理 Boss 直聘专有的 Protobuf 协议数据。
4.  **平台逻辑 (`src/platform/bossPlatform.ts`)**：封装平台特有的操作逻辑，如消息处理、简历发送。
5.  **AI 集成 (`src/platform/aiPower.ts`)**：与后端 AI 服务通信，获取回复策略。
6.  **持久化管理 (`src/stores/index.ts`)**：管理用户配置、运行状态及偏好设置。

## 开发与运行 (Development & Running)

### 环境要求
*   Node.js (建议 v18+)
*   pnpm (推荐使用)

### 常用命令
*   **安装依赖**：`pnpm install`
*   **开发模式**：`pnpm run dev` (启动 Vite 开发服务器，在浏览器中通过油猴安装开发版脚本)
*   **生产构建**：`pnpm run build` (生成的脚本位于 `dist/ai-job-hunting.user.js`)

## 开发约定 (Development Conventions)

1.  **Hook 机制**：修改原有网站行为时，优先使用 `src/webSocket/hookMain.ts` 中的拦截器，避免直接操作 DOM。
2.  **日志记录**：使用 `src/logging` 提供的 `logger` 进行调试，关键运行数据应记录到 `LogRecorder` 中。
3.  **UI 隔离**：由于是脚本注入，所有 UI 元素应挂载在独立的 Shadow DOM 或特定的容器中，避免样式冲突。
4.  **Protobuf 更新**：如果 Boss 直聘更新了通信协议，需同步更新 `src/webSocket/protobuf.ts` 中的定义。
5.  **异步操作**：与 AI 交互通常耗时较长，务必处理好超时和并发控制（参考 `AiPower.ask` 的超时配置）。

## 关键文件 (Key Files)

*   `vite.config.ts`: 包含 `vite-plugin-monkey` 配置，定义脚本元数据（如 `@match`, `@grant`）。
*   `src/platform/bossPlatform.ts`: 实现 `Platform` 接口的核心类。
*   `src/webSocket/hookMain.ts`: WebSocket Proxy 实现逻辑。
*   `src/stores/remote.ts`: 处理与后端服务的状态同步。

## TODO / 扩展计划
*   [ ] 适配更多招聘平台（如前程无忧、猎聘）。
*   [ ] 优化 AI 提示词算法，提升回复准确度。
*   [ ] 增强验证码自动识别与绕过机制。



