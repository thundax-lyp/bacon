# STORAGE DATABASE DESIGN

## 1. Purpose

本文档定义 `Storage` 域的持久化模型。

本文档只回答三件事：

- 哪些表存在
- 字段如何落库
- 索引和约束如何设计

业务边界和接口契约见 `STORAGE-REQUIREMENTS.md`。

## 2. Global Rules

- 数据库：`MySQL 8.x`
- 引擎：`InnoDB`
- 字符集：`utf8mb4`
- 时间字段：`datetime(3)`
- 运行态流水主键：`bigint`
- 业务主键和值对象字段：`varchar`
- 枚举字段：`varchar`
- 文件内容不入库，只保存元数据和运行态记录

## 3. Table Mapping

| Domain Object | Table |
| --- | --- |
| `StoredObject` | `bacon_storage_object` |
| `StoredObjectReference` | `bacon_storage_object_reference` |
| `StorageAuditLog` | `bacon_storage_audit_log` |
| `StorageAuditOutbox` | `bacon_storage_audit_outbox` |
| `MultipartUploadSession` | `bacon_storage_multipart_upload` |
| `MultipartUploadPart` | `bacon_storage_multipart_upload_part` |

## 4. Stable Enum Columns

- `storage_type`: `LOCAL_FILE`, `OSS`
- `object_status`: `ACTIVE`, `DELETING`, `DELETED`
- `reference_status`: `UNREFERENCED`, `REFERENCED`
- `upload_status`: `INITIATED`, `UPLOADING`, `COMPLETED`, `ABORTED`
- `action_type`: `UPLOAD`, `REFERENCE_ADD`, `REFERENCE_CLEAR`, `DELETE`
- `status`: `NEW`, `PROCESSING`, `RETRYING`, `DEAD`

## 5. Table Design

### 5.1 `bacon_storage_object`

用途：

- 持久化统一存储对象主数据

字段：

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| `id` | `varchar(64)` | N | 存储对象业务主键，对应 `StoredObjectId.value` |
| `tenant_id` | `varchar(64)` | Y | 租户业务键，对应 `TenantId.value` |
| `storage_type` | `varchar(32)` | N | 底层存储类型，对应 `StorageType` |
| `bucket_name` | `varchar(128)` | Y | 存储桶或本地逻辑目录 |
| `object_key` | `varchar(512)` | N | 底层对象键，全局唯一 |
| `original_filename` | `varchar(255)` | N | 原始文件名 |
| `content_type` | `varchar(128)` | N | 文件内容类型 |
| `size` | `bigint` | N | 文件大小，字节 |
| `access_endpoint` | `varchar(1024)` | N | 当前访问端点 |
| `object_status` | `varchar(32)` | N | 对应 `StoredObjectStatus` |
| `reference_status` | `varchar(32)` | N | 对应 `StoredObjectReferenceStatus` |
| `created_by` | `varchar(64)` | Y | 创建人 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

约束和索引：

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_object_key (object_key)`
- `KEY idx_tenant_status (tenant_id, object_status, reference_status)`

### 5.2 `bacon_storage_object_reference`

用途：

- 持久化对象与业务对象的引用关系

字段：

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| `object_id` | `varchar(64)` | N | 存储对象业务主键 |
| `owner_type` | `varchar(64)` | N | 引用方类型 |
| `owner_id` | `varchar(64)` | N | 引用方业务主键 |

约束和索引：

- `PRIMARY KEY (object_id, owner_type, owner_id)`
- `KEY idx_owner (owner_type, owner_id)`

### 5.3 `bacon_storage_audit_log`

用途：

- 持久化对象关键操作审计日志

字段：

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| `id` | `bigint` | N | 运行态主键 |
| `tenant_id` | `varchar(64)` | Y | 所属租户业务键 |
| `object_id` | `varchar(64)` | Y | 存储对象业务主键 |
| `owner_type` | `varchar(64)` | Y | 引用方类型 |
| `owner_id` | `varchar(64)` | Y | 引用方业务主键 |
| `action_type` | `varchar(64)` | N | 审计动作类型 |
| `before_status` | `varchar(32)` | Y | 变更前状态 |
| `after_status` | `varchar(32)` | Y | 变更后状态 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人主键 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

约束和索引：

- `PRIMARY KEY (id)`
- `KEY idx_tenant_occurred (tenant_id, occurred_at)`
- `KEY idx_object_occurred (object_id, occurred_at)`
- `KEY idx_operator_occurred (operator_id, occurred_at)`

### 5.4 `bacon_storage_audit_outbox`

用途：

- 持久化审计失败后的补偿任务

字段：

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| `id` | `bigint` | N | 运行态主键 |
| `tenant_id` | `varchar(64)` | Y | 所属租户业务键 |
| `object_id` | `varchar(64)` | Y | 存储对象业务主键 |
| `owner_type` | `varchar(64)` | Y | 引用方类型 |
| `owner_id` | `varchar(64)` | Y | 引用方业务主键 |
| `action_type` | `varchar(64)` | N | 审计动作类型 |
| `before_status` | `varchar(32)` | Y | 变更前状态 |
| `after_status` | `varchar(32)` | Y | 变更后状态 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人主键 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |
| `error_message` | `varchar(512)` | Y | 失败原因 |
| `status` | `varchar(32)` | N | 补偿状态 |
| `retry_count` | `int` | N | 重试次数 |
| `next_retry_at` | `datetime(3)` | N | 下次重试时间 |
| `updated_at` | `datetime(3)` | N | 最后更新时间 |

约束和索引：

- `PRIMARY KEY (id)`
- `KEY idx_status_retry (status, next_retry_at)`
- `KEY idx_object_occurred (object_id, occurred_at)`

### 5.5 `bacon_storage_multipart_upload`

用途：

- 持久化分段上传会话

字段：

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| `id` | `bigint` | N | 运行态主键 |
| `upload_id` | `varchar(64)` | N | 分段上传会话业务键 |
| `tenant_id` | `varchar(64)` | Y | 所属租户业务键 |
| `owner_type` | `varchar(64)` | N | 引用方类型 |
| `owner_id` | `varchar(64)` | N | 引用方业务主键 |
| `category` | `varchar(64)` | Y | 对象分类 |
| `original_filename` | `varchar(255)` | N | 原始文件名 |
| `content_type` | `varchar(128)` | N | 文件内容类型 |
| `object_key` | `varchar(512)` | N | 最终对象键 |
| `provider_upload_id` | `varchar(128)` | Y | 底层存储分段会话标识 |
| `total_size` | `bigint` | N | 总文件大小 |
| `part_size` | `bigint` | N | 固定分段大小 |
| `uploaded_part_count` | `int` | N | 已上传分段数 |
| `upload_status` | `varchar(32)` | N | 分段上传状态 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |
| `completed_at` | `datetime(3)` | Y | 完成时间 |
| `aborted_at` | `datetime(3)` | Y | 取消时间 |

约束和索引：

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_upload_id (upload_id)`
- `KEY idx_object_key (object_key)`
- `KEY idx_tenant_status (tenant_id, upload_status, created_at)`

### 5.6 `bacon_storage_multipart_upload_part`

用途：

- 持久化分段上传分片记录

字段：

| Column | Type | Null | Description |
| --- | --- | --- | --- |
| `id` | `bigint` | N | 运行态主键 |
| `upload_id` | `varchar(64)` | N | 分段上传会话业务键 |
| `part_number` | `int` | N | 分段序号 |
| `etag` | `varchar(128)` | N | 分段校验标识 |
| `size` | `bigint` | N | 分段大小 |
| `created_at` | `datetime(3)` | N | 创建时间 |

约束和索引：

- `PRIMARY KEY (id)`
- `UNIQUE KEY uk_upload_part (upload_id, part_number)`
- `KEY idx_upload_id (upload_id)`

## 6. Data File

- `db/schema/storage.sql` 是唯一 `DDL` 来源
- `db/data/storage.sql` 当前不提供默认业务种子数据

## 7. Sync Rule

以下任一变化都必须同步更新 `db/schema/storage.sql`：

- 新增或删除表
- 字段类型变化
- 枚举取值变化
- 索引或唯一约束变化
