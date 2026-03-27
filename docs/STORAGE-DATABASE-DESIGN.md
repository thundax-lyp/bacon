# STORAGE DATABASE DESIGN

## 1. Purpose

本文档定义 `Storage` 业务域的数据库设计。  
目标是让 AI 和工程师可直接据此生成 `DDL`、`DataObject`、`Mapper`、`Repository` 和查询实现。  
本文档只定义 `Storage` 自有的持久化对象、字段、索引、引用关系和查询模型。  
本文档必须遵守 [DATABASE-RULES.md](./DATABASE-RULES.md)。

## 2. Scope

当前范围覆盖以下持久化对象：

- `StoredObject`
- `StoredObjectReference`

当前范围不建表的对象：

- 业务域主数据表
- 文件内容本身
- 临时上传分片表

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库默认值
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 枚举字段统一使用 `varchar`
- 文件内容固定存放在底层存储，不直接落数据库

## 4. Naming Rules

- 表名前缀固定使用 `bacon_storage_`
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 状态字段统一命名为 `object_status`
- 引用状态字段统一命名为 `reference_status`
- 创建时间统一命名为 `created_at`

## 5. Enum Storage Rules

### 5.1 Fixed Enums

- `storage_type`: `LOCAL_FILE`、`OSS`
- `object_status`: `ACTIVE`、`DELETED`
- `reference_status`: `UNREFERENCED`、`REFERENCED`
- `owner_type`: 至少包含 `UPMS_USER_AVATAR`

### 5.2 Fixed Length Rules

- `tenant_id`: `varchar(64)`
- `storage_type`: `varchar(32)`
- `bucket_name`: `varchar(128)`
- `object_key`: `varchar(512)`
- `original_filename`: `varchar(255)`
- `content_type`: `varchar(128)`
- `access_url`: `varchar(1024)`
- `object_status`: `varchar(32)`
- `reference_status`: `varchar(32)`
- `owner_type`: `varchar(64)`
- `owner_id`: `varchar(64)`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `StoredObject` | `bacon_storage_object` |
| `StoredObjectReference` | `bacon_storage_object_reference` |

## 7. Table Design

### 7.1 `bacon_storage_object`

表类型：`Master Table`

用途：

- 持久化统一存储对象主数据
- 保存底层存储定位信息和访问地址

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | Y | 所属租户业务键 |
| `storage_type` | `varchar(32)` | N | 底层存储类型 |
| `bucket_name` | `varchar(128)` | Y | 存储桶或本地逻辑目录 |
| `object_key` | `varchar(512)` | N | 底层对象键，全局唯一 |
| `original_filename` | `varchar(255)` | N | 原始文件名 |
| `content_type` | `varchar(128)` | N | 内容类型 |
| `size` | `bigint` | N | 文件大小，字节 |
| `access_url` | `varchar(1024)` | N | 当前访问地址 |
| `object_status` | `varchar(32)` | N | 对象状态 |
| `reference_status` | `varchar(32)` | N | 引用状态 |
| `created_by` | `bigint` | Y | 创建人 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_object_key(object_key)`
- `idx_tenant_status(tenant_id, object_status, reference_status)`

### 7.2 `bacon_storage_object_reference`

表类型：`Relation Table`

用途：

- 持久化对象与业务对象的引用关系
- 判断对象是否仍可删除

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `object_id` | `bigint` | N | 存储对象主键 |
| `owner_type` | `varchar(64)` | N | 引用方类型 |
| `owner_id` | `varchar(64)` | N | 引用方业务主键 |
| `created_at` | `datetime(3)` | N | 建立引用时间 |

索引与约束：

- `pk(id)`
- `uk_object_owner(object_id, owner_type, owner_id)`
- `idx_owner(owner_type, owner_id)`

## 8. Relationship Rules

- `bacon_storage_object_reference.object_id` 关联 `bacon_storage_object.id`
- 当前设计默认不强制数据库外键
- 业务对象表只保存 `object_id`，不保存 `bucket_name`、`object_key`、`access_url`

## 9. Persistence Rules

- 上传成功后必须同时写 `bacon_storage_object`
- 建立业务引用后必须写 `bacon_storage_object_reference`
- 清理引用后如对象已无任何引用，必须把 `reference_status` 更新为 `UNREFERENCED`
- 删除对象时必须先删除底层对象，再把 `object_status` 更新为 `DELETED`

## 10. Query Model Rules

- `getObjectById` 固定查询 `bacon_storage_object`
- `existsReference` 固定查询 `bacon_storage_object_reference`
- 按 `(owner_type, owner_id)` 查询时固定使用 `idx_owner`

## 11. Open Items

无
