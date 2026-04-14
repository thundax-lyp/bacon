# DEPLOYMENT AND TRAFFIC BOUNDARY RULES

## 1. Purpose

本文档定义部署样例、流量边界、路径约束、组件职责。  
本文档是架构约束，优先给 AI 和实现者读取。  
本文档不是操作手册，不记录详细部署步骤。

## 2. Scope

覆盖范围：

- `mono` 部署样例
- `micro` 部署样例
- `nginx`
- `gateway`
- `register`
- `ops nginx`
- 公开接口
- `provider.controller` 内部接口
- `actuator` 观测接口

不在范围内：

- 业务功能需求
- 数据库字段设计
- 详细操作步骤
- 长篇背景说明

## 3. Global Constraints

- `deploy/` 固定维护 `mono` 与 `micro` 两套 Docker Compose 样例。
- 两套样例都必须包含应用进程与基础设施进程。
- 基础设施固定包含 `MySQL`、`Redis`、`MinIO`、`RocketMQ`。
- `micro` 样例固定包含 `gateway` 与 `register` 运行时。
- 部署配置以环境变量为主。
- 打包产物固定为各启动模块的可执行 jar。
- `README` 只负责人工操作说明，不得重新定义与本文档冲突的规则。

## 4. Deployment Modes

### 4.1 Mono

- `mono` 固定由 `nginx` 与 `bacon-mono-boot` 组成业务入口链路。
- `bacon-mono-boot` 承载全部业务域能力。
- `mono` 对外只暴露 `nginx`。

### 4.2 Micro

- `micro` 固定由 `nginx`、`gateway`、各业务服务组成业务入口链路。
- `micro` 固定包含 `ops nginx` 作为内部观测入口。
- `micro` 对外业务入口只允许经过 `nginx -> gateway`。
- `gateway` 不得直接作为公网入口。

## 5. External Entry Rule

- 公网唯一业务入口固定为 `nginx`。
- `micro` 下 `gateway` 只接收来自 `nginx` 的业务流量。
- 业务服务不得直接暴露公网业务端口。
- `register` 不得作为公网业务入口。

## 6. Provider Route Rule

- `provider.controller` 固定表示内部 `Facade` HTTP 入口。
- `provider.controller` 路径固定使用 `/providers/{domain}/**`。
- 当服务设置 `server.servlet.context-path=/api` 时，完整路径固定为 `/api/providers/{domain}/**`。
- `interfaces.controller` 不得使用 `/providers/**` 前缀。
- 对外控制器不得复用 `provider.controller` 的路径前缀。

## 7. Provider Access Rule

- `micro` 下 `provider` 调用固定为服务间直连。
- `provider` 请求不得经过 `gateway`。
- `provider` 接口必须校验 `X-Bacon-Provider-Token`。
- `provider` 接口不得作为前端或第三方接入入口。

## 8. Nginx Blocking Rule

- `nginx` 对所有来自外部的 `/api/providers/**` 请求固定返回 `403`。
- `nginx` 不得把 `/api/providers/**` 转发到 `gateway`。
- `mono` 与 `micro` 都适用该拦截规则。

## 9. Gateway Rule

- `gateway` 只代理公开业务流量。
- `gateway` 不代理 `provider` 流量。
- `gateway` 不承担内部 `Facade` 转发职责。
- `gateway` 路由必须显式面向公开业务路径配置。

## 10. Observability Rule

- 观测入口与业务入口固定分离。
- `ops nginx` 只用于内部观测，不作为业务入口。
- `ops nginx` 只代理 `actuator` 相关只读接口。
- 业务服务默认不对公网开放 `actuator`。

## 11. Port Exposure Rule

- `mono` 默认只暴露 `nginx`。
- `micro` 默认只暴露 `nginx`。
- `ops nginx` 只允许本机或内网访问。
- `gateway`、业务服务、`MySQL`、`Redis`、`RocketMQ` 默认不映射公网端口。
- `MinIO Console`、`register` 控制台如需暴露，必须在部署样例中明确标注为调试用途。

## 12. Nacos Rule

- `micro` 的注册发现固定使用 `Nacos Discovery`。
- `micro` 首版不强制启用 `Nacos Config`。
- `gateway` 与各业务服务必须使用统一的 `Nacos server-addr`。
- 使用服务名远程调用时，服务名必须与 `spring.application.name` 一致。

## 13. Open Items

- `bacon-register` 启动模块与 `Nacos Server` 运行时的最终收敛方式待后续实现统一。当前部署样例以 `Nacos Server` 运行时为准。
