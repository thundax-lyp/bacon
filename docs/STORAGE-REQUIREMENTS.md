# STORAGE REQUIREMENTS

## 1. Purpose

`Storage` 是 Bacon 的统一文件存储业务域。

本文档只定义三类信息：

- 领域边界
- 对外契约
- 核心业务规则

本文档不重复数据库字段明细，不重复上线整改清单。数据库细节见 `STORAGE-DATABASE-DESIGN.md`，上线验收见 `STORAGE-LAUNCH-READINESS.md`。

## 2. Scope

### 2.1 In Scope

- 统一管理 `StoredObject` 主数据
- 普通文件上传
- 大文件分段上传
- 对象删除
- 对象详情与分页查询
- 对象引用建立与清理
- 访问端点生成
- 本地文件与 `OSS` 双存储后端适配

### 2.2 Out Of Scope

- 图片裁剪
- 图片压缩
- 视频转码
- 文档在线预览
- `CDN` 管理
- 文件内容安全扫描

## 3. Bounded Context

### 3.1 Storage Owns

- `StoredObject`
- `StoredObjectReference`
- `MultipartUploadSession`
- `MultipartUploadPart`
- `StorageAuditLog`
- `StorageAuditOutbox`

### 3.2 Storage Does

- 生成并持久化存储对象主数据
- 调用底层存储上传和删除对象
- 管理对象状态与引用状态
- 管理分段上传会话与分片记录
- 对内提供统一文件访问契约

### 3.3 Storage Does Not Do

- 不拥有用户、租户、订单等业务主数据
- 不负责业务权限和数据可见性判断
- 不直接提供面向终端用户的业务语义接口
- 不让业务域持久化底层存储路径

## 4. Module Mapping

### 4.1 `bacon-storage-api`

职责：

- 跨域调用契约
- `DTO`
- `Command`
- `Facade`

核心接口：

- `StoredObjectFacade`

固定方法：

- `uploadObject`
- `initMultipartUpload`
- `uploadMultipartPart`
- `completeMultipartUpload`
- `abortMultipartUpload`
- `getObjectById`
- `markObjectReferenced`
- `clearObjectReference`
- `deleteObject`

### 4.2 `bacon-storage-interfaces`

职责：

- 管理端 `Controller`
- 内部 `Provider`
- 本地 `Facade` 适配

管理端接口：

- `GET /api/storage/objects`
- `GET /api/storage/objects/{objectId}`
- `DELETE /api/storage/objects/{objectId}`

内部 `Provider` 接口：

- `POST /providers/storage/objects/upload`
- `POST /providers/storage/objects/multipart/init`
- `POST /providers/storage/objects/multipart/{uploadId}/parts`
- `POST /providers/storage/objects/multipart/{uploadId}/complete`
- `DELETE /providers/storage/objects/multipart/{uploadId}`
- `GET /providers/storage/objects/{objectId}`
- `GET /providers/storage/objects`
- `POST /providers/storage/objects/{objectId}/references`
- `DELETE /providers/storage/objects/{objectId}/references`
- `DELETE /providers/storage/objects/{objectId}`

### 4.3 `bacon-storage-application`

职责：

- 用例编排
- 查询组装
- 删除、引用、分段上传等流程 orchestration

核心服务：

- `StoredObjectApplicationService`
- `MultipartUploadApplicationService`
- `StoredObjectQueryApplicationService`

### 4.4 `bacon-storage-domain`

职责：

- 领域实体和值对象
- 状态流转规则
- 仓储接口

### 4.5 `bacon-storage-infra`

职责：

- `Repository` 实现
- `MyBatis` 持久化
- 远程门面适配
- 基于 `bacon-common-oss` 的底层存储适配

## 5. Core Domain Model

### 5.1 `StoredObject`

固定字段：

- `id`: `StoredObjectId`
- `tenantId`: `TenantId`
- `storageType`: `StorageType`
- `bucketName`: `String`
- `objectKey`: `String`
- `originalFilename`: `String`
- `contentType`: `String`
- `size`: `Long`
- `accessEndpoint`: `String`
- `objectStatus`: `StoredObjectStatus`
- `referenceStatus`: `StoredObjectReferenceStatus`
- `createdBy`: `String`
- `createdAt`: `LocalDateTime`
- `updatedBy`: `String`
- `updatedAt`: `LocalDateTime`

说明：

- `id` 是存储对象业务主键，对外接口按字符串传递
- `accessEndpoint` 是派生访问端点，不是业务域主数据

### 5.2 `StoredObjectReference`

固定字段：

- `id`: `Long`
- `objectId`: `StoredObjectId`
- `ownerType`: `String`
- `ownerId`: `String`

### 5.3 `MultipartUploadSession`

固定字段：

- `id`: `Long`
- `uploadId`: `String`
- `tenantId`: `TenantId`
- `ownerType`: `String`
- `ownerId`: `String`
- `category`: `String`
- `originalFilename`: `String`
- `contentType`: `String`
- `objectKey`: `String`
- `providerUploadId`: `String`
- `totalSize`: `Long`
- `partSize`: `Long`
- `uploadedPartCount`: `Integer`
- `uploadStatus`: `String`
- `createdAt`: `LocalDateTime`
- `updatedAt`: `LocalDateTime`
- `completedAt`: `LocalDateTime`
- `abortedAt`: `LocalDateTime`

### 5.4 `MultipartUploadPart`

固定字段：

- `id`: `Long`
- `uploadId`: `String`
- `partNumber`: `Integer`
- `etag`: `String`
- `size`: `Long`
- `createdAt`: `LocalDateTime`

### 5.5 `StorageAuditLog`

固定字段：

- `id`: `Long`
- `tenantId`: `String`
- `objectId`: `StoredObjectId`
- `ownerType`: `String`
- `ownerId`: `String`
- `actionType`: `String`
- `beforeStatus`: `String`
- `afterStatus`: `String`
- `operatorType`: `String`
- `operatorId`: `Long`
- `occurredAt`: `LocalDateTime`

### 5.6 `StorageAuditOutbox`

固定字段：

- `id`: `Long`
- `tenantId`: `String`
- `objectId`: `StoredObjectId`
- `ownerType`: `String`
- `ownerId`: `String`
- `actionType`: `String`
- `beforeStatus`: `String`
- `afterStatus`: `String`
- `operatorType`: `String`
- `operatorId`: `Long`
- `occurredAt`: `LocalDateTime`
- `errorMessage`: `String`
- `status`: `String`
- `retryCount`: `Integer`
- `nextRetryAt`: `LocalDateTime`
- `updatedAt`: `LocalDateTime`

## 6. Fixed Enums And Stable Values

### 6.1 Domain Enums

- `StorageType`: `LOCAL_FILE`, `OSS`
- `StoredObjectStatus`: `ACTIVE`, `DELETING`, `DELETED`
- `StoredObjectReferenceStatus`: `UNREFERENCED`, `REFERENCED`

### 6.2 Runtime Status Values

- `uploadStatus`: `INITIATED`, `UPLOADING`, `COMPLETED`, `ABORTED`
- `actionType`: `UPLOAD`, `REFERENCE_ADD`, `REFERENCE_CLEAR`, `DELETE`
- `outboxStatus`: `NEW`, `PROCESSING`, `RETRYING`, `DEAD`

### 6.3 Cross-Domain Stable Values

- `ownerType` 由接入业务域定义，但必须全局稳定
- 业务域只能保存 `objectId`，不能保存底层 `objectKey` 或物理路径

## 7. Functional Requirements

### 7.1 Upload

- 普通上传成功后必须生成一条 `StoredObject`
- `StoredObject.objectStatus` 初始值固定为 `ACTIVE`
- `StoredObject.referenceStatus` 初始值固定为 `UNREFERENCED`
- 存储后端可以是 `LOCAL_FILE` 或 `OSS`

### 7.2 Multipart Upload

- 必须支持分段上传初始化、分片上传、完成上传、取消上传
- 一个分段上传会话由 `uploadId` 唯一标识
- 分片记录按 `uploadId + partNumber` 唯一约束
- 完成上传后才生成正式 `StoredObject`
- 取消上传后会话不得继续写入分片

### 7.3 Reference Management

- 业务域通过 `objectId + ownerType + ownerId` 建立对象引用
- 同一引用关系必须幂等
- 清理最后一个引用后，对象引用状态必须更新为 `UNREFERENCED`
- 存在引用的对象不得被业务流程误判为可直接清理

### 7.4 Query

- 必须支持按 `objectId` 查询对象详情
- 必须支持管理端分页查询
- 分页筛选至少支持：
  - `tenantId`
  - `storageType`
  - `objectStatus`
  - `referenceStatus`
  - `originalFilename`
  - `objectKey`

### 7.5 Delete

- 删除流程由 `Storage` 负责调用底层存储删除对象
- 删除完成后，对象状态必须进入已删除语义
- 已删除对象不得再建立新引用

## 8. Cross-Domain Constraints

- 其他业务域只能依赖 `bacon-storage-api`
- 单体模式走本地 `Facade`
- 微服务模式走远程 `Facade`
- 本地与远程模式必须保持相同契约
- 业务域负责业务权限、业务校验、文件类型和大小限制
- `Storage` 只负责存储能力和对象生命周期

## 9. Non-Functional Requirements

- 存储后端切换不能改变上层业务接口
- 接口契约变化必须同步更新 `api`、文档和测试
- 涉及表结构变化时，必须同步更新 `db/schema/storage.sql` 与 `db/data/storage.sql`
- 文档只保留稳定事实，不记录临时讨论过程
