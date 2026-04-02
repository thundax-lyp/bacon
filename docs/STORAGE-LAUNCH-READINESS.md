# STORAGE LAUNCH READINESS

## 1. Purpose

本文档只保留 `Storage` 上线前的验收门槛。

本文档不记录已经完成的历史整改过程，不重复需求和数据库设计内容。

## 2. Scope

覆盖范围：

- `bacon-storage-api`
- `bacon-storage-interfaces`
- `bacon-storage-application`
- `bacon-storage-domain`
- `bacon-storage-infra`
- `bacon-common-oss`
- `db/schema/storage.sql`
- `db/data/storage.sql`

## 3. Release Gates

### 3.1 Contract Gate

上线前必须确认：

- 本地 `Facade` 与远程 `Facade` 契约一致
- `Provider` 路径与远程调用路径一致
- 文档中的接口路径、字段名、状态值与当前代码一致

### 3.2 Data Gate

上线前必须确认：

- `db/schema/storage.sql` 与当前持久化实现一致
- `db/data/storage.sql` 与初始化策略一致
- 文档中的枚举值与数据库 `varchar` 存储值一致

### 3.3 Lifecycle Gate

上线前必须确认：

- 普通上传能生成 `StoredObject`
- 分段上传能初始化、上传分片、完成、取消
- 建立引用和清理引用会正确维护 `referenceStatus`
- 删除后对象进入已删除语义，且不能再作为可用对象继续使用

### 3.4 Backend Gate

上线前必须确认：

- `LOCAL_FILE` 模式可正常上传、查询、删除
- `OSS` 模式可正常上传、查询、删除
- 两种后端对上层接口的行为一致

### 3.5 Query Gate

上线前必须确认：

- 管理端详情查询可用
- 管理端分页查询可用
- 分页筛选字段与文档一致

### 3.6 Test Gate

上线前至少应覆盖：

- 普通上传主流程
- 分段上传主流程
- 引用建立与清理
- 删除流程
- 本地和远程门面契约

## 4. Current Rule

- 若存在阻断上线的问题，应直接补到本文件，不要散落到其他文档
- 若某项问题已经修复，应从本文件移除，不保留历史描述
- 本文件始终只保留“当前仍需要确认的事项”
