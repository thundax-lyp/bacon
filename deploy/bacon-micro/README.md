# Micro Deploy README

本文档用途：人工操作说明。  
本文档给人读，不作为实现约束最高依据。  
部署边界以 `docs/00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md` 为准。

## Purpose

本目录提供 `micro` 模式的 Docker Compose 样例，覆盖：

- `Nginx` 公网入口
- `Gateway`
- `Nacos Discovery`
- 内部 `provider` 直连
- `ops nginx` 观测入口

## Topology

包含服务：

- `nginx`
- `ops-nginx`
- `bacon-register`，当前以 `Nacos Server` 运行时提供注册发现
- `bacon-gateway`
- `bacon-auth-service`
- `bacon-upms-service`
- `bacon-order-service`
- `bacon-inventory-service`
- `bacon-payment-service`
- `bacon-storage-service`
- `mysql`
- `redis`
- `minio`
- `rocketmq-nameserver`
- `rocketmq-broker`

流量路径：

- 外部业务流量：`client -> nginx -> gateway -> public controller`
- 内部 `provider` 流量：`service -> service`
- `/api/providers/**` 外部访问：`nginx` 直接返回 `403`
- 观测流量：`ops-nginx -> 各服务 actuator`

## Build

```bash
mvn -q -pl bacon-app/bacon-gateway,bacon-app/bacon-auth-starter,bacon-app/bacon-upms-starter,bacon-app/bacon-order-starter,bacon-app/bacon-inventory-starter,bacon-app/bacon-payment-starter,bacon-app/bacon-storage-starter -am -DskipTests package
```

## Start

复制环境变量样例：

```bash
cp deploy/bacon-micro/.env.example deploy/bacon-micro/.env
```

启动：

```bash
docker compose --env-file deploy/bacon-micro/.env -f deploy/bacon-micro/docker-compose.yml up -d
```

停止：

```bash
docker compose --env-file deploy/bacon-micro/.env -f deploy/bacon-micro/docker-compose.yml down
```

## Access

- 公网入口：`http://127.0.0.1:18080`
- 本机观测入口：`http://127.0.0.1:19080`
- Nacos Console：`http://127.0.0.1:18848/nacos`
- MinIO API：`http://127.0.0.1:19000`
- MinIO Console：`http://127.0.0.1:19001`

## Internal Rules

- `provider.controller` 统一走 `/api/providers/{domain}/**`
- `provider` 只允许服务间直连
- `gateway` 不承载 `provider` 转发
- 外部访问 `/api/providers/**` 会被 `nginx` 拦截成 `403`
- 内部 `provider` 请求必须带 `X-Bacon-Provider-Token`

## Environment Variables

关键变量：

- `BACON_NACOS_SERVER_ADDR`
- `BACON_AUTH_PROVIDER_TOKEN`
- `BACON_UPMS_PROVIDER_TOKEN`
- `BACON_ORDER_PROVIDER_TOKEN`
- `BACON_INVENTORY_PROVIDER_TOKEN`
- `BACON_PAYMENT_PROVIDER_TOKEN`
- `BACON_STORAGE_PROVIDER_TOKEN`
- `BACON_DB_URL`
- `BACON_REDIS_URL`
- `BACON_ROCKETMQ_NAME_SERVER`

## Data Initialization

当前样例不自动导入数据库脚本。首次启动后，按顺序导入仓库脚本：

```bash
cat db/schema/*.sql | docker compose --env-file deploy/bacon-micro/.env -f deploy/bacon-micro/docker-compose.yml exec -T mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> bacon_sample
cat db/data/*.sql | docker compose --env-file deploy/bacon-micro/.env -f deploy/bacon-micro/docker-compose.yml exec -T mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> bacon_sample
```

## Verification

查看 `gateway` 健康：

```bash
curl http://127.0.0.1:19080/gateway/actuator/health
```

查看 `auth` 健康：

```bash
curl http://127.0.0.1:19080/auth/actuator/health
```

确认 `provider` 路径被拦截：

```bash
curl -i http://127.0.0.1:18080/api/providers/auth/tokens/verify
```

期望返回 `403`。

## Troubleshooting

- 服务未注册到 Nacos，先看 `BACON_NACOS_SERVER_ADDR` 是否指向 `bacon-register:8848`
- `gateway` 转发失败，先看 `bacon-gateway` 日志和 `ops-nginx` 健康接口
- `provider` 调用报 `401/403`，先核对 `BACON_*_PROVIDER_TOKEN` 与 `BACON_REMOTE_*_PROVIDER_TOKEN`
- `MySQL` 库表缺失，先导入 `db/schema/*.sql` 与 `db/data/*.sql`
