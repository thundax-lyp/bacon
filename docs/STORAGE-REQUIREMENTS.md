# STORAGE REQUIREMENTS

## 1. Purpose

`Storage` 是 Bacon 的统一存储对象业务域。  
本文档定义上传对象、存储后端切换、对象引用、访问地址生成和跨域读写契约。  
目标是让后续头像、租户 Logo、导入导出文件、业务附件等能力共享同一套对象模型，而不是分散在各业务域重复建模。

## 2. Scope

### 2.1 In Scope

- 统一 `StoredObject` 主数据
- 文件上传
- 文件删除
- 文件元数据查询
- 存储后端切换
- 对象访问地址生成
- 对象引用标记
- 用户头像对象管理
- 商品图片对象管理

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

### 3.3 UPMS

- `UPMS` 只保存 `avatarObjectId`
- `UPMS` 不直接保存 `avatarUrl`
- `UPMS` 不直接保存本地文件路径或 `OSS object key`
- 用户头像上传、替换、清除通过 `Storage` 能力完成

### 3.4 Inventory

- 商品主数据中的图片字段只保存 `objectId`
- `Inventory` 不直接保存商品图片访问地址
- `Inventory` 不直接保存本地文件路径或 `OSS object key`
- 商品图片上传、替换、清除通过 `Storage` 能力完成

### 3.5 Cross-Domain Rule

- 其他业务域只能依赖 `bacon-storage-api`
- 其他业务域不得依赖 `Storage` 内部实现
- 业务域只保存 `objectId` 或固定引用字段，不直接保存底层存储路径
- 单体模式使用本地 `Facade` 实现
- 微服务模式使用远程 `Facade` 实现，并保持同一契约

## 4. Module Mapping

### 4.1 `bacon-storage-api`

- 跨域 `Facade`
- `DTO`
- 对外共享枚举

固定接口：

- `StoredObjectFacade`

`StoredObjectFacade` 固定方法：

- `uploadObject(command)`，返回固定 `StoredObjectDTO`
- `getObjectById(objectId)`，返回固定 `StoredObjectDTO`
- `markObjectReferenced(objectId, ownerType, ownerId)`，无返回
- `clearObjectReference(objectId, ownerType, ownerId)`，无返回
- `deleteObject(objectId)`，无返回

`StoredObjectDTO` 至少包含：

- `id`
- `storageType`
- `bucketName`
- `objectKey`
- `originalFilename`
- `contentType`
- `size`
- `accessUrl`
- `objectStatus`
- `referenceStatus`

`UploadObjectCommand` 至少包含：

- `ownerType`
- `tenantId`
- `category`
- `originalFilename`
- `contentType`
- `size`
- `inputStream` 或运行时等价输入对象

### 4.2 `bacon-storage-interfaces`

- `Controller`
- 请求 `Request`
- 响应 `Response`
- `Assembler`
- `Provider`

### 4.3 `bacon-storage-application`

固定服务：

- `StoredObjectApplicationService`
- `StoredObjectQueryApplicationService`

### 4.4 `bacon-storage-domain`

- `StoredObject`
- `StoredObjectReference`
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

## 5.1 Fixed Enums

- `storageType` 固定为 `LOCAL_FILE`、`OSS`
- `objectStatus` 固定为 `ACTIVE`、`DELETED`
- `referenceStatus` 固定为 `UNREFERENCED`、`REFERENCED`
- `ownerType` 固定至少包含 `UPMS_USER_AVATAR`、`INVENTORY_PRODUCT_IMAGE`

## 5.2 Fixed Fields

`StoredObject` 固定字段：

- `id`
- `tenantId`
- `storageType`
- `bucketName`
- `objectKey`
- `originalFilename`
- `contentType`
- `size`
- `accessUrl`
- `objectStatus`
- `referenceStatus`
- `createdAt`

`StoredObjectReference` 固定字段：

- `id`
- `objectId`
- `ownerType`
- `ownerId`
- `createdAt`

## 6. Global Constraints

- `StoredObject` 只保存稳定元数据，不保存业务对象完整快照
- `accessUrl` 由 `Storage` 统一生成
- 业务域读取头像、附件等对象时，只依赖 `objectId`
- 业务域不得自行拼接本地文件访问路径或 `OSS URL`
- 对象删除不得直接物理删除仍被引用的对象
- 对象替换时先建立新对象引用，再解除旧对象引用
- 同一对象可被多个业务对象引用时，必须通过引用表判断是否允许物理删除

## 7. Functional Requirements

### 7.1 StoredObject Management

功能对象：

- `StoredObject`

功能能力：

- 上传对象时必须先写底层存储，再写 `StoredObject` 主数据
- 上传成功后返回固定 `StoredObjectDTO`
- 查询对象时必须返回 `accessUrl`
- 删除对象时必须先校验引用状态

必要补充约束：

- `objectKey` 由 `Storage` 统一生成
- `originalFilename` 只用于展示，不作为唯一键
- `contentType` 固定保存上传声明值
- `size` 固定保存字节大小

### 7.2 Reference Management

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

### 7.3 User Avatar Rule

功能对象：

- `User.avatarObjectId`

功能能力：

- 用户头像上传后返回新的 `objectId`
- 用户头像替换时更新 `avatarObjectId`
- 用户头像清除时清空 `avatarObjectId`

必要补充约束：

- `UPMS` 只保存 `avatarObjectId`
- `UPMS` 响应模型固定返回 `avatarObjectId` 和 `avatarUrl`
- `avatarUrl` 由 `Storage` 查询结果派生，不单独落在 `UPMS`
- 头像旧对象在引用解除后由 `Storage` 负责删除策略

### 7.4 Product Image Rule

功能对象：

- `Product.imageObjectId`

功能能力：

- 商品图片上传后返回新的 `objectId`
- 商品图片替换时更新 `imageObjectId`
- 商品图片清除时清空 `imageObjectId`

必要补充约束：

- 商品域只保存 `imageObjectId`
- 商品响应模型固定返回 `imageObjectId` 和 `imageUrl`
- `imageUrl` 由 `Storage` 查询结果派生，不单独落在商品域
- 商品图片旧对象在引用解除后由 `Storage` 负责删除策略

## 8. Key Flows

### 8.1 Upload Flow

1. 调用方上传文件到 `Storage`
2. `Storage` 选择当前配置的底层存储实现
3. 底层存储返回 `bucketName`、`objectKey`
4. `Storage` 写入 `StoredObject`
5. 调用方拿到 `objectId`

### 8.2 Replace Avatar Flow

1. `UPMS` 调用 `Storage` 上传新头像
2. `UPMS` 更新 `User.avatarObjectId`
3. `UPMS` 调用 `Storage.clearObjectReference(oldObjectId, UPMS_USER_AVATAR, userId)`
4. `UPMS` 调用 `Storage.markObjectReferenced(newObjectId, UPMS_USER_AVATAR, userId)`

### 8.3 Replace Product Image Flow

1. 商品域调用 `Storage` 上传新图片
2. 商品域更新 `Product.imageObjectId`
3. 商品域调用 `Storage.clearObjectReference(oldObjectId, INVENTORY_PRODUCT_IMAGE, productId)`
4. 商品域调用 `Storage.markObjectReferenced(newObjectId, INVENTORY_PRODUCT_IMAGE, productId)`

### 8.4 Delete Object Flow

1. 调用删除接口
2. `Storage` 校验对象是否仍被引用
3. 仍被引用时拒绝删除
4. 未被引用时删除底层对象并更新 `StoredObject.objectStatus`

## 9. Non-Functional Requirements

- 本地文件和 `OSS` 必须共用同一业务契约
- 切换存储后端不得影响业务域表结构
- 访问地址生成规则必须集中在 `Storage`
- 上传失败不得写入孤立的 `StoredObject` 主数据
- 审计日志后续如接入，不得记录敏感文件内容

## 10. Open Items

无
