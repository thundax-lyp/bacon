# STORAGE REQUIREMENTS

## 1. Purpose

`Storage` 是 Bacon 的统一存储对象业务域。  
本文档定义上传对象、存储后端切换、对象引用、访问地址生成和跨域读写契约。  
目标是让后续各业务域资源能力共享同一套对象模型，而不是分散在各业务域重复建模。

## 2. Scope

### 2.1 In Scope

- 统一 `StoredObject` 主数据
- 文件上传
- 大文件分段上传
- 文件删除
- 文件元数据查询
- 管理员全局管理 `StoredObject`
- 存储后端切换
- 对象访问地址生成
- 对象引用标记
- 业务资源对象管理（如图片、附件、导入导出文件）

### 2.2 Out Of Scope

- 图片裁剪
- 图片压缩
- 视频转码
- 文档在线预览
- CDN 管理
- 病毒扫描

## 3. Bounded Context

### 3.1 Storage

- `Storage` 拥有 `StoredObject` 主数据
- `Storage` 负责上传到 `OSS` 或本地文件系统
- `Storage` 负责生成稳定的对象访问地址
- `Storage` 负责对象状态与引用状态管理
- `Storage` 不拥有用户、租户、订单等业务主数据

### 3.2 Common OSS

- `bacon-common-oss` 只负责底层对象存储技术适配
- `bacon-common-oss` 提供 `OSS` 与本地文件的统一上传、删除、读取基础能力
- `bacon-common-oss` 不直接承载业务表、不直接承载业务 `Facade`

### 3.3 Cross-Domain Rule

- 其他业务域只能依赖 `bacon-storage-api`
- 其他业务域不得依赖 `Storage` 内部实现
- 业务域只保存 `objectId` 或固定引用字段，不直接保存底层存储路径
- 单体模式使用本地 `Facade` 实现
- 微服务模式使用远程 `Facade` 实现，并保持同一契约

### 3.4 External API Exposure Rule

- 面向终端用户或第三方调用方的资源业务接口必须由接入业务域提供，不得让外部直接依赖 `Storage Provider`
- `Storage` 可为平台管理员后台提供受控 `Controller`，用于全局查询与清理 `StoredObject`
- 管理端前端调用 `Storage Controller`，内部跨域调用 `Storage Provider` 或 `StoredObjectFacade`
- 终端用户侧对外接口路径应使用业务语义，不得以 `Storage` 资源路径直接对外建模
- 业务域负责认证、鉴权、数据可见性、业务主键校验、文件类型与大小校验
- `Storage` 负责上传、删除、引用关系与访问地址生成，不负责业务权限判断

## 4. Module Mapping

### 4.1 `bacon-storage-api`

- 跨域 `Facade`
- `DTO`
- 对外共享枚举
- 内部管理分页查询 `DTO`

固定接口：

- `StoredObjectFacade`

`StoredObjectFacade` 固定方法：

- `uploadObject(command)`，返回固定 `StoredObjectDTO`
- `initMultipartUpload(command)`，返回固定 `MultipartUploadSessionDTO`
- `uploadMultipartPart(command)`，返回固定 `MultipartUploadPartDTO`
- `completeMultipartUpload(command)`，返回固定 `StoredObjectDTO`
- `abortMultipartUpload(command)`，无返回
- `getObjectById(objectId)`，返回固定 `StoredObjectDTO`
- `markObjectReferenced(objectId, ownerType, ownerId)`，无返回
- `clearObjectReference(objectId, ownerType, ownerId)`，无返回
- `deleteObject(objectId)`，无返回

固定模型：

- `StoredObjectDTO`
- `UploadObjectCommand`
- `MultipartUploadSessionDTO`
- `MultipartUploadPartDTO`
- `InitMultipartUploadCommand`
- `UploadMultipartPartCommand`
- `CompleteMultipartUploadCommand`
- `AbortMultipartUploadCommand`
- `StoredObjectPageQueryDTO`
- `StoredObjectPageResultDTO`

### 4.2 `bacon-storage-interfaces`

- 管理端 `Controller`
- `Provider`
- 本地域内 `Facade` 适配器

管理端 `Controller` 固定支持：

- `GET /api/storage/objects`，分页查询 `StoredObject`
- `GET /api/storage/objects/{objectId}`，查询 `StoredObject` 详情
- `DELETE /api/storage/objects/{objectId}`，删除 `StoredObject`

`Provider` 固定支持管理员全局管理 `StoredObject`：

- `uploadObject(command)`，返回固定 `StoredObjectDTO`
- `initMultipartUpload(command)`，返回固定 `MultipartUploadSessionDTO`
- `uploadMultipartPart(command)`，返回固定 `MultipartUploadPartDTO`
- `completeMultipartUpload(command)`，返回固定 `StoredObjectDTO`
- `abortMultipartUpload(command)`，无返回
- `getObjectById(objectId)`，返回固定 `StoredObjectDTO`
- `pageObjects(query)`，返回固定 `StoredObjectPageResultDTO`
- `markObjectReferenced(objectId, ownerType, ownerId)`，无返回
- `clearObjectReference(objectId, ownerType, ownerId)`，无返回
- `deleteObject(objectId)`，无返回

### 4.3 `bacon-storage-application`

固定服务：

- `StoredObjectApplicationService`
- `MultipartUploadApplicationService`
- `StoredObjectQueryApplicationService`

### 4.4 `bacon-storage-domain`

- `StoredObject`
- `StoredObjectReference`
- `MultipartUploadSession`
- `MultipartUploadPart`
- `Repository` 接口
- 存储状态规则
- 引用状态规则

### 4.5 `bacon-storage-infra`

- `Repository` 实现
- `MyBatis-Plus Mapper`
- `DataObject`
- 远程适配器
- 基于 `bacon-common-oss` 的本地文件和 `OSS` 实现适配

## 5. Core Domain Objects

- `StoredObject`
- `StoredObjectReference`
- `MultipartUploadSession`
- `MultipartUploadPart`
- `StorageAuditLog`
- `StorageAuditOutbox`

## 5.1 Fixed Enums

- `storageType` 固定为 `LOCAL_FILE`、`OSS`
- `objectStatus` 固定为 `ACTIVE`、`DELETING`、`DELETED`
- `referenceStatus` 固定为 `UNREFERENCED`、`REFERENCED`
- `uploadStatus` 固定为 `INITIATED`、`UPLOADING`、`COMPLETED`、`ABORTED`
- `auditActionType` 固定为 `UPLOAD`、`DELETE`、`REFERENCE_ADD`、`REFERENCE_CLEAR`
- `auditOutboxStatus` 固定为 `NEW`、`PROCESSING`、`RETRYING`、`DEAD`
- `ownerType` 由接入业务域约定并在全局保持稳定

## 5.2 Terminology

- `accessEndpoint`：由 `Storage` 派生的对象访问端点，仅用于展示和下载，不作为业务主数据
- `providerUploadId`：底层 `OSS/S3 API` 分段上传会话标识，只属于 `Storage` 内部运行态
- 管理员全局管理 `StoredObject`：指 `Storage Controller` 提供给管理端前端的查询与删除能力，以及 `Storage Provider` 提供给内部服务的原始管理能力

## 5.3 Fixed Fields

`StoredObject` 固定字段：

- `id`
- `tenantNo`
- `storageType`
- `bucketName`
- `objectKey`
- `originalFilename`
- `contentType`
- `size`
- `accessEndpoint`
- `objectStatus`
- `referenceStatus`
- `createdAt`

`StoredObjectReference` 固定字段：

- `id`
- `objectId`
- `ownerType`
- `ownerId`

`MultipartUploadSession` 固定字段：

- `uploadId`
- `ownerType`
- `ownerId`
- `tenantNo`
- `category`
- `originalFilename`
- `contentType`
- `objectKey`
- `providerUploadId`
- `totalSize`
- `partSize`
- `uploadedPartCount`
- `uploadStatus`
- `createdAt`

`MultipartUploadPart` 固定字段：

- `id`
- `uploadId`
- `partNumber`
- `etag`
- `size`
- `createdAt`

`StorageAuditLog` 固定字段：

- `id`
- `tenantNo`
- `objectId`
- `ownerType`
- `ownerId`
- `actionType`
- `beforeStatus`
- `afterStatus`
- `operatorType`
- `operatorId`
- `occurredAt`

`StorageAuditOutbox` 固定字段：

- `id`
- `tenantNo`
- `objectId`
- `ownerType`
- `ownerId`
- `actionType`
- `beforeStatus`
- `afterStatus`
- `operatorType`
- `operatorId`
- `occurredAt`
- `errorMessage`
- `status`
- `retryCount`
- `nextRetryAt`
- `updatedAt`

## 5.4 Fixed Request Contracts

`UploadObjectCommand` 固定字段：

- `ownerType`
- `tenantNo`
- `category`
- `originalFilename`
- `contentType`
- `size`
- `inputStream`

`InitMultipartUploadCommand` 固定字段：

- `ownerType`
- `ownerId`
- `tenantNo`
- `category`
- `originalFilename`
- `contentType`
- `totalSize`
- `partSize`

`UploadMultipartPartCommand` 固定字段：

- `uploadId`
- `ownerType`
- `ownerId`
- `tenantNo`
- `partNumber`
- `size`
- `inputStream`

`CompleteMultipartUploadCommand` 固定字段：

- `uploadId`
- `ownerType`
- `ownerId`
- `tenantNo`

`AbortMultipartUploadCommand` 固定字段：

- `uploadId`
- `ownerType`
- `ownerId`
- `tenantNo`

`StoredObjectPageQueryDTO` 固定字段：

- `tenantNo`
- `storageType`
- `objectStatus`
- `referenceStatus`
- `originalFilename`
- `objectKey`
- `pageNo`
- `pageSize`

## 5.5 Fixed Response Contracts

`StoredObjectDTO` 固定字段：

- `id`
- `storageType`
- `bucketName`
- `objectKey`
- `originalFilename`
- `contentType`
- `size`
- `accessEndpoint`
- `objectStatus`
- `referenceStatus`
- `createdAt`

`MultipartUploadSessionDTO` 固定字段：

- `uploadId`
- `ownerType`
- `ownerId`
- `tenantNo`
- `category`
- `originalFilename`
- `contentType`
- `totalSize`
- `partSize`
- `uploadedPartCount`
- `uploadStatus`

`MultipartUploadPartDTO` 固定字段：

- `uploadId`
- `partNumber`
- `etag`

`StoredObjectPageResultDTO` 固定字段：

- `records`
- `total`
- `pageNo`
- `pageSize`

其中 `records` 元素固定为 `StoredObjectDTO`

## 5.6 Uniqueness And Index Rules

- `StoredObject.id` 固定全局唯一
- `StoredObject.objectKey` 固定全局唯一
- `StoredObjectReference` 的 `(objectId, ownerType, ownerId)` 固定唯一
- `MultipartUploadSession.uploadId` 固定全局唯一
- `MultipartUploadPart` 的 `(uploadId, partNumber)` 固定唯一
- `StorageAuditOutbox.id` 固定全局唯一

## 6. Global Constraints

- `StoredObject` 只保存稳定元数据，不保存业务对象完整快照
- `accessEndpoint` 由 `Storage` 统一生成
- 业务域读取资源对象时，只依赖 `objectId`
- 业务域不得自行拼接本地文件访问路径或 `OSS URL`
- 对象删除不得直接物理删除仍被引用的对象
- 对象替换时先建立新对象引用，再解除旧对象引用
- 同一对象可被多个业务对象引用时，必须通过引用表判断是否允许物理删除
- 对象进入 `DELETING` 后不得再建立新引用
- 普通文件上传和大文件分段上传必须使用不同接口和不同应用服务逻辑
- 大文件上传完成前不得写入正式 `StoredObject`
- 大文件上传中断、取消、超时后，`Storage` 必须能够清理未完成分段数据
- 大文件分段上传初始化后，`Storage` 必须持久化本次上传使用的 `objectKey`
- 大文件分段上传初始化后，`Storage` 必须持久化 `ownerType`、`ownerId`、`tenantNo`，后续所有分段操作都必须校验归属一致
- 当底层为 `OSS/S3 API` 时，`Storage` 必须持久化 provider 分段上传会话标识，供后续分片上传、完成和取消复用
- `providerUploadId` 属于 `Storage` 内部运行态字段，不得暴露为业务域主数据
- 单次普通上传最大文件大小必须由 `Storage` 配置统一控制
- 分片上传固定分片大小必须由 `Storage` 配置统一控制，不允许调用方自定义分片规格
- 分片上传总文件最大大小必须由 `Storage` 配置统一控制
- 分片上传超时清理必须由 `Storage` 定时任务统一执行

## 7. Functional Requirements

### 7.1 StoredObject Management

功能对象：

- `StoredObject`

功能能力：

- 上传对象时必须先写底层存储，再写 `StoredObject` 主数据
- 上传成功后返回固定 `StoredObjectDTO`
- 查询对象时必须返回 `accessEndpoint`
- 删除对象时必须先校验引用状态，并先把对象状态提交为 `DELETING`

必要补充约束：

- `objectKey` 由 `Storage` 统一生成
- `originalFilename` 只用于展示，不作为唯一键
- `contentType` 固定保存上传声明值
- `size` 固定保存字节大小
- `DELETING/DELETED` 对象查询固定返回 `404`
- `DELETING/DELETED` 对象不得再建立新引用
- 物理删除失败时对象状态必须保持为 `DELETING`，供后续重试或补偿

### 7.2 StoredObject Admin Management

功能对象：

- `StoredObject`

功能能力：

- 管理员可按租户、存储类型、对象状态、引用状态、原始文件名、对象键分页查询 `StoredObject`
- 管理员可查看单个 `StoredObject` 详情
- 管理员可删除未被引用的 `StoredObject`

必要补充约束：

- 管理员前端固定调用 `Storage Controller`，不直接调用 `Storage Provider`
- `Storage Controller` 的 `storageType`、`objectStatus`、`referenceStatus` 查询参数固定按枚举解析，非法值返回 `400`
- 内部跨域管理能力固定通过 `Storage Provider` 提供，不扩展 `StoredObjectFacade`
- 分页查询固定返回 `StoredObjectPageResultDTO`
- 分页查询固定按创建时间倒序返回，创建时间相同时按 `id` 倒序返回
- `pageNo`、`pageSize` 必须先归一化后再进入仓储查询
- `pageSize` 固定受工程统一上限约束

### 7.3 Multipart Upload Management

功能对象：

- `MultipartUploadSession`
- `MultipartUploadPart`

功能能力：

- 初始化大文件分段上传
- 上传单个分段
- 完成分段上传并合并对象
- 取消分段上传
- 查询分段上传会话状态

必要补充约束：

- 大文件上传与普通文件上传必须使用不同接口
- `uploadId` 固定作为分段上传会话业务键
- 同一 `(uploadId, partNumber)` 固定唯一
- `partSize` 必须等于 `Storage` 配置的固定分片大小，默认 `8MB`
- `totalSize` 不得超过 `Storage` 配置的总大小上限，默认 `4GB`
- 分段上传完成后才允许写入正式 `StoredObject`
- 分段上传取消或超时后不得生成正式 `StoredObject`
- 合并完成后必须清理临时分段数据
- 上传分段、完成分段、取消分段都必须校验 `tenantNo`、`ownerType`、`ownerId` 与初始化会话一致
- 分段完成前必须校验分片序号连续、已上传分片数一致、分片总大小等于会话 `totalSize`
- 超时 `INITIATED/UPLOADING` 会话必须由定时任务自动转为 `ABORTED` 并清理底层临时分片数据

### 7.4 Reference Management

功能对象：

- `StoredObjectReference`

功能能力：

- 建立对象引用
- 清理对象引用
- 查询对象是否仍被引用

必要补充约束：

- 同一 `(objectId, ownerType, ownerId)` 固定唯一
- 清理引用后，如对象已无任何引用，可进入删除候选状态
- 引用关系变更不得依赖调用方本地缓存判断

### 7.5 Storage Audit Management

功能对象：

- `StorageAuditLog`
- `StorageAuditOutbox`

功能能力：

- 主流程成功时记录存储审计日志
- 审计写入失败时补写 `StorageAuditOutbox`
- 定时任务重试失败审计
- 超过重试上限的 outbox 固定转为 `DEAD`

必要补充约束：

- 审计失败不得阻断上传、删除、引用管理主流程
- 审计补偿必须保留 `objectId`、`ownerType`、`ownerId`、`actionType`
- 重试任务必须支持批量扫描与退避重试
- `DEAD` 状态 outbox 必须定时清理

### 7.6 Generic Business Integration Rule

功能对象：

- 任意业务域的资源关联字段（如主数据中的 `objectId` 引用字段）

功能能力：

- 业务域对外提供资源上传、替换、清除、访问入口
- 业务域内部调用 `Storage` 完成对象存储与引用管理
- 业务域响应模型按需返回 `objectId` 与派生访问地址

必要补充约束：

- 业务域只持久化 `objectId` 或固定引用字段，不复制 `bucketName`、`objectKey`、`accessEndpoint`
- 业务域替换资源时必须先绑定新对象，再解除旧对象引用
- 业务域清除资源时必须先解除引用，再清空业务字段
- 终端用户前端与第三方调用方不得直接依赖 `Storage Provider` 或内部路径

## 8. Key Flows

### 8.1 Upload Flow

1. 调用方上传文件到 `Storage`
2. `Storage` 选择当前配置的底层存储实现
3. 底层存储返回 `bucketName`、`objectKey`
4. `Storage` 写入 `StoredObject`
5. 调用方拿到 `objectId`

### 8.2 Multipart Upload Flow

1. 调用方调用 `Storage.initMultipartUpload`
2. `Storage` 创建 `uploadId` 和分段上传会话
3. 调用方按 `partNumber` 多次上传分段
4. `Storage` 逐段写入临时分段数据并记录分段元数据
5. 调用方调用 `Storage.completeMultipartUpload`
6. `Storage` 校验分段完整性并合并底层对象
7. `Storage` 写入正式 `StoredObject`
8. `Storage` 清理临时分段数据
9. 调用方拿到 `objectId`

### 8.3 Delete Object Flow

1. 调用删除接口
2. `Storage` 校验对象是否仍被引用
3. 仍被引用时拒绝删除
4. 未被引用时先提交 `StoredObject.objectStatus=DELETING`
5. `Storage` 删除底层对象
6. 删除成功后再提交 `StoredObject.objectStatus=DELETED`

### 8.4 Admin Manage StoredObject Flow

1. 管理员调用 `Storage Controller` 分页查询接口
2. `Storage` 归一化分页参数并按筛选条件查询 `StoredObject`
3. `Storage` 返回 `StoredObjectPageResultDTO`
4. 管理员按需调用详情接口获取单个 `StoredObject`
5. `Storage Controller` 内部调用应用服务完成删除

### 8.5 Generic Business Upload And Access Flow

1. 前端调用业务域资源接口
2. 业务域完成认证、鉴权、数据可见性与业务参数校验
3. 业务域调用 `Storage` 上传对象并获取 `objectId`
4. 业务域回写资源关联字段（如主数据中的 `objectId`）
5. 如为替换场景，业务域先建立新对象引用，再解除旧对象引用
6. 前端通过业务域接口获取资源访问信息，业务域通过 `Storage` 派生 `accessEndpoint`

## 9. Non-Functional Requirements

- 本地文件和 `OSS` 必须共用同一业务契约
- 切换存储后端不得影响业务域表结构
- 访问地址生成规则必须集中在 `Storage`
- 上传失败不得写入孤立的 `StoredObject` 主数据
- 审计写入失败不得阻断主流程，但必须输出 `error` 级别告警日志，并累加审计写入成功/失败指标
- 审计写入失败时必须补写 `StorageAuditOutbox`，供后续重试任务补偿
- 审计补偿任务必须支持批量扫描、指数退避重试，并在超过上限后把 outbox 标记为 `DEAD`
- `DEAD` 状态 outbox 必须按保留期定时清理，避免补偿表无限增长
- 审计日志后续如接入，不得记录敏感文件内容

## 10. Open Items

无
