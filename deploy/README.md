# Deploy README

本文档用途：人工操作与部署说明。  
本文档给人读，不作为实现约束最高依据。  
部署边界、路径规则、职责划分以 `docs/00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md` 为准。

## Overview

`deploy/` 提供两套完整部署样例：

- `bacon-mono-boot`：单体模式，适合本地联调、演示环境、单实例部署
- `bacon-micro`：微服务模式，适合验证注册发现、网关边界、内部 `provider` 调用

两套样例都包含：

- 应用 jar 进程
- `MySQL`
- `Redis`
- `MinIO`
- `RocketMQ`

## Before You Start

前置要求：

- JDK 17
- Maven 3.9+
- Docker
- Docker Compose v2

默认约定：

- 宿主机先执行 Maven 打包
- Compose 通过挂载 `target/*.jar` 启动应用
- 大部分配置通过 `.env` 或环境变量覆盖

## Packaging

单体打包：

```bash
mvn -q -pl bacon-app/bacon-mono-boot -am -DskipTests package
```

微服务打包：

```bash
mvn -q -pl bacon-app/bacon-gateway,bacon-app/bacon-auth-starter,bacon-app/bacon-upms-starter,bacon-app/bacon-order-starter,bacon-app/bacon-inventory-starter,bacon-app/bacon-payment-starter,bacon-app/bacon-storage-starter -am -DskipTests package
```

## Deployment Choices

选择 `mono`：

- 需要最快跑通完整业务闭环
- 不需要验证注册发现
- 需要最少进程数

选择 `micro`：

- 需要验证 `gateway`、`Nacos Discovery`、服务间 `provider` 直连
- 需要验证对外与对内路径隔离
- 需要验证观测入口与业务入口分离

## Directory Guide

- `deploy/bacon-mono-boot/`：单体 Compose、`.env` 样例、`nginx` 配置、单体说明
- `deploy/bacon-micro/`：微服务 Compose、`.env` 样例、`nginx` 配置、微服务说明

## Common Notes

- `/api/providers/**` 是内部路径，外部经过 `nginx` 会直接得到 `403`
- `micro` 下内部 `provider` 调用不经过 `gateway`
- 当前 `micro` 部署样例固定使用 `nacos/nacos-server:v3.1.1` 作为注册中心运行时
- `MySQL` 初始化脚本仍以仓库 `db/schema/*.sql` 与 `db/data/*.sql` 为准
