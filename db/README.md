# DB README

## 1. Purpose

本文档说明 `./db/` 目录下数据库脚本的用途、目录结构、执行顺序和初始化数据约定。  
目标是让工程师和 AI 能直接定位并执行 `Auth`、`UPMS` 等业务域的数据库脚本。

## 2. Scope

当前范围覆盖：

- `schema` 目录中的建表脚本
- `data` 目录中的初始化数据脚本
- `Auth`、`UPMS` 与 `Storage` 的执行顺序
- 默认管理员与默认 `OAuth2 client` 的初始化说明

当前范围不覆盖：

- 自动迁移工具接入说明
- 本地 `MySQL` 安装说明
- 其他业务域数据库脚本

## 3. Directory Structure

```text
db/
├── README.md
├── schema/
│   ├── storage.sql
│   ├── auth.sql
│   └── upms.sql
└── data/
    ├── storage.sql
    ├── auth.sql
    └── upms.sql
```

固定规则：

- `schema/` 只放建表脚本
- `data/` 只放初始化和测试数据脚本
- 文件名使用业务域名，与文档中的业务域名称保持一致
- 新增业务域脚本时，固定追加到 `schema/<domain>.sql` 与 `data/<domain>.sql`

## 4. File Usage

### 4.1 `schema/upms.sql`

- 用途：创建 `UPMS` 全量表结构
- 覆盖对象：`Tenant`、`User`、`UserIdentity`、`Department`、`Post`、`Role`、`Menu`、`Resource`、关系表、数据权限表、审计表

### 4.2 `data/upms.sql`

- 用途：插入 `UPMS` 初始化和测试数据
- 覆盖对象：默认租户、默认部门、默认岗位、管理员用户、用户身份、超级管理员角色、菜单、资源、授权关系、审计日志

### 4.3 `schema/auth.sql`

- 用途：创建 `Auth` 全量表结构
- 覆盖对象：会话、刷新令牌、`OAuthClient`、授权码、`OAuth2 access token`、`OAuth2 refresh token`、授权同意、审计表

### 4.4 `data/auth.sql`

- 用途：插入 `Auth` 初始化和测试数据
- 覆盖对象：默认后台 `OAuth2 client`、管理员授权同意记录、审计日志

### 4.5 `schema/storage.sql`

- 用途：创建 `Storage` 全量表结构
- 覆盖对象：`StoredObject`、`StoredObjectReference`、分段上传会话、分段上传分片、存储审计日志、审计补偿出站表

### 4.6 `data/storage.sql`

- 用途：预留 `Storage` 初始化和测试数据
- 当前约定：暂无默认种子数据，脚本保留为空模板

## 5. Execution Order

固定执行顺序：

1. `db/schema/upms.sql`
2. `db/data/upms.sql`
3. `db/schema/auth.sql`
4. `db/data/auth.sql`
5. `db/schema/storage.sql`
6. `db/data/storage.sql`

固定原因：

- `Auth` 的测试数据依赖 `UPMS` 中已存在的 `tenant_id`、`user_id`、`identity_id`
- 管理员授权同意与审计记录必须引用已存在的用户和身份
- `Storage` 当前没有默认种子依赖，可在 `Auth`/`UPMS` 完成后独立执行

## 6. Seed Data Rules

- 初始化数据使用固定主键和固定业务键
- `UserIdentityId` 与所有 `identity_id` 引用固定使用字符串值，不使用数据库自增数值
- 初始化脚本统一使用 `ON DUPLICATE KEY UPDATE`
- 重复执行脚本时，目标是收敛到固定数据状态
- 初始化数据中的密码和客户端密钥只提供明文说明，不在表中保存明文
- 表中实际保存值固定为哈希结果

## 7. Default Accounts

### 7.1 Default Tenant

- `tenant_id`: `T1000001`
- `code`: `BACON`
- `name`: `Bacon 默认租户`

### 7.2 Default Administrator

- `account`: `admin`
- `password`: `Admin@123456`
- `user_id`: `'2000001'`
- `tenant_id`: `T1000001`
- `role_code`: `SUPER_ADMIN`

### 7.3 Default OAuth2 Client

- `client_id`: `bacon-admin-web`
- `client_secret`: `BaconClient@123`
- `client_type`: `CONFIDENTIAL`

## 8. Example Usage

示例命令：

```bash
mysql -u root -p < db/schema/upms.sql
mysql -u root -p < db/data/upms.sql
mysql -u root -p < db/schema/auth.sql
mysql -u root -p < db/data/auth.sql
mysql -u root -p < db/schema/storage.sql
mysql -u root -p < db/data/storage.sql
```

如果需要指定数据库：

```bash
mysql -u root -p bacon < db/schema/upms.sql
mysql -u root -p bacon < db/data/upms.sql
mysql -u root -p bacon < db/schema/auth.sql
mysql -u root -p bacon < db/data/auth.sql
mysql -u root -p bacon < db/schema/storage.sql
mysql -u root -p bacon < db/data/storage.sql
```

## 9. Maintenance Rules

- 修改表结构时，先同步 `docs/*-DATABASE-DESIGN.md`，再修改 `schema/*.sql`
- 修改固定业务初始化数据时，先同步对应 `docs/*-REQUIREMENTS.md`，再修改 `data/*.sql`
- 如果后续接入 `Flyway` 或 `Liquibase`，本目录作为迁移来源基线，不直接删除已有脚本

## 10. Open Items

无
