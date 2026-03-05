# Hub 国际跳棋 — 迁移指南

> 从 Java Swing 桌面应用迁移至 Spring Boot + Vue 3 Web 应用

---

## 目录

1. [项目概述](#1-项目概述)
2. [技术栈对比](#2-技术栈对比)
3. [项目结构](#3-项目结构)
4. [后端迁移详解](#4-后端迁移详解)
5. [前端迁移详解](#5-前端迁移详解)
6. [快速启动](#6-快速启动)
7. [后续改进建议](#7-后续改进建议)

---

## 1. 项目概述

### 1.1 原始项目

Hub 是一款基于 Java Swing 的 10×10 国际跳棋（International Draughts）桌面应用程序。它集成了 Scan 引擎用于 AI 对弈，通过 `hub.ini` 配置文件管理游戏参数，使用 AWT/Swing 绘制棋盘界面，并支持拖拽走棋、音效播放、PDN 棋谱导入导出等功能。

### 1.2 新架构

迁移后的项目采用前后端分离架构：

- **后端**：Spring Boot 3.2 REST API，负责游戏逻辑与状态管理
- **前端**：Vue 3 单页应用（SPA），负责棋盘渲染与用户交互

### 1.3 架构图

```
┌─────────────────────────────────────────────────────┐
│                    用户浏览器                         │
│  ┌───────────────────────────────────────────────┐   │
│  │              Vue 3 前端 (SPA)                  │   │
│  │  ┌─────────────────┐  ┌─────────────────────┐ │   │
│  │  │ DraughtsBoard   │  │  GameControls       │ │   │
│  │  │  棋盘渲染/交互   │  │  控制面板/时钟       │ │   │
│  │  └────────┬────────┘  └──────────┬──────────┘ │   │
│  │           │     gameApi.js       │            │   │
│  │           └──────────┬───────────┘            │   │
│  └──────────────────────┼────────────────────────┘   │
│                         │ HTTP (fetch)               │
└─────────────────────────┼───────────────────────────┘
                          │ REST API (/api/game/*)
┌─────────────────────────┼───────────────────────────┐
│               Spring Boot 后端                       │
│  ┌──────────────────────┴──────────────────────┐     │
│  │            GameController                   │     │
│  │     REST 控制器 — 接收/返回 JSON            │     │
│  └──────────────────────┬──────────────────────┘     │
│  ┌──────────────────────┴──────────────────────┐     │
│  │             GameService                     │     │
│  │       业务逻辑 — 管理游戏状态               │     │
│  └──────────────────────┬──────────────────────┘     │
│  ┌──────────────────────┴──────────────────────┐     │
│  │          com.hub.draughts 包                │     │
│  │   Game / Pos / Gen / Move / Square / FEN    │     │
│  │          核心跳棋规则引擎                    │     │
│  └─────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────┘
```

---

## 2. 技术栈对比

| 维度 | 原始版本（Swing 桌面应用） | 新版本（Web 应用） |
|---|---|---|
| **语言** | Java | Java 17（后端）+ JavaScript（前端） |
| **UI 框架** | Swing / AWT (`JFrame`, `JPanel`) | Vue 3（Composition API） |
| **后端框架** | 无，直接 Java `main()` 启动 | Spring Boot 3.2 |
| **构建工具** | Shell 脚本 (`make.sh` / `clean.sh`) | Maven（后端）+ Vite（前端） |
| **API 通信** | 无（桌面进程内直接调用） | REST API（JSON over HTTP） |
| **状态管理** | 静态变量 (`Hub.vars`, `Hub.gui`) | Spring Service（后端）+ Vue `ref` / `reactive`（前端） |
| **配置管理** | 自定义 `Var_List` 解析 `hub.ini` | `application.properties` + `@ConfigurationProperties` |
| **引擎通信** | 子进程 + 管道 (`Engine_Hub`) | 待集成（规划中） |
| **部署方式** | `java -jar hub.jar` 单机运行 | 后端 `mvn spring-boot:run` + 前端 `npm run dev` |

---

## 3. 项目结构

```
hub/
├── backend/                        # Spring Boot 后端
│   ├── pom.xml                     # Maven 构建配置
│   └── src/main/
│       ├── java/com/hub/
│       │   ├── HubApplication.java         # Spring Boot 启动类
│       │   ├── config/
│       │   │   ├── GameConfig.java          # 游戏配置（@ConfigurationProperties）
│       │   │   └── WebConfig.java           # CORS 跨域配置
│       │   ├── controller/
│       │   │   └── GameController.java      # REST 控制器（7 个端点）
│       │   ├── dto/
│       │   │   ├── BoardDTO.java            # 棋盘状态数据传输对象
│       │   │   ├── GameStateDTO.java        # 完整游戏状态
│       │   │   ├── LegalMoveDTO.java        # 合法走法
│       │   │   ├── MoveRequestDTO.java      # 走棋请求
│       │   │   └── MoveResponseDTO.java     # 走棋响应
│       │   ├── draughts/                    # 迁移后的跳棋核心逻辑
│       │   │   ├── Draughts.java
│       │   │   ├── Game.java
│       │   │   ├── Gen.java
│       │   │   ├── Move.java
│       │   │   ├── Pos.java
│       │   │   ├── Square.java
│       │   │   ├── FEN.java
│       │   │   ├── Bit.java
│       │   │   ├── Side.java
│       │   │   ├── Piece.java
│       │   │   ├── Piece_Side.java
│       │   │   ├── Node.java
│       │   │   ├── List.java
│       │   │   ├── Bad_Input.java
│       │   │   └── NumberScanner.java
│       │   └── service/
│       │       └── GameService.java         # 游戏业务逻辑服务
│       └── resources/
│           └── application.properties       # Spring Boot 配置文件
│
├── frontend/                       # Vue 3 前端
│   ├── package.json                # Node.js 依赖配置
│   ├── vite.config.js              # Vite 构建与代理配置
│   ├── index.html                  # HTML 入口
│   └── src/
│       ├── main.js                 # Vue 应用入口
│       ├── App.vue                 # 根组件（状态管理中心）
│       ├── api/
│       │   └── gameApi.js          # 后端 API 调用封装
│       ├── assets/
│       │   └── main.css            # 全局样式
│       └── components/
│           ├── DraughtsBoard.vue   # 棋盘组件
│           └── GameControls.vue    # 控制面板组件
│
├── src/                            # 原始 Java Swing 代码（保留参考）
│   ├── draughts/                   # 原始跳棋逻辑
│   ├── engine/                     # Scan 引擎通信
│   ├── gui/                        # Swing GUI
│   ├── hub/                        # 主程序与用户模型
│   ├── io/                         # I/O 工具
│   └── util/                       # 通用工具类
│
├── hub.ini                         # 原始配置文件
├── engine.ini                      # 引擎参数配置
└── readme.txt                      # 项目说明
```

---

## 4. 后端迁移详解

### 4.1 游戏逻辑迁移

原始跳棋逻辑位于 `src/draughts/` 包，迁移至 `backend/src/main/java/com/hub/draughts/`。

#### 关键改动

| 类 | 变更说明 |
|---|---|
| `Draughts.java` | `init()` 方法新增 `String variant` 参数，不再依赖 `Hub.vars` 全局变量 |
| `Square.java` | `init(String variant)` 替代了原来的 `init()`，通过参数判断是否为 Frisian 变体，移除了 `import hub.*` |
| `Game.java` | 移除了对 `Hub.log` 的所有依赖，保持纯逻辑 |
| `NumberScanner.java` | 新增工具类，替代原始代码中对 `java.util.Scanner` 的使用 |

**原始代码（依赖全局静态变量）：**

```java
// src/draughts/Square.java（原始）
import hub.*;

static void init() {
    dir_size = (Hub.vars.get("game-variant").equals("frisian")) ? 8 : 4;
}
```

**迁移后代码（参数传递）：**

```java
// backend/.../com/hub/draughts/Square.java（迁移后）
static void init(String variant) {
    dir_size = (variant.equals("frisian")) ? 8 : 4;
}
```

### 4.2 配置管理

#### 原始方式

使用自定义的 `Var_List` 类解析 `hub.ini` 文件：

```ini
# hub.ini
game-moves = 75
game-time = 3
game-inc = 0
game-ponder = false
```

```java
// 原始调用方式
int moves = Hub.vars.get_int("game-moves");
double time = Hub.vars.get_real("game-time");
```

#### 迁移后方式

使用 Spring Boot 的 `application.properties` + `@ConfigurationProperties`：

```properties
# application.properties
server.port=8080
hub.game.variant=normal
hub.game.moves=0
hub.game.time=5.0
hub.game.inc=0.0
```

```java
@Configuration
@ConfigurationProperties(prefix = "hub.game")
public class GameConfig {
    private String variant = "normal";
    private int moves = 0;
    private double time = 5.0;
    private double inc = 0.0;
    // getter / setter ...
}
```

### 4.3 REST API 设计

所有端点均以 `/api/game` 为前缀，由 `GameController` 统一管理。

#### 端点一览

| 方法 | 路径 | 说明 | 请求体 | 响应体 |
|---|---|---|---|---|
| `GET` | `/api/game/state` | 获取当前游戏状态 | 无 | `GameStateDTO` |
| `POST` | `/api/game/new` | 开始新游戏 | 无 | `GameStateDTO` |
| `POST` | `/api/game/move` | 执行走棋 | `MoveRequestDTO` | `MoveResponseDTO` |
| `POST` | `/api/game/undo` | 悔棋 | 无 | `GameStateDTO` |
| `POST` | `/api/game/redo` | 重做 | 无 | `GameStateDTO` |
| `POST` | `/api/game/position` | 加载 FEN 局面 | `String`（纯文本） | `GameStateDTO` |
| `GET` | `/api/game/moves` | 获取当前合法走法列表 | 无 | `List<LegalMoveDTO>` |

#### 请求/响应示例

**获取游戏状态** `GET /api/game/state`

```json
{
  "board": {
    "board": [
      [-1, 2, -1, 2, -1, 2, -1, 2, -1, 2],
      [2, -1, 2, -1, 2, -1, 2, -1, 2, -1],
      [-1, 2, -1, 2, -1, 2, -1, 2, -1, 2],
      [2, -1, 2, -1, 2, -1, 2, -1, 2, -1],
      [-1, 0, -1, 0, -1, 0, -1, 0, -1, 0],
      [0, -1, 0, -1, 0, -1, 0, -1, 0, -1],
      [-1, 1, -1, 1, -1, 1, -1, 1, -1, 1],
      [1, -1, 1, -1, 1, -1, 1, -1, 1, -1],
      [-1, 1, -1, 1, -1, 1, -1, 1, -1, 1],
      [1, -1, 1, -1, 1, -1, 1, -1, 1, -1]
    ],
    "turn": 0,
    "end": false,
    "moveNumber": 1,
    "fen": "W:W31-50:B1-20",
    "highlightedSquares": []
  },
  "legalMoves": [
    { "from": 31, "to": 26, "notation": "31-26" },
    { "from": 31, "to": 27, "notation": "31-27" }
  ],
  "whiteTime": 300.0,
  "blackTime": 300.0
}
```

**棋盘值说明**：`-1` = 亮色格（不可用），`0` = 空暗格，`1` = 白兵，`2` = 黑兵，`3` = 白王，`4` = 黑王

**执行走棋** `POST /api/game/move`

请求：
```json
{ "from": 31, "to": 26 }
```

成功响应：
```json
{
  "success": true,
  "message": "Move played",
  "board": { ... }
}
```

失败响应：
```json
{
  "success": false,
  "message": "Illegal move",
  "board": { ... }
}
```

**加载 FEN 局面** `POST /api/game/position`

请求（`Content-Type: text/plain`）：
```
W:W31-50:B1-20
```

### 4.4 服务层

`GameService` 替代了原始的 `Model_User`，是迁移中最核心的重构。

#### 对比

| 原始 `Model_User` | 新 `GameService` |
|---|---|
| 实现 `Engine_User` 接口，耦合引擎通信 | 纯游戏逻辑服务，无引擎依赖 |
| 调用 `Hub.gui.set_board()` 更新界面 | 返回 DTO，由前端渲染 |
| 调用 `Hub.log()` 记录日志 | 使用 Spring 日志框架（待接入） |
| 通过 `Hub.vars` 读取配置 | 通过 `GameConfig`（依赖注入）获取配置 |
| 使用 `synchronized` + 内部状态机管理回合 | 使用 `synchronized` 保护游戏状态，无状态机 |
| 支持引擎思考、分析模式、ponder | 暂不支持引擎相关功能 |

#### 核心方法映射

```
Model_User.new_game()    →  GameService.newGame()
Model_User.click() / drag()  →  GameService.makeMove()
Model_User.undo()        →  GameService.undoMove()
Model_User.redo()        →  GameService.redoMove()
Model_User.set_pos()     →  GameService.loadPosition()
```

`GameService` 在初始化时通过 `@PostConstruct` 完成跳棋规则引擎的初始化：

```java
@PostConstruct
public void init() {
    Draughts.init(config.getVariant());
    game = new Game();
    game.set_time_control(config.getMoves(), config.getTime() * 60.0, config.getInc());
}
```

---

## 5. 前端迁移详解

### 5.1 棋盘组件 — `DraughtsBoard.vue`

**替代原始组件**：`gui/Panel_Board`（Swing `JPanel`，自定义 `paintComponent` 绘制）

#### 实现方式

| 特性 | 原始 Swing | Vue 3 |
|---|---|---|
| 棋盘绘制 | `Graphics2D` 手动绘制格子、棋子 | CSS Grid 10×10 布局 |
| 棋子渲染 | `fillOval()` / 自定义绘制 | CSS `border-radius: 50%` + 渐变 |
| 王棋标记 | 绘制皇冠图形 | `.piece__crown` CSS 圆环 |
| 走棋交互 | `MouseListener` 拖拽 + 点击 | `@click` 事件，两次点击走棋 |
| 合法走法提示 | 无 | 绿色圆点 (`.legal-dot`) 标识可达格 |
| 高亮上一步 | 自定义 `bit` 标记 | `highlightedSquares` 数组 + CSS 类 |

#### 交互流程

```
用户点击棋子 → selectedSquare 记录起点
     ↓
显示合法目标格（绿色圆点）
     ↓
用户点击目标格 → emit('move', { from, to })
     ↓
App.vue 调用 gameApi.makeMove() → 后端处理 → 返回新状态 → 刷新棋盘
```

#### 棋盘编号

棋盘使用国际跳棋标准编号（1-50），显示在每个暗格左上角，方便对照走法记谱。

### 5.2 控制面板 — `GameControls.vue`

**替代原始组件**：`GUI.java` 中的 `JLabel` 面板 + 键盘快捷键

#### 功能映射

| 功能 | 原始操作方式 | 新操作方式 |
|---|---|---|
| 新游戏 | 键盘快捷键 | 「新游戏」按钮 |
| 悔棋 | 键盘快捷键 | 「悔棋」按钮 |
| 重做 | 键盘快捷键 | 「重做」按钮 |
| 加载局面 | 菜单/命令行 | FEN 输入框 + 「加载」按钮 |
| 时钟显示 | `JLabel` 文本 | `MM:SS` 格式化显示 |
| 当前回合 | 侧边标签 | 白/黑方标签，带颜色标识 |
| 游戏结束 | 状态栏文本 | 🏁 图标 + 红色高亮文字 |

### 5.3 API 通信

#### 封装层 — `gameApi.js`

使用浏览器原生 `fetch` API 与后端通信，所有请求均指向 `/api/game` 前缀：

```javascript
const BASE_URL = '/api/game'

export function fetchGameState() {
  return fetch(`${BASE_URL}/state`).then(handleResponse)
}

export function makeMove(from, to) {
  return fetch(`${BASE_URL}/move`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ from, to })
  }).then(handleResponse)
}

export function newGame() {
  return fetch(`${BASE_URL}/new`, { method: 'POST' }).then(handleResponse)
}

// undoMove(), redoMove(), loadPosition(), fetchLegalMoves() 类似...
```

#### 开发环境代理

`vite.config.js` 配置了开发代理，将 `/api` 请求转发至后端：

```javascript
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

**前端开发服务器**运行在 `http://localhost:5173`，所有 `/api/*` 请求自动转发到 `http://localhost:8080`。

#### 状态管理

`App.vue` 作为状态管理中心，使用 Vue 3 Composition API 的 `ref` 管理响应式状态：

```javascript
const board = ref([])           // 棋盘二维数组
const turn = ref(0)             // 当前回合（0=白方, 1=黑方）
const legalMoves = ref([])      // 当前合法走法列表
const highlightedSquares = ref([])  // 高亮格子
const moveNumber = ref(1)       // 步数
const whiteTime = ref(300)      // 白方剩余时间（秒）
const blackTime = ref(300)      // 黑方剩余时间（秒）
const isEnd = ref(false)        // 游戏是否结束
```

每次 API 调用返回后，统一通过 `applyState()` 函数更新所有状态。

---

## 6. 快速启动

### 环境要求

- Java 17+
- Maven 3.8+
- Node.js 18+
- npm 9+

### 启动步骤

#### 1. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`。

#### 2. 启动前端（新终端窗口）

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器运行在 `http://localhost:5173`。

#### 3. 访问应用

打开浏览器访问 **http://localhost:5173**，即可开始下棋。

### 验证 API

可以使用 `curl` 验证后端是否正常运行：

```bash
# 获取游戏状态
curl http://localhost:8080/api/game/state

# 开始新游戏
curl -X POST http://localhost:8080/api/game/new

# 走棋
curl -X POST http://localhost:8080/api/game/move \
  -H "Content-Type: application/json" \
  -d '{"from": 31, "to": 26}'

# 获取合法走法
curl http://localhost:8080/api/game/moves
```

### 生产构建

```bash
# 构建前端静态资源
cd frontend
npm run build
# 产物位于 frontend/dist/

# 构建后端 JAR
cd backend
mvn clean package
java -jar target/hub-backend-0.0.1-SNAPSHOT.jar
```

---

## 7. 后续改进建议

### 高优先级

| 改进项 | 说明 |
|---|---|
| **WebSocket 实时通信** | 替代轮询，实现引擎思考过程的实时推送（搜索信息、评估分数） |
| **Scan 引擎集成** | 将原始 `Engine_Hub` 子进程通信移植到后端，恢复 AI 对弈功能 |
| **时钟实时更新** | 后端定时器 + WebSocket 推送，或前端本地计时 |

### 中优先级

| 改进项 | 说明 |
|---|---|
| **用户认证** | Spring Security + JWT，支持多用户同时在线 |
| **游戏持久化** | 使用数据库（如 H2/PostgreSQL）保存对局历史和棋谱 |
| **多人对战** | WebSocket 双人实时对弈，支持匹配/房间机制 |
| **PDN 棋谱支持** | 恢复原始 `PDN_Input` / `PDN_Output` 功能，支持导入/导出棋谱 |

### 低优先级

| 改进项 | 说明 |
|---|---|
| **移动端适配** | 响应式设计，触摸屏交互优化 |
| **国际化 (i18n)** | 支持中文、英文、荷兰语等多语言界面 |
| **Docker 部署** | 提供 `Dockerfile` + `docker-compose.yml`，一键启动前后端 |
| **Frisian 变体支持** | 验证并完善 Frisian 跳棋规则在新架构下的兼容性 |
| **音效支持** | 前端集成走棋音效（替代原始 `go_stone.wav` 的 Java Sound 播放） |
| **棋盘外观设置** | 棋子/棋盘主题切换，支持椭圆/圆形棋子风格 |

---

> 本文档基于项目实际代码编写。原始 Swing 代码保留在 `src/` 目录下，供迁移参考。
