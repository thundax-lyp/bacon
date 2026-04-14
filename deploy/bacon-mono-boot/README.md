# Mono Deploy README

本文档用途：人工操作说明。  
本文档给人读，不作为实现约束最高依据。  
部署边界以 `docs/00-governance/DEPLOYMENT-AND-TRAFFIC-BOUNDARY-RULES.md` 为准。

## Purpose

本目录提供 `bacon-mono-boot` 的 Docker Compose 部署样例。

## Topology

包含服务：

- `nginx`
- `bacon-mono-boot`
- `mysql`
- `redis`
- `minio`
- `rocketmq-nameserver`
- `rocketmq-broker`

流量路径：

- 外部业务流量：`client -> nginx -> bacon-mono-boot`
- 外部访问 `/api/providers/**`：`nginx` 直接返回 `403`

## Build

```bash
mvn -q -pl bacon-app/bacon-mono-boot -am -DskipTests package
```

产物路径：

- `bacon-app/bacon-mono-boot/target/bacon-mono-boot-0.0.1-SNAPSHOT.jar`

## Start

复制环境变量样例：

```bash
cp deploy/bacon-mono-boot/.env.example deploy/bacon-mono-boot/.env
```

启动：

```bash
docker compose --env-file deploy/bacon-mono-boot/.env -f deploy/bacon-mono-boot/docker-compose.yml up -d
```

停止：

```bash
docker compose --env-file deploy/bacon-mono-boot/.env -f deploy/bacon-mono-boot/docker-compose.yml down
```

## Access

- 业务入口：`http://127.0.0.1:18080`
- MinIO API：`http://127.0.0.1:19000`
- MinIO Console：`http://127.0.0.1:19001`

## Environment Variables

关键变量：

- `BACON_MONO_JAR`
- `BACON_DB_URL`
- `BACON_DB_USERNAME`
- `BACON_DB_PASSWORD`
- `BACON_REDIS_URL`
- `BACON_ROCKETMQ_NAME_SERVER`
- `BACON_STORAGE_OSS_ENDPOINT`
- `BACON_STORAGE_OSS_BUCKET_NAME`
- `BACON_INTERNAL_API_TOKEN`

## Data Initialization

当前样例不自动导入数据库脚本。首次启动后，按顺序导入仓库脚本：

```bash
cat db/schema/*.sql | docker compose --env-file deploy/bacon-mono-boot/.env -f deploy/bacon-mono-boot/docker-compose.yml exec -T mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> bacon_sample
cat db/data/*.sql | docker compose --env-file deploy/bacon-mono-boot/.env -f deploy/bacon-mono-boot/docker-compose.yml exec -T mysql mysql -uroot -p<MYSQL_ROOT_PASSWORD> bacon_sample
```

如果使用外部数据库，直接调整 `.env` 中的 `BACON_DB_*` 即可。

## Verification

健康检查：

```bash
curl http://127.0.0.1:18080/actuator/health
```

确认 `provider` 路径被拦截：

```bash
curl -i http://127.0.0.1:18080/api/providers/auth/tokens/verify
```

期望返回 `403`。

## Troubleshooting

- `bacon-mono-boot` 起不来，先检查 `BACON_MONO_JAR` 是否指向已打包产物
- 登录或业务接口报库表不存在，先导入 `db/schema/*.sql` 与 `db/data/*.sql`
- 上传对象失败，先检查 `MinIO` endpoint、bucket、access key、secret key
- MQ 启动失败，先看 `rocketmq-broker` 日志是否成功连到 `rocketmq-nameserver`
