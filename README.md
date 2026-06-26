# 简历分析平台 (Resume Platform)

基于 AI 的智能简历分析与模拟面试系统。上传简历后，系统可自动分析简历内容，生成修改建议，并模拟技术面试。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Axios |
| 网关 | Spring Cloud Gateway 2023 |
| 后端 | Spring Boot 3.2 + Spring AI 0.8 + MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.0 |
| 文件处理 | Apache Tika 2.9 |
| AI 模型 | DeepSeek（通过 Spring AI OpenAI 兼容接口调用） |

## 项目结构

```
resume-project/
├── server/                    # 后端根模块
│   ├── pom.xml                # Maven 父 POM
│   ├── sql/
│   │   └── init.sql           # 数据库初始化脚本
│   ├── gateway/               # API 网关 (端口 8080)
│   │   ├── pom.xml
│   │   └── src/main/java/com/resume/gateway/
│   └── backend/               # 后端服务 (端口 8081)
│       ├── pom.xml
│       └── src/main/java/com/resume/
│           ├── ResumeApplication.java
│           ├── common/        # 统一返回结果、全局异常处理
│           ├── config/        # MyBatis-Plus、CORS、AI 配置
│           ├── entity/        # 数据库实体
│           ├── dto/           # 请求/响应 DTO
│           ├── mapper/        # MyBatis-Plus Mapper
│           ├── service/       # 业务逻辑
│           └── controller/    # RESTful 接口
├── frontend/                  # Vue 3 前端 (端口 3000)
│   ├── public/
│   │   └── favicon.svg        # 站点图标
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/               # Axios 封装接口
│       ├── views/             # 页面组件
│       ├── components/        # 通用组件
│       └── router/            # 路由配置
├── .gitignore
└── README.md
```

## 快速开始

### 1. 环境准备

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 2. 创建数据库

```sql
-- 执行 SQL 脚本创建数据库和表
mysql -u root -p < server/sql/init.sql
```

### 3. 配置环境变量

**后端**（`server/backend/src/main/resources/application.yml`）：

```yaml
spring:
  datasource:
    username: root
    password: your-password
  ai:
    openai:
      api-key: your-deepseek-api-key
```

建议通过环境变量设置：
- `DEEPSEEK_API_KEY` - DeepSeek API Key
- `DEEPSEEK_BASE_URL` - DeepSeek API 地址（可选，默认 https://api.deepseek.com）

### 4. 启动后端

```bash
# 进入后端模块
cd server
# 构建项目
mvn clean package -DskipTests

# 启动网关
java -jar gateway/target/gateway-1.0.0.jar

# 启动后端服务（另一个终端）
java -jar backend/target/backend-1.0.0.jar

# 返回项目根目录
cd ..
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`，通过 Vite 代理将 `/api` 请求转发到网关 `http://localhost:8080`。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/resume/upload` | 上传简历文件 |
| POST | `/api/resume/parse/{id}` | AI 解析简历 |
| POST | `/api/resume/advice` | 获取修改建议 |
| GET  | `/api/resume/{id}` | 获取简历信息 |
| POST | `/api/interview/start` | 开始模拟面试（支持上传岗位截图） |
| POST | `/api/interview/chat` | 继续面试（提交答案） |

## 功能说明

1. **简历上传**：支持 PDF/Word/TXT 文件，自动提取文本内容
2. **智能解析**：AI 提取姓名、学历、技能、项目等结构化信息
3. **修改建议**：根据目标岗位生成具体修改建议，无需修改时自动提示，支持追问对话
4. **模拟面试**：粘贴岗位 JD 后 AI 自动生成面试题，支持上传岗位截图辅助分析
5. **逐题作答评分**：每次回答后 AI 根据简历和岗位要求评分并追问
6. **面试报告**：结束时生成综合评价、能力评分和理想回答参考
7. **评分系统**：每题 0-5 分，总分 60 及格，90 优秀

## 注意事项

- 上传文件限制最大 10MB
- AI 调用建议配置合理超时时间（默认 2 分钟）
- 所有 AI 返回均有异常降级处理
- 日志中可查看 AI 调用耗时明细（DEBUG 级别）
