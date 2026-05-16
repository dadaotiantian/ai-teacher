# 背单词教学程序 - 完整项目需求提示词

请根据本文档生成一个可运行的前后端项目。后端使用 Java + Spring Boot + Netty WebSocket，前端使用 Vue 3 + TypeScript + Vite。项目目标是实现一个支持多账号、多角色、智能体对话和单词记忆复习的教学系统。

## 一、项目概述

### 1.1 项目名称

EnglishWordMemorize - 智能背单词教学系统

### 1.2 技术栈

#### 后端技术栈

- 基础框架：Spring Boot 2.7+
- AI 底座：Spring AI Alibaba
- 网络通信：Netty WebSocket，使用二进制消息协议
- 项目管理：Maven
- 数据库：SQLite，运行数据文件为 `data/working/ai_data_1.db`，静态单词配置文件为 `data/english_config.db`
- Java 版本：17 或 21
- 后端端口：8080
- 包结构：`com.dadaotiantian.memorize`

#### 前端技术栈

- 框架：Vue 3 + TypeScript
- 构建工具：Vite
- 状态管理：Pinia
- 前端端口：5173
- 通信协议：WebSocket。登录、选角、聊天、单词考核都通过 WebSocket 完成，不额外提供 HTTP API。

### 1.3 核心特性

- 多账号登录系统
- 每个账号最多创建 6 个角色
- UID 表示角色 ID，不表示账号 ID，全局唯一
- 智能体模块化管理
- WebSocket 二进制协议通信
- SM-2 记忆算法复习
- 三种单词考核类型：音标、拼写、使用
- 启动时自动初始化 SQLite 表结构，并导入单词数据

## 二、后端详细设计

### 2.1 项目结构

```text
src/main/java/com/dadaotiantian/memorize/
├── Launcher.java                         # 启动类
├── config/
│   ├── ServerConfig.java                 # 加载 server_config.json
│   ├── DatabaseConfig.java               # SQLite 配置
│   └── WebSocketConfig.java              # WebSocket 配置
├── network/
│   ├── WebSocketServer.java              # Netty WebSocket 服务端
│   ├── AIServerMsgHandler.java           # 消息接收回调类
│   ├── ClientFunctionDef.java            # 客户端到服务器消息定义
│   └── ServerFunctionDef.java            # 服务器到客户端消息定义
├── thread/
│   ├── ThreadManager.java                # 线程池管理类
│   └── AbstractActionQueue.java          # 消息队列抽象类
├── account/
│   ├── AccountManager.java               # 账号管理器
│   ├── Account.java                      # 账号实体
│   └── AccountDB.java                    # 账号数据操作
├── player/
│   ├── PlayerManager.java                # 角色管理器
│   ├── Player.java                       # 角色实体
│   ├── PlayerDB.java                     # 角色数据操作
│   └── BasePlayer.java         # 模块化管理基类
├── agent/
│   ├── AgentManager.java                 # 智能体管理器
│   ├── AgentObject.java                  # 智能体对象
│   └── modules/
│       ├── DialogueModule.java           # 对话模块
│       ├── WordTestModule.java           # 单词考核模块
│       └── StudyPlanModule.java          # 学习计划模块
├── db/
│   ├── MaxIdMgr.java                     # ID 管理器
│   ├── SQLiteHelper.java                 # 数据库助手
│   └── migration/
│       └── DatabaseInitializer.java      # 数据库初始化
├── word/
│   ├── WordManager.java                  # 单词管理器
│   ├── MemoryWordService.java            # 记忆服务
│   └── ReviewAlgorithm.java              # SM-2 复习算法
├── handler/
└── utils/
    ├── PasswordUtil.java                 # 密码哈希
    ├── JsonUtil.java                     # JSON 处理
    └── LogUtil.java                      # 日志工具
```

### 2.2 配置文件

`config/server_config.json`：

```json
{
  "server": {
    "host": "0.0.0.0",
    "port": 8080,
    "websocket_path": "/ws",
    "boss_threads": 1,
    "worker_threads": 4,
    "business_threads": 8
  },
  "database": {
    "url": "jdbc:sqlite:data/working/ai_data_1.db",
    "enable_wal": true,
    "busy_timeout_ms": 5000
  },
  "heartbeat": {
    "interval_seconds": 30,
    "timeout_seconds": 90
  },
  "word": {
    "config_database_url": "jdbc:sqlite:data/english_config.db",
    "new_words_per_day": 20,
    "review_words_per_day": 100,
    "correct_threshold": 0.8
  }
}
```

### 2.3 WebSocket 消息协议

消息体使用二进制格式：

```text
+----------------+----------------+----------------+------------------+
| 消息ID(2字节)   | 消息长度(4字节) | UID(4字节)      | 消息体(JSON)      |
+----------------+----------------+----------------+------------------+
| unsigned short | unsigned int   | unsigned int   | UTF-8 JSON       |
+----------------+----------------+----------------+------------------+
```

说明：

- 消息 ID 使用全局唯一编号，不能因为方向不同而复用。
- 登录前 UID 固定为 0。
- 登录成功后仍然没有 UID，只有 `account_id` 和 `token`。
- 用户选择角色成功后，后续请求才使用该角色的 UID。
- 消息长度表示 JSON 消息体的字节长度，不包含 10 字节消息头。
- 数字字段统一使用大端序。

#### 客户端到服务器消息

```java
public enum ClientFunctionDef {
    LOGIN_REQ(0x0001),
    HEARTBEAT_REQ(0x0002),
    LOGOUT_REQ(0x0003),
    REGISTER_REQ(0x0011),
    LIST_PLAYER_REQ(0x0021),
    CREATE_PLAYER_REQ(0x0022),
    SELECT_PLAYER_REQ(0x0023),
    DELETE_PLAYER_REQ(0x0024),
    CREATE_AGENT_REQ(0x0031),
    LIST_AGENT_REQ(0x0032),
    WORD_TEST_REQ(0x0041),
    WORD_REVIEW_REQ(0x0042),
    CHAT_MESSAGE_REQ(0x0051);

    private final short msgId;
}
```

#### 服务器到客户端消息

```java
public enum ServerFunctionDef {
    LOGIN_RSP(0x1001),
    HEARTBEAT_RSP(0x1002),
    LOGOUT_RSP(0x1003),
    REGISTER_RSP(0x1011),
    LIST_PLAYER_RSP(0x1021),
    CREATE_PLAYER_RSP(0x1022),
    SELECT_PLAYER_RSP(0x1023),
    DELETE_PLAYER_RSP(0x1024),
    CREATE_AGENT_RSP(0x1031),
    LIST_AGENT_RSP(0x1032),
    WORD_TEST_RSP(0x1041),
    WORD_REVIEW_RSP(0x1042),
    CHAT_MESSAGE_RSP(0x1051),
    ERROR_RSP(0x1FFF);

    private final short msgId;
}
```

#### 登录和选角流程

```text
1. 客户端连接 WebSocket：ws://host:8080/ws
2. 客户端发送 LOGIN_REQ，消息头 UID = 0
   请求体：{"username":"xxx","password":"plain_or_client_hash"}
3. 服务端验证账号密码，返回 LOGIN_RSP
   成功：{"result":0,"account_id":1001,"token":"session_token","message":"登录成功"}
   失败：{"result":1,"account_id":0,"token":"","message":"用户名或密码错误"}
4. 客户端发送 LIST_PLAYER_REQ，消息头 UID = 0，请求体携带 token
5. 用户选择角色，发送 SELECT_PLAYER_REQ
6. 服务端返回 SELECT_PLAYER_RSP
   成功：{"result":0,"uid":2001,"player_name":"Tom","message":"选角成功"}
7. 后续业务请求的消息头 UID 使用选中的角色 ID
```

### 2.4 数据库设计

数据库文件：

```text
data/working/ai_data_1.db # 运行数据
data/english_config.db # 静态单词配置
```

SQLite 建表语句必须使用 SQLite 语法，索引需要单独创建，不能在 `CREATE TABLE` 内写 MySQL 风格的 `INDEX`。

#### 账号表 `t_u_account`

```sql
CREATE TABLE IF NOT EXISTS t_u_account (
    account_id INTEGER PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    password_salt TEXT,
    email TEXT,
    created_time INTEGER NOT NULL,
    last_login_time INTEGER,
    status INTEGER DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_t_u_account_username
ON t_u_account(username);
```

#### 角色表 `t_u_player`

```sql
CREATE TABLE IF NOT EXISTS t_u_player (
    uid INTEGER PRIMARY KEY,
    account_id INTEGER NOT NULL,
    player_name TEXT NOT NULL,
    level INTEGER DEFAULT 1,
    experience INTEGER DEFAULT 0,
    created_time INTEGER NOT NULL,
    last_login_time INTEGER,
    agent_config TEXT,
    status INTEGER DEFAULT 1,
    FOREIGN KEY(account_id) REFERENCES t_u_account(account_id)
);

CREATE INDEX IF NOT EXISTS idx_t_u_player_account_id
ON t_u_player(account_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_u_player_account_name
ON t_u_player(account_id, player_name);
```

#### 单词表 `t_s_english_words`

该表需要从现有文件 `sql/t_s_english_words.sql` 转换导入。原文件是 MySQL 导出，目标 SQLite 表结构如下：

```sql
CREATE TABLE IF NOT EXISTS t_s_english_words (
    id INTEGER PRIMARY KEY,
    word_str TEXT NOT NULL UNIQUE,
    pronunciation_uk TEXT,
    pronunciation_us TEXT,
    meaning_zh TEXT,
    difficulty INTEGER DEFAULT 1,
    grade INTEGER DEFAULT 1,
    frequency INTEGER
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_s_english_words_word_str
ON t_s_english_words(word_str);
```

字段映射：

```text
MySQL id               -> SQLite id
MySQL word_str         -> SQLite word_str
MySQL pronunciation_uk -> SQLite pronunciation_uk
MySQL pronunciation_us -> SQLite pronunciation_us
MySQL meaning_zh       -> SQLite meaning_zh
MySQL difficulty       -> SQLite difficulty
MySQL grade            -> SQLite grade
MySQL frequency        -> SQLite frequency
```

#### 单词释义表 `t_s_english_word_definitions`

该表需要从现有文件 `sql/t_s_english_word_definitions.sql` 转换导入。原文件是 MySQL 导出，目标 SQLite 表结构如下：

```sql
CREATE TABLE IF NOT EXISTS t_s_english_word_definitions (
    id INTEGER PRIMARY KEY,
    word_id INTEGER NOT NULL,
    part_of_speech TEXT,
    meaning TEXT NOT NULL,
    meaning_zh TEXT,
    example TEXT,
    sort_order INTEGER DEFAULT 0,
    FOREIGN KEY(word_id) REFERENCES t_s_english_words(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_t_s_english_word_definitions_word_id
ON t_s_english_word_definitions(word_id);
```

字段映射：

```text
MySQL id             -> SQLite id
MySQL word_id        -> SQLite word_id
MySQL part_of_speech -> SQLite part_of_speech
MySQL meaning        -> SQLite meaning
MySQL meaning_zh     -> SQLite meaning_zh
MySQL example        -> SQLite example
MySQL sort_order     -> SQLite sort_order
```

#### 用户单词记忆表 `t_u_memory_word`

```sql
CREATE TABLE IF NOT EXISTS t_u_memory_word (
    uid INTEGER NOT NULL,
    word_id INTEGER NOT NULL,
    data TEXT NOT NULL,
    last_review_time INTEGER,
    review_count INTEGER DEFAULT 0,
    next_review_time INTEGER,
    created_time INTEGER,
    updated_time INTEGER,
    PRIMARY KEY(uid, word_id),
    FOREIGN KEY(uid) REFERENCES t_u_player(uid)
);

CREATE INDEX IF NOT EXISTS idx_t_u_memory_word_uid
ON t_u_memory_word(uid);

CREATE INDEX IF NOT EXISTS idx_t_u_memory_word_next_review
ON t_u_memory_word(next_review_time);
```

`data` 字段 JSON 示例：

```json
{
  "records": [
    {"timestamp": 1704067200, "type": "pronunciation", "result": 1},
    {"timestamp": 1704153600, "type": "spelling", "result": 0},
    {"timestamp": 1704240000, "type": "usage", "result": 1}
  ],
  "easiness_factor": 2.5,
  "interval": 1,
  "repetition": 3
}
```

### 2.5 ID 管理要求

`MaxIdMgr` 用于管理由程序创建的数据 ID。不要和 SQLite `AUTOINCREMENT` 混用。

要求：

1. 管理 `t_u_account.account_id` 和 `t_u_player.uid`。
2. 启动时读取对应字段当前最大值。
3. 提供线程安全方法 `getMaxId(String tableName, String fieldName)`。
4. 每次获取时返回当前最大值 + 1，并更新内存中的最大值。
5. 静态单词表 `t_s_english_words` 和 `t_s_english_word_definitions` 存放在 `data/english_config.db`，使用导入文件里的原始 ID，不由 `MaxIdMgr` 重新生成。

### 2.6 核心类要求

#### ThreadManager

1. 管理业务线程池。
2. 管理 Netty Worker 线程池。
3. 线程数量从 `server_config.json` 读取。
4. 支持优雅关闭。

#### AbstractActionQueue

1. 每个玩家一个独立队列。
2. 队列大小限制为 1000。
3. 支持优先级，系统消息优先于业务消息。
4. 队列满时丢弃最旧的低优先级消息，并记录日志。

#### Player

1. 使用 `moduleMap` 管理所有模块，
2. 至少包含单词记忆模块。
3. 支持模块初始化、启动、停止。
4. 每个 Player 对应一个全局唯一 UID。

#### AgentObject

1. 使用模块化能力管理，能力接口命名为 `IAbility`。
2. 基础能力模块包括对话、单词考核、学习计划。
3. 支持动态添加和移除能力模块。
4. 每个智能体绑定一个角色 `Player`。
5. 客户端消息可以路由到对应能力模块执行。

### 2.7 记忆算法要求

实现简化版 SM-2：

```text
1. repetition 初始为 0。
2. easiness_factor 初始为 2.5。
3. 正确时：
   - repetition = repetition + 1
   - easiness_factor = min(2.5, easiness_factor + 0.1)
4. 错误时：
   - repetition = 0
   - easiness_factor = max(1.3, easiness_factor - 0.2)
5. interval：
   - repetition <= 1：1 天
   - repetition == 2：6 天
   - repetition > 2：round(previous_interval * easiness_factor)
6. next_review_time = 当前时间 + interval 天
```

考核类型：

```json
{
  "pronunciation": "音标考核：显示音标，让用户拼写单词",
  "spelling": "拼写考核：显示中文释义，让用户拼写单词",
  "usage": "使用考核：显示例句填空，让用户填写单词"
}
```

## 三、前端详细设计

### 3.1 项目结构

```text
client/
├── src/
│   ├── main.ts
│   ├── App.vue
│   ├── env.d.ts
│   ├── assets/
│   ├── components/
│   │   ├── WordTestCard.vue
│   │   ├── AgentAvatar.vue
│   │   └── ChatBubble.vue
│   ├── views/
│   │   ├── LoginView.vue
│   │   ├── SelectCharacterView.vue
│   │   ├── CreateCharacterView.vue
│   │   ├── CreateAgentView.vue
│   │   └── ChatView.vue
│   ├── stores/
│   │   ├── account.ts
│   │   ├── player.ts
│   │   ├── agent.ts
│   │   └── websocket.ts
│   ├── types/
│   │   ├── message.ts
│   │   ├── protocol.ts
│   │   └── models.ts
│   ├── composables/
│   │   ├── useWebSocket.ts
│   │   ├── useWordGame.ts
│   │   └── useMessageHandler.ts
│   ├── services/
│   │   └── wsService.ts
│   └── utils/
│       └── validator.ts
├── index.html
├── package.json
├── vite.config.ts
└── tsconfig.json
```

### 3.2 页面功能

#### 登录页面 `LoginView.vue`

1. 表单字段：用户名、密码。
2. 使用 WebSocket 发送登录消息，不使用 HTTP。
3. 登录成功后保存 `account_id` 和 `token` 到状态管理和 localStorage。
4. 登录成功后跳转到选角页面。
5. 登录失败显示错误信息。

#### 选角页面 `SelectCharacterView.vue`

1. 显示该账号下所有角色，最多 6 个。
2. 每个角色显示名称、等级、经验、最后登录时间。
3. 支持选择角色、创建角色、删除角色。
4. 角色数量小于 6 时显示创建按钮。
5. 删除角色需要二次确认。
6. 选择角色成功后保存 UID，并进入对话页面。

#### 创角页面 `CreateCharacterView.vue`

1. 表单字段：角色名称，长度 2 到 12 个字符。
2. 显示当前已创建角色数量和最大数量 6。
3. 同一账号下角色名称不能重复。
4. 创建成功后返回选角页面并刷新列表。

#### 创建智能体页面 `CreateAgentView.vue`

1. 支持输入智能体名称。
2. 支持选择能力模块：对话能力必选，单词考核能力和学习计划能力可选。
3. 支持选择智能体头像。
4. 创建成功后进入对话页面。

#### 对话页面 `ChatView.vue`

1. 左侧显示智能体列表。
2. 右侧显示对话区域。
3. 支持文字输入和发送。
4. 支持单词考核模式切换：音标、拼写、使用。
5. 显示单词学习进度。
6. 自动根据记忆曲线推送复习单词。
7. WebSocket 断线后自动重连。

### 3.3 WebSocket 前端封装

`useWebSocket` 组合式函数要求：

1. 自动连接和重连，使用指数退避策略。
2. 每 30 秒发送一次心跳。
3. 支持离线消息发送队列。
4. 支持消息接收分发。
5. 支持连接状态监听：`onOpen`、`onClose`、`onError`。
6. 支持二进制消息编码和解码。

## 四、开发要求

### 4.1 后端开发要求

代码规范：

- 遵循阿里巴巴 Java 开发手册。
- 关键类和复杂方法添加 Javadoc。
- 异常处理完整，避免吞异常。

性能要求：

- 支持 1000+ 并发 WebSocket 连接。
- 普通消息处理延迟小于 100ms。
- 数据库查询使用索引。

安全要求：

- 服务端使用 BCrypt 或 PBKDF2 存储密码哈希。
- 不在数据库中保存明文密码。
- 生产环境使用 WSS。
- 登录后所有业务消息都要校验 token 和 UID 关系。
- 防 SQL 注入，必须使用 PreparedStatement 或 ORM 参数绑定。
- WebSocket 消息需要包含基础防重放机制，例如 token、时间戳或 nonce。

日志要求：

- 使用 SLF4J + Log4j2。
- 记录登录、创角、选角、删除角色、考核等关键操作。
- 异常堆栈完整记录。

### 4.2 前端开发要求

代码规范：

- ESLint + Prettier。
- TypeScript 严格模式。
- 组件化开发。

UI/UX 要求：

- 响应式设计，适配 PC 和移动端。
- 所有异步操作需要加载状态和错误提示。
- 对话滚动体验要顺滑。

性能要求：

- 首屏加载小于 2 秒。
- 消息发送交互延迟小于 50ms。
- 对话列表过长时使用虚拟滚动。

## 五、数据库初始化和数据导入

启动时执行：

1. 确保 `data/working/ai_data_1.db` 和 `data/english_config.db` 所在目录存在。
2. 开启 SQLite WAL 模式。
3. 自动检测业务表是否存在，不存在则创建。
4. MySQL 导出文件中的 `DROP TABLE`、`CREATE TABLE`、`SET NAMES`、`ENGINE`、`COLLATE`、反引号等 MySQL 专有语法不能直接交给 SQLite 执行，需要转换或只解析 `INSERT INTO` 数据。
5. 初始化 `MaxIdMgr` 中业务表的最大 ID。

## 六、启动和部署

### 6.1 后端启动

```bash
mvn clean package
java -jar target/ai-teacher-1.0.0.jar
```

或直接运行启动类：

```bash
mvn exec:java -Dexec.mainClass="com.dadaotiantian.memorize.Launcher"
```

### 6.2 前端启动

```bash
cd client
npm install
npm run dev
npm run build
npm run preview
```

### 6.3 配置文件位置

```text
项目根目录/
├── config/
│   └── server_config.json
├── data/
│   ├── working/
│   │   └── ai_data_1.db
│   └── english_config.db
├── logs/
│   └── app.log
├── sql/
│   ├── t_s_english_words.sql
│   └── t_s_english_word_definitions.sql
└── client/
```

## 七、测试要点

功能测试：

- 账号注册和登录。
- 登录后查询角色列表。
- 创建角色，最多 6 个。
- 同一账号下角色名不能重复。
- 选择角色后 UID 生效。
- 创建智能体。
- 智能体对话。
- 三种单词考核模式。
- SM-2 复习算法正确性。
- WebSocket 断线重连。

性能测试：

- 并发连接数测试，目标 1000+。
- 消息吞吐量测试。
- 数据库查询性能。
- 内存泄漏测试。

兼容性测试：

- Chrome 最新版。
- Firefox 最新版。
- Safari 最新版。
- Edge 最新版。
- 移动端浏览器。

## 八、交付清单

后端：

- 所有 Java 源代码。
- Maven 配置文件 `pom.xml`。
- `server_config.json` 示例。
- SQLite 初始化和导入代码。
- README.md，包含启动说明。

前端：

- 所有 Vue 组件和 TypeScript 代码。
- `package.json` 和依赖配置。
- `vite.config.ts` 配置。
- 环境变量配置示例。

文档：

- API 消息协议文档。
- 部署运维说明。
- 用户使用说明。

## 九、特别注意事项

1. 登录必须使用 WebSocket，不能用 HTTP API 替代。
2. 消息 ID 必须全局唯一。
3. 登录成功只代表账号登录成功，选角成功后才有 UID。
4. UID 是角色 ID，不是账号 ID。
5. 每个账号最多 6 个角色，前后端都要校验，后端必须强校验。
6. SQLite 不支持在 `CREATE TABLE` 内写 MySQL 风格 `INDEX`。
7. 现有 `sql/*.sql` 是 MySQL 导出文件，导入 SQLite 前必须转换。
8. 静态单词表使用原 SQL 文件中的 ID，不要重新生成 ID。
9. `MaxIdMgr` 只管理程序运行中创建的业务 ID。
10. SQLite 多线程环境需要正确配置 WAL、busy timeout 和连接访问策略。
11. Spring AI Alibaba、Netty、Vue 等依赖版本应选择兼容稳定版本，避免依赖冲突。

## 十、参考说明

- `AgentObject`：参考原项目的智能体架构设计。
- `BasePlayer`：参考模块化管理实现。
- `MaxIdMgr`：参考 ID 生成和管理。
- `AbstractActionQueue`：参考消息队列实现。
- `ddtt` 目录：作为前端 UI 风格参考。
