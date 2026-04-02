# STORAGE DATABASE DESIGN

## 1. Purpose

本文档定义 `Storage` 业务域的数据库设计。  
本文档可直接用于生成 `DDL`、`DataObject`、`Mapper`、`Repository` 和查询实现。  
本文档只定义 `Storage` 自有的持久化对象、字段、索引、引用关系和查询模型。  
本文档必须遵守 [DATABASE-RULES.md](./DATABASE-RULES.md)。

## 2. Scope

本文档定义以下持久化对象：
- `StoredObject`
- `StoredObjectReference`
- `MultipartUploadSession`
- `MultipartUploadPart`
- `StorageAuditLog`
- `StorageAuditOutbox`

本文档不定义以下持久化对象：
- 业务域主数据表
- 文件内容本身
- 底层存储临时分片物理文件

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库默认值
- 时间字段统一使用 `datetime(3)`
- 运行态自增主键统一使用 `bigint`
- 业务主键和值对象字段按领域建模使用 `varchar`
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
- `object_status`: `ACTIVE`、`DELETING`、`DELETED`
- `reference_status`: `UNREFERENCED`、`REFERENCED`
- `upload_status`: `INITIATED`、`UPLOADING`、`COMPLETED`、`ABORTED`
- `action_type`: `UPLOAD`、`REFERENCE_ADD`、`REFERENCE_CLEAR`、`DELETE`
- `status`: `NEW`、`PROCESSING`、`RETRYING`、`DEAD`
- `owner_type`: 由接入业务域约定并在全局保持稳定

### 5.2 Fixed Length Rules

- `tenant_id`: `varchar(64)`
- `id`: `varchar(64)`，仅适用于 `StoredObject.id`
- `object_id`: `varchar(64)`
- `storage_type`: `varchar(32)`
- `bucket_name`: `varchar(128)`
- `object_key`: `varchar(512)`
- `original_filename`: `varchar(255)`
- `content_type`: `varchar(128)`
- `access_endpoint`: `varchar(1024)`
- `object_status`: `varchar(32)`
- `reference_status`: `varchar(32)`
- `created_by`: `varchar(64)`
- `updated_by`: `varchar(64)`
- `owner_type`: `varchar(64)`
- `owner_id`: `varchar(64)`
- `action_type`: `varchar(64)`
- `operator_type`: `varchar(32)`
- `upload_id`: `varchar(64)`
- `provider_upload_id`: `varchar(128)`
- `category`: `varchar(64)`
- `upload_status`: `varchar(32)`
- `etag`: `varchar(128)`
- `error_message`: `varchar(512)`
- `status`: `varchar(32)`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `StoredObject` | `bacon_storage_object` |
| `StoredObjectReference` | `bacon_storage_object_reference` |
| `MultipartUploadSession` | `bacon_storage_multipart_upload` |
| `MultipartUploadPart` | `bacon_storage_multipart_upload_part` |
| `StorageAuditLog` | `bacon_storage_audit_log` |
| `StorageAuditOutbox` | `bacon_storage_audit_outbox` |

## 7. Table Design

### 7.1 `bacon_storage_object`

表类型：`Master Table`

用途：

- 持久化统一存储对象主数据
- 保存底层存储定位信息和访问地址

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `varchar(64)` | N | 存储对象业务主键，对应 `StoredObjectId.value` |
| `tenant_id` | `varchar(64)` | Y | 所属租户业务键 |
| `storage_type` | `varchar(32)` | N | 底层存储类型，对应 `StorageType` |
| `bucket_name` | `varchar(128)` | Y | 存储桶或本地逻辑目录 |
| `object_key` | `varchar(512)` | N | 底层对象键，全局唯一 |
| `original_filename` | `varchar(255)` | N | 原始文件名 |
| `content_type` | `varchar(128)` | N | 内容类型 |
| `size` | `bigint` | N | 文件大小，字节 |
| `access_endpoint` | `varchar(1024)` | N | 当前访问端点 |
| `object_status` | `varchar(32)` | N | 对象状态 |
| `reference_status` | `varchar(32)` | N | 引用状态 |
| `created_by` | `varchar(64)` | Y | 创建人 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `varchar(64)` | Y | 更新人 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

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
| `object_id` | `varchar(64)` | N | 存储对象业务主键 |
| `owner_type` | `varchar(64)` | N | 引用方类型 |
| `owner_id` | `varchar(64)` | N | 引用方业务主键 |

索引与约束：

- `pk(id)`
- `uk_object_owner(object_id, owner_type, owner_id)`
- `idx_owner(owner_type, owner_id)`

### 7.3 `bacon_storage_audit_log`

表类型：`Audit Log Table`

用途：

- 持久化对象上传、引用变更、删除等关键操作审计摘要
- 支持按租户、对象、操作人、时间范围查询

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | Y | 所属租户业务键 |
| `object_id` | `varchar(64)` | Y | 存储对象业务主键 |
| `owner_type` | `varchar(64)` | Y | 引用方类型 |
| `owner_id` | `varchar(64)` | Y | 引用方业务主键 |
| `action_type` | `varchar(64)` | N | 审计动作类型 |
| `before_status` | `varchar(32)` | Y | 变更前对象状态 |
| `after_status` | `varchar(32)` | Y | 变更后对象状态 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人主键 |
| `occurred_at` | `datetime(3)` | N | 审计发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_occurred(tenant_id, occurred_at)`
- `idx_object_occurred(object_id, occurred_at)`
- `idx_operator_occurred(operator_id, occurred_at)`

### 7.4 `bacon_storage_multipart_upload`

表类型：`Runtime Table`

用途：

- 持久化大文件分段上传会话
- 记录分段上传状态与会话元数据

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `upload_id` | `varchar(64)` | N | 分段上传会话业务键 |
| `tenant_id` | `varchar(64)` | Y | 所属租户业务键 |
| `owner_type` | `varchar(64)` | N | 引用方类型 |
| `owner_id` | `varchar(64)` | N | 引用方业务主键 |
| `category` | `varchar(64)` | Y | 对象分类 |
| `original_filename` | `varchar(255)` | N | 原始文件名 |
| `content_type` | `varchar(128)` | N | 内容类型 |
| `object_key` | `varchar(512)` | N | 本次分段上传最终对象键 |
| `provider_upload_id` | `varchar(128)` | Y | 底层存储提供方分段上传会话标识 |
| `total_size` | `bigint` | N | 总文件大小，字节 |
| `part_size` | `bigint` | N | 固定分段大小，字节 |
| `uploaded_part_count` | `int` | N | 已上传分段数 |
| `upload_status` | `varchar(32)` | N | 分段上传状态 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |
| `completed_at` | `datetime(3)` | Y | 完成时间 |
| `aborted_at` | `datetime(3)` | Y | 取消时间 |

索引与约束：

- `pk(id)`
- `uk_upload_id(upload_id)`
- `idx_object_key(object_key)`
- `idx_tenant_status(tenant_id, upload_status, created_at)`

### 7.5 `bacon_storage_audit_outbox`

表类型：`Runtime Table`

用途：

- 持久化审计写入失败后的补偿事件
- 支持定时重试任务扫描与回补正式审计日志

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `varchar(64)` | Y | 所属租户业务键 |
| `object_id` | `varchar(64)` | Y | 存储对象业务主键 |
| `owner_type` | `varchar(64)` | Y | 引用方类型 |
| `owner_id` | `varchar(64)` | Y | 引用方业务主键 |
| `action_type` | `varchar(64)` | N | 审计动作类型 |
| `before_status` | `varchar(32)` | Y | 变更前状态 |
| `after_status` | `varchar(32)` | Y | 变更后状态 |
| `operator_type` | `varchar(32)` | Y | 操作人类型 |
| `operator_id` | `bigint` | Y | 操作人主键 |
| `occurred_at` | `datetime(3)` | N | 审计发生时间 |
| `error_message` | `varchar(512)` | Y | 最近一次失败原因 |
| `status` | `varchar(32)` | N | 当前补偿状态 |
| `retry_count` | `int` | N | 已重试次数 |
| `next_retry_at` | `datetime(3)` | N | 下次重试时间 |
| `updated_at` | `datetime(3)` | N | 最后更新时间 |

索引与约束：

- `pk(id)`
- `idx_status_retry(status, next_retry_at)`
- `idx_object_occurred(object_id, occurred_at)`

### 7.6 `bacon_storage_multipart_upload_part`

表类型：`Runtime Table`

用途：

- 持久化已上传分段元数据
- 支持分段完整性校验与最终合并

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `upload_id` | `varchar(64)` | N | 分段上传会话业务键 |
| `part_number` | `int` | N | 分段序号 |
| `etag` | `varchar(128)` | N | 分段校验标识 |
| `size` | `bigint` | N | 分段大小，字节 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_upload_part(upload_id, part_number)`
- `idx_upload_id(upload_id)`

## 8. Relationship Rules

- `bacon_storage_object_reference.object_id` 关联 `bacon_storage_object.id`
- `bacon_storage_multipart_upload_part.upload_id` 关联 `bacon_storage_multipart_upload.upload_id`
- 当前设计默认不强制数据库外键
- 业务对象表只保存 `object_id`，不保存 `bucket_name`、`object_key`、`access_endpoint`

## 9. Persistence Rules

- 上传成功后必须同时写 `bacon_storage_object`
- 初始化分段上传后必须写 `bacon_storage_multipart_upload`
- 初始化分段上传后必须同时写入 `owner_type`、`owner_id`、`tenant_id`，供后续分段上传、完成与取消时做归属校验
- 初始化分段上传后必须同时写入 `object_key`，如底层为 `OSS/S3 API` 还必须写入 `provider_upload_id`
- 分段上传成功后必须写 `bacon_storage_multipart_upload_part`
- 分段上传完成后必须写 `bacon_storage_object` 并更新 `bacon_storage_multipart_upload.upload_status`
- 分段上传取消后必须把 `bacon_storage_multipart_upload.upload_status` 更新为 `ABORTED`
- 建立业务引用后必须写 `bacon_storage_object_reference`
- 清理引用后如对象已无任何引用，必须把 `reference_status` 更新为 `UNREFERENCED`
- 删除对象时必须先把 `object_status` 更新为 `DELETING`
- 删除底层对象成功后必须再把 `object_status` 更新为 `DELETED`
- 上传、引用变更、删除等关键操作必须写 `bacon_storage_audit_log`
- 审计写入失败时必须补写 `bacon_storage_audit_outbox`
- 审计补偿任务重试时必须按 `status + next_retry_at` 扫描 `bacon_storage_audit_outbox`
- `DEAD` 状态的 `bacon_storage_audit_outbox` 必须按 `updated_at` 保留期定时清理

## 10. Query Model Rules

- `getObjectById` 固定查询 `bacon_storage_object`
- `existsReference` 固定查询 `bacon_storage_object_reference`
- `getMultipartUploadByUploadId` 固定查询 `bacon_storage_multipart_upload`
- `listMultipartParts` 固定查询 `bacon_storage_multipart_upload_part`
- 按 `(owner_type, owner_id)` 查询时固定使用 `idx_owner`
- 按租户、对象、操作人查询审计日志时固定使用 `bacon_storage_audit_log` 对应索引
