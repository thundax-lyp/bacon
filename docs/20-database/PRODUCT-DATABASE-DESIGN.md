# PRODUCT DATABASE DESIGN

## 1. Purpose

本文档定义 `Product` 业务域的 MySQL 数据库设计。  
本文档可直接用于生成 `DDL`、`DataObject`、`Mapper`、`Repository`、商品查询和商品命令持久化实现。  
本文档只定义 `Product` 自有的持久化对象、字段、索引、幂等记录和 outbox，不定义 Elasticsearch 索引结构。  
Elasticsearch 结构见 [PRODUCT-ELASTICSEARCH-DESIGN.md](./PRODUCT-ELASTICSEARCH-DESIGN.md)。  
本文档必须遵守 [DATABASE-RULES.md](../00-governance/DATABASE-RULES.md)。如与工程级数据库规范冲突，以 [DATABASE-RULES.md](../00-governance/DATABASE-RULES.md) 为准。

## 2. Scope

本文档定义以下持久化对象：

- `ProductSpu`
- `ProductSku`
- `ProductCategory`
- `ProductImage`
- `ProductSnapshot`
- `ProductArchive`
- `ProductIdempotencyRecord`
- `ProductOutbox`
- `ProductAuditLog`

本文档不定义以下持久化对象：

- 库存主数据
- 订单主数据
- 支付主数据
- 存储对象主数据
- Elasticsearch 索引
- SQL 脚本

## 3. Database Rules

- 数据库固定使用 `MySQL 8.x`
- 存储引擎固定使用 `InnoDB`
- 字符集固定使用 `utf8mb4`
- 排序规则使用数据库实例可用的 `utf8mb4` 排序规则（推荐 `utf8mb4_unicode_ci`）
- 时间字段统一使用 `datetime(3)`
- 主键字段统一使用 `bigint`
- 金额字段统一使用 `decimal(18,2)`
- 枚举字段统一使用 `varchar`
- JSON 字段仅用于规格属性、快照内容、归档内容、请求摘要结果载荷和 outbox payload
- 商品主数据表统一包含 `created_at`、`created_by`、`updated_at`、`updated_by`
- 商品快照、归档、幂等、outbox 和审计表不强制增加 `created_by`、`updated_by`
- 分类、SPU、SKU、图片主数据使用逻辑删除字段 `deleted`
- 快照、归档、幂等、outbox 和审计表不使用逻辑删除字段

## 4. Naming Rules

- 表名前缀固定使用 `bacon_product_`
- 主键列统一命名为 `id`
- 租户隔离列统一命名为 `tenant_id`
- 聚合版本列固定命名为 `version`
- 审计发生时间统一命名为 `occurred_at`
- outbox 租约字段固定命名为 `processing_owner`、`lease_until`

## 5. Enum Storage Rules

### 5.1 Fixed Enums

- `product_status`: `DRAFT`、`ON_SALE`、`OFF_SALE`、`ARCHIVED`
- `sku_status`: `ENABLED`、`DISABLED`
- `category_status`: `ENABLED`、`DISABLED`
- `image_type`: `MAIN`、`GALLERY`、`DETAIL`
- `archive_type`: `CREATE`、`UPDATE_BASE`、`UPDATE_SKU`、`UPDATE_IMAGE`、`STATUS_CHANGE`、`ARCHIVE`
- `idempotency_status`: `PROCESSING`、`SUCCESS`、`FAILED`
- `outbox_status`: `PENDING`、`PROCESSING`、`SUCCEEDED`、`FAILED`、`DEAD`
- `outbox_event_type`: `PRODUCT_CREATED`、`PRODUCT_UPDATED`、`PRODUCT_STATUS_CHANGED`、`PRODUCT_ARCHIVED`
- `audit_action_type`: `PRODUCT_CREATE`、`PRODUCT_UPDATE`、`PRODUCT_STATUS_CHANGE`、`PRODUCT_ARCHIVE`、`PRODUCT_SNAPSHOT_CREATE`、`PRODUCT_INDEX_SYNC`

### 5.2 Fixed Length Rules

- `spu_code`: `varchar(64)`
- `spu_name`: `varchar(128)`
- `sku_code`: `varchar(64)`
- `sku_name`: `varchar(128)`
- `category_code`: `varchar(64)`
- `category_name`: `varchar(128)`
- `order_no`: `varchar(64)`
- `order_item_no`: `varchar(64)`
- `object_id`: `varchar(128)`
- `operation_type`: `varchar(64)`
- `idempotency_key`: `varchar(128)`
- `request_hash`: `varchar(128)`
- `result_ref_type`: `varchar(64)`
- `event_type`: `varchar(64)`
- `aggregate_type`: `varchar(64)`
- `processing_owner`: `varchar(128)`
- `operator_type`: `varchar(32)`
- `operator_id`: `varchar(64)`

## 6. Table Mapping

| Domain Object | Table |
|----|----|
| `ProductSpu` | `bacon_product_spu` |
| `ProductSku` | `bacon_product_sku` |
| `ProductCategory` | `bacon_product_category` |
| `ProductImage` | `bacon_product_image` |
| `ProductSnapshot` | `bacon_product_snapshot` |
| `ProductArchive` | `bacon_product_archive` |
| `ProductIdempotencyRecord` | `bacon_product_idempotency_record` |
| `ProductOutbox` | `bacon_product_outbox` |
| `ProductAuditLog` | `bacon_product_audit_log` |

## 7. Table Design

### 7.1 `bacon_product_spu`

表类型：`Master Table`

用途：

- 持久化商品 SPU 主数据
- 承载商品聚合版本和商品销售状态

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键，SPU ID |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `spu_code` | `varchar(64)` | N | SPU 业务编码，同租户唯一 |
| `spu_name` | `varchar(128)` | N | 商品名称 |
| `category_id` | `bigint` | N | 商品分类 ID |
| `description` | `text` | Y | 商品描述 |
| `main_image_object_id` | `varchar(128)` | Y | 主图 Storage 对象 ID |
| `product_status` | `varchar(32)` | N | 商品状态，取值见 `product_status` |
| `version` | `bigint` | N | SPU 聚合顺序版本 |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `bigint` | Y | 创建人用户主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `bigint` | Y | 更新人用户主键 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_spu_code(tenant_id, spu_code)`
- `idx_tenant_category_status(tenant_id, category_id, product_status)`
- `idx_tenant_updated_at(tenant_id, updated_at)`

### 7.2 `bacon_product_sku`

表类型：`Master Table`

用途：

- 持久化 SKU 主数据
- 承载规格属性、销售价和 SKU 状态

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键，SKU ID |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `spu_id` | `bigint` | N | 所属 SPU ID |
| `sku_code` | `varchar(64)` | N | SKU 业务编码，同租户唯一 |
| `sku_name` | `varchar(128)` | N | SKU 名称 |
| `spec_attributes` | `json` | N | 规格属性 JSON |
| `sale_price` | `decimal(18,2)` | N | 销售价 |
| `sku_status` | `varchar(32)` | N | SKU 状态，取值见 `sku_status` |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `bigint` | Y | 创建人用户主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `bigint` | Y | 更新人用户主键 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_sku_code(tenant_id, sku_code)`
- `idx_tenant_spu(tenant_id, spu_id)`
- `idx_tenant_sku_status(tenant_id, sku_status)`

### 7.3 `bacon_product_category`

表类型：`Master Table`

用途：

- 持久化商品域内置分类
- 支持树形分类结构

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键，分类 ID |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `parent_id` | `bigint` | Y | 父分类 ID，顶级分类为空 |
| `category_code` | `varchar(64)` | N | 分类编码，同租户唯一 |
| `category_name` | `varchar(128)` | N | 分类名称 |
| `sort_order` | `int` | N | 排序值 |
| `category_status` | `varchar(32)` | N | 分类状态，取值见 `category_status` |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `bigint` | Y | 创建人用户主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `bigint` | Y | 更新人用户主键 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_category_code(tenant_id, category_code)`
- `idx_tenant_parent_sort(tenant_id, parent_id, sort_order)`
- `idx_tenant_category_status(tenant_id, category_status)`

### 7.4 `bacon_product_image`

表类型：`Master Table`

用途：

- 持久化商品图片对象引用
- 不保存文件内容和底层存储路径

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键，图片记录 ID |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `spu_id` | `bigint` | N | 所属 SPU ID |
| `sku_id` | `bigint` | Y | 绑定 SKU ID，为空表示 SPU 图片 |
| `object_id` | `varchar(128)` | N | Storage 对象 ID |
| `image_type` | `varchar(32)` | N | 图片类型，取值见 `image_type` |
| `sort_order` | `int` | N | 排序值 |
| `deleted` | `tinyint(1)` | N | 逻辑删除标记 |
| `created_by` | `bigint` | Y | 创建人用户主键 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_by` | `bigint` | Y | 更新人用户主键 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_spu_type_sort(tenant_id, spu_id, image_type, sort_order)`
- `idx_tenant_sku(tenant_id, sku_id)`
- `idx_tenant_object(tenant_id, object_id)`

### 7.5 `bacon_product_snapshot`

表类型：`Runtime Table`

用途：

- 持久化订单商品明细维度的商品快照
- 以 `order_no + order_item_no` 作为幂等业务键

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键，快照 ID |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `order_no` | `varchar(64)` | N | 订单业务单号 |
| `order_item_no` | `varchar(64)` | N | 订单明细业务行号 |
| `spu_id` | `bigint` | N | 快照来源 SPU ID |
| `spu_code` | `varchar(64)` | N | 快照 SPU 编码 |
| `spu_name` | `varchar(128)` | N | 快照商品名称 |
| `sku_id` | `bigint` | N | 快照来源 SKU ID |
| `sku_code` | `varchar(64)` | N | 快照 SKU 编码 |
| `sku_name` | `varchar(128)` | N | 快照 SKU 名称 |
| `category_id` | `bigint` | N | 快照分类 ID |
| `category_name` | `varchar(128)` | N | 快照分类名称 |
| `spec_attributes` | `json` | N | 快照规格属性 |
| `sale_price` | `decimal(18,2)` | N | 快照销售价 |
| `quantity` | `int` | N | 快照购买数量 |
| `main_image_object_id` | `varchar(128)` | Y | 快照主图 Storage 对象 ID |
| `product_version` | `bigint` | N | 创建快照时的商品聚合版本 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_order_item(tenant_id, order_no, order_item_no)`
- `idx_tenant_order(tenant_id, order_no)`
- `idx_tenant_spu_version(tenant_id, spu_id, product_version)`
- `idx_tenant_sku(tenant_id, sku_id)`

### 7.6 `bacon_product_archive`

表类型：`Archive Table`

用途：

- 持久化商品聚合历史版本归档
- 支撑商品历史追溯

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键，归档 ID |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `spu_id` | `bigint` | N | SPU ID |
| `product_version` | `bigint` | N | 被归档商品聚合版本 |
| `archive_type` | `varchar(32)` | N | 归档类型，取值见 `archive_type` |
| `archive_content` | `json` | N | 商品聚合归档内容 |
| `created_at` | `datetime(3)` | N | 创建时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_spu_version(tenant_id, spu_id, product_version)`
- `idx_tenant_spu_created_at(tenant_id, spu_id, created_at)`

### 7.7 `bacon_product_idempotency_record`

表类型：`Runtime Table`

用途：

- 持久化商品命令幂等记录
- 为后续抽取统一幂等能力保留稳定字段

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键，幂等记录 ID |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `operation_type` | `varchar(64)` | N | 操作类型 |
| `idempotency_key` | `varchar(128)` | N | 幂等键 |
| `request_hash` | `varchar(128)` | N | 请求内容摘要 |
| `result_ref_type` | `varchar(64)` | Y | 结果引用类型 |
| `result_ref_id` | `varchar(128)` | Y | 结果引用 ID |
| `result_payload` | `json` | Y | 必要返回结果 JSON |
| `idempotency_status` | `varchar(32)` | N | 幂等状态，取值见 `idempotency_status` |
| `failure_reason` | `varchar(255)` | Y | 失败原因 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `uk_tenant_operation_key(tenant_id, operation_type, idempotency_key)`
- `idx_tenant_status_updated_at(tenant_id, idempotency_status, updated_at)`

### 7.8 `bacon_product_outbox`

表类型：`Runtime Table`

用途：

- 持久化 MySQL 到 Elasticsearch 的同步事件
- 支撑重试、租约抢占和死信

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键，事件 ID |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `aggregate_id` | `bigint` | N | 聚合 ID，固定为 SPU ID |
| `aggregate_type` | `varchar(64)` | N | 聚合类型，固定为 `PRODUCT` |
| `event_type` | `varchar(64)` | N | 事件类型，取值见 `outbox_event_type` |
| `product_version` | `bigint` | N | 变更后的商品聚合版本 |
| `payload` | `json` | N | 事件载荷 |
| `outbox_status` | `varchar(32)` | N | outbox 状态，取值见 `outbox_status` |
| `retry_count` | `int` | N | 重试次数 |
| `next_retry_at` | `datetime(3)` | Y | 下次重试时间 |
| `processing_owner` | `varchar(128)` | Y | 当前处理实例 |
| `lease_until` | `datetime(3)` | Y | 处理租约到期时间 |
| `failure_reason` | `varchar(255)` | Y | 最近一次失败原因 |
| `created_at` | `datetime(3)` | N | 创建时间 |
| `updated_at` | `datetime(3)` | N | 更新时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_status_retry(tenant_id, outbox_status, next_retry_at)`
- `idx_tenant_aggregate_version(tenant_id, aggregate_id, product_version)`
- `idx_tenant_owner_lease(tenant_id, processing_owner, lease_until)`

### 7.9 `bacon_product_audit_log`

表类型：`Audit Log Table`

用途：

- 记录商品关键操作审计日志
- 只追加，不更新历史记录

字段定义：

| Column | Type | Null | Description |
|----|----|----|----|
| `id` | `bigint` | N | 主键 |
| `tenant_id` | `bigint` | N | 租户业务键 |
| `spu_id` | `bigint` | Y | SPU ID |
| `sku_id` | `bigint` | Y | SKU ID |
| `order_no` | `varchar(64)` | Y | 订单业务单号 |
| `order_item_no` | `varchar(64)` | Y | 订单明细业务行号 |
| `action_type` | `varchar(64)` | N | 审计动作类型，取值见 `audit_action_type` |
| `before_status` | `varchar(32)` | Y | 变更前状态 |
| `after_status` | `varchar(32)` | Y | 变更后状态 |
| `operator_type` | `varchar(32)` | N | 操作人类型 |
| `operator_id` | `varchar(64)` | Y | 操作人标识 |
| `summary` | `json` | Y | 审计摘要 |
| `occurred_at` | `datetime(3)` | N | 发生时间 |

索引与约束：

- `pk(id)`
- `idx_tenant_spu_action(tenant_id, spu_id, action_type)`
- `idx_tenant_order_item(tenant_id, order_no, order_item_no)`
- `idx_tenant_operator_time(tenant_id, operator_type, operator_id, occurred_at)`

## 8. Relationship Rules

- 不强制数据库外键
- `ProductSku.spuId` 必须关联同租户 `ProductSpu.id`
- `ProductSpu.categoryId` 必须关联同租户 `ProductCategory.id`
- `ProductImage.objectId` 是 `Storage` 对象引用，不复制 `Storage` 主数据
- `ProductSnapshot.orderNo` 与 `orderItemNo` 是订单来源业务键，不复制订单主表结构
- `ProductSnapshot` 创建后不得回写或更新快照业务字段
- `ProductArchive.archiveContent` 保存商品聚合历史内容，不替代当前商品事实表

## 9. Persistence Rules

- 商品创建、编辑、上下架、归档必须在同一 MySQL 事务内写入商品事实数据和 `ProductOutbox`
- `ProductSpu.version` 初始为 `1`
- 任意影响展示、搜索、快照或可售校验的商品变更必须推进 `ProductSpu.version`
- 一次业务操作最多推进一次 `ProductSpu.version`
- 商品编辑必须基于 `spu_id + expected_version` 执行乐观锁更新
- `ProductIdempotencyRecord` 的唯一约束是商品命令幂等最终兜底
- `ProductSnapshot` 的唯一约束是订单商品快照幂等最终兜底
- `ProductOutbox` 消费必须先抢占再处理
- `ProductOutbox` 达到最大重试次数后必须进入 `DEAD`
- 审计日志写入失败不得破坏商品主业务提交结果

## 10. Query Model Rules

- 管理端商品详情查询必须读取 MySQL
- 管理端商品搜索和列表查询可以读取 Elasticsearch
- 商品快照创建必须读取 MySQL
- SKU 可售校验必须读取 MySQL
- `ProductSearchDocument` 不建 MySQL 表
- MySQL 分页查询必须优先使用 `tenant_id` 前缀索引

## 11. Open Items

无
