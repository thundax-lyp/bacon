# PRODUCT ELASTICSEARCH DESIGN

## 1. Purpose

本文档定义 `Product` 业务域的 Elasticsearch 索引结构。  
本文档只描述商品搜索投影、字段、同步规则、版本控制和查询模型。  
本文档不定义 MySQL 表结构，不提供 SQL。MySQL 设计见 [PRODUCT-DATABASE-DESIGN.md](./PRODUCT-DATABASE-DESIGN.md)。

## 2. Scope

本文档定义以下 Elasticsearch 对象：

- `ProductSearchDocument`
- `product_spu` 索引
- MySQL 到 Elasticsearch 的同步语义

本文档不定义以下内容：

- MySQL `DDL`
- Elasticsearch index template JSON
- 具体分片副本参数
- 商品详情事实模型
- 库存事实模型

## 3. Source Of Truth

- MySQL 是 Product 主数据事实来源
- Elasticsearch 是 Product 查询投影
- Elasticsearch 不得作为商品可售校验、订单商品快照、商品状态判断的事实来源
- 商品写操作不得直接以 ES 写入成功作为业务成功条件
- ES 同步失败不得回滚 MySQL 商品写入

## 4. Index Naming

- 商品 SPU 搜索索引固定命名为 `bacon_product_spu`
- 第一版只建立 SPU 级索引
- 第一版不建立独立 SKU 级索引
- SKU 信息以摘要和必要嵌套字段进入 SPU 文档

## 5. Document Identity

- `_id` 固定使用 `tenantId + ":" + spuId`
- 文档必须包含 `tenantId`
- 文档必须包含 `spuId`
- 文档必须包含 `productVersion`
- 同一 `tenantId + spuId` 只保留一份当前搜索文档

## 6. Field Mapping

### 6.1 Root Fields

| Field | Type | Description |
|----|----|----|
| `tenantId` | `long` | 租户业务键 |
| `spuId` | `long` | SPU ID |
| `spuCode` | `keyword` | SPU 编码 |
| `spuName` | `text + keyword` | 商品名称，支持关键词搜索和精确过滤 |
| `categoryId` | `long` | 分类 ID |
| `categoryName` | `keyword` | 分类名称 |
| `mainImageObjectId` | `keyword` | 主图 Storage 对象 ID |
| `productStatus` | `keyword` | 商品状态 |
| `minSalePrice` | `scaled_float` | 最低 SKU 销售价 |
| `maxSalePrice` | `scaled_float` | 最高 SKU 销售价 |
| `skuCount` | `integer` | SKU 总数 |
| `enabledSkuCount` | `integer` | 启用 SKU 数量 |
| `specSummary` | `text + keyword` | 规格摘要 |
| `productVersion` | `long` | 已同步商品聚合版本 |
| `createdAt` | `date` | 商品创建时间 |
| `updatedAt` | `date` | 商品更新时间 |
| `indexedAt` | `date` | ES 索引写入时间 |

### 6.2 SKU Nested Fields

`skus` 使用嵌套结构或对象数组，第一版只用于展示摘要和过滤，不作为业务事实来源。

| Field | Type | Description |
|----|----|----|
| `skus.skuId` | `long` | SKU ID |
| `skus.skuCode` | `keyword` | SKU 编码 |
| `skus.skuName` | `text + keyword` | SKU 名称 |
| `skus.salePrice` | `scaled_float` | SKU 销售价 |
| `skus.skuStatus` | `keyword` | SKU 状态 |
| `skus.specAttributes` | `object` | 规格属性摘要 |

### 6.3 Search Text Fields

| Field | Type | Description |
|----|----|----|
| `searchText` | `text` | 商品名称、SKU 名称、分类名称和规格摘要合并搜索字段 |
| `suggestText` | `completion` | 搜索建议字段，第一版可不启用 |

## 7. ProductSearchDocument

`ProductSearchDocument` 固定字段：

- `tenantId`
- `spuId`
- `spuCode`
- `spuName`
- `categoryId`
- `categoryName`
- `mainImageObjectId`
- `productStatus`
- `minSalePrice`
- `maxSalePrice`
- `skuCount`
- `enabledSkuCount`
- `specSummary`
- `skus`
- `searchText`
- `productVersion`
- `createdAt`
- `updatedAt`
- `indexedAt`

## 8. Sync Rules

- ES 同步固定由 `ProductOutbox` 驱动
- outbox payload 只作为触发信息，ES 文档必须从 MySQL 当前事实重建
- 商品创建、编辑、上下架、归档都必须触发 ES 同步
- 同步成功后 outbox 标记为 `SUCCEEDED`
- 同步失败后 outbox 进入可重试状态
- 重试耗尽后 outbox 标记为 `DEAD`
- 死信事件必须保留，支持后续人工重建索引

## 9. Version Rules

- ES 文档必须保存 `productVersion`
- 消费 outbox 时必须比较事件 `productVersion` 与 ES 当前文档 `productVersion`
- 当事件 `productVersion` 小于 ES 当前文档 `productVersion` 时，必须跳过该事件
- 当事件 `productVersion` 等于 ES 当前文档 `productVersion` 时，允许幂等覆盖
- 当事件 `productVersion` 大于 ES 当前文档 `productVersion` 时，允许写入
- ES 旧版本事件不得覆盖新版本文档
- 删除或归档商品第一版不删除 ES 文档，必须将 `productStatus` 同步为 `ARCHIVED`

## 10. Query Model Rules

商品搜索至少支持：

- 按 `tenantId` 过滤
- 按关键词搜索 `searchText`
- 按 `categoryId` 过滤
- 按 `productStatus` 过滤
- 按销售价区间过滤
- 按 `updatedAt` 排序
- 按 `minSalePrice` 排序

查询约束：

- 任何查询必须携带 `tenantId`
- 面向前台可售商品列表必须过滤 `productStatus=ON_SALE`
- 面向管理端列表可以查询全部状态
- 订单创建前不得直接信任 ES 搜索结果，必须通过 Product MySQL 快照能力再次校验

## 11. Rebuild Rules

- Product 必须提供按 `spuId` 重建单个 ES 文档的能力
- Product 必须提供按条件批量重建 ES 文档的应用层能力
- 重建 ES 文档必须从 MySQL 当前事实数据生成
- 重建不得修改 MySQL 商品事实数据
- 重建写入仍必须遵守 `productVersion` 防旧覆盖规则

## 12. Open Items

无
