# 命名与目录规则

本文件只回答三个问题：

1. 该建什么类型的类
2. 该放哪一层、哪个目录
3. 该叫什么名字

## Fast Choice

- HTTP 外部入口：`Controller`
- HTTP 内部入口：`ProviderController`
- 接口层请求解析：`Resolver`
- 用例编排入口：`ApplicationService`
- 应用层内部复用：`Helper` / `Factory` / `Resolver` / `Executor`
- 核心领域规则：`DomainService`
- 领域读写契约：`Repository`
- 仓储实现：`RepositoryImpl`
- 数据库访问：`Mapper`
- 持久化对象：`DO`
- 持久化桥接转换：`PersistenceAssembler`
- 通用对象转换：`Converter`
- 基础类型和值对象互转：`Codec`
- 跨域调用契约：`Facade`
- 单体跨域适配：`FacadeLocalImpl`
- 微服务跨域适配：`FacadeRemoteImpl`
- 通用技术能力：`Service`

## Hard Rules

- `Controller`、`ProviderController` 直接调用本域 `ApplicationService`
- 跨域调用统一依赖 `Facade`，不直接依赖对方 `ApplicationService`
- `interfaces.controller` 路径固定使用 `/{domain}/**`，对外完整路径固定为 `/api/{domain}/**`
- `interfaces.provider` 路径固定使用 `/providers/{domain}/**`，对内完整路径固定为 `/api/providers/{domain}/**`
- `{domain}` 固定与业务域目录名一致（`auth`、`upms`、`order`、`inventory`、`payment`、`storage`），不得混用单复数别名
- `Local`、`Remote` 是运行模式语义，不省略
- `Codec` 只做基础类型和值对象互转，不做业务编排
- `Converter` 做通用对象转换，不承载仓储语义
- `PersistenceAssembler` 只做 `Domain <-> DO`
- `infra` 层已有更精确后缀时，不再使用业务 `*Service`
- `domain.model.enums` 简单枚举统一实现 `value() -> name()` 和 `from(...)`

## Placement

- `interfaces/controller/`
  `Controller`
- `interfaces/provider/`
  `ProviderController`
- `interfaces/resolver/`
  `Resolver`
- `interfaces/facade/`
  `FacadeLocalImpl`
- `application/command/`
  写动作 `ApplicationService`
- `application/query/`
  查动作 `ApplicationService`
- `application/audit/`
  审计场景 `ApplicationService`
- `application/support/`
  `Helper` / `Factory` / `Resolver` / `Executor`
- `application/codec/`
  `Codec`
- `domain/service/`
  `DomainService`
- `domain/repository/`
  `Repository`
- `infra/repository/impl/`
  `RepositoryImpl`
- `infra/persistence/mapper/`
  `Mapper`
- `infra/persistence/dataobject/`
  `DO`
- `infra/persistence/assembler/`
  `PersistenceAssembler`
- `api/facade/`
  `Facade`
- `infra/facade/remote/`
  `FacadeRemoteImpl`

## Naming

- `Controller`
  `{业务对象}{动作}Controller`
- `ProviderController`
  `{业务对象}{动作}ProviderController`
- `Resolver`
  `{业务对象}{动作}Resolver`
- `ApplicationService`
  `{业务对象}{动作}ApplicationService`
  `{业务对象}QueryApplicationService`
  `{业务对象}{场景}{动作}ApplicationService`
- `DomainService`
  `{业务对象}DomainService`
- `Repository`
  `{业务对象}Repository`
- `RepositoryImpl`
  `{业务对象}RepositoryImpl`
- `Mapper`
  `{业务对象}Mapper`
- `DO`
  `{业务对象}DO`
- `PersistenceAssembler`
  `{业务对象}PersistenceAssembler`
- `Converter`
  `{业务对象}Converter`
- `Codec`
  `{业务对象}Codec`
- `Facade`
  `{业务对象}{动作}Facade`
- `FacadeLocalImpl`
  `{业务对象}{动作}FacadeLocalImpl`
- `FacadeRemoteImpl`
  `{业务对象}{动作}FacadeRemoteImpl`

## Good Names

- `OrderCreateController`
- `OrderQueryProviderController`
- `TenantRequestResolver`
- `OrderCreateApplicationService`
- `OrderQueryApplicationService`
- `OrderAuditCompensationApplicationService`
- `OrderAuditReplayApplicationService`
- `OrderDomainService`
- `OrderRepository`
- `OrderRepositoryImpl`
- `OrderMapper`
- `OrderDO`
- `OrderPersistenceAssembler`
- `OrderConverter`
- `OrderNoCodec`
- `UserReadFacade`
- `UserReadFacadeLocalImpl`
- `UserReadFacadeRemoteImpl`
- `VerificationCodeService`

## Bad Names

- `OrderService`
- `InventoryService`
- `InventoryAuditCompensationService`
- 看不出层次和职责的名字
