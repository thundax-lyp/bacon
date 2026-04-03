# 命名与目录规则

## 选型

- 面向 HTTP 暴露业务入口：建 `Controller` 或 `ProviderController`
- 编排本域业务用例：建 `ApplicationService`
- 封装跨实体、跨聚合的领域规则：建 `DomainService`
- 定义跨域调用能力：建 `Facade`
- 实现单体模式跨域调用：建 `FacadeLocalImpl`
- 实现微服务模式跨域调用：建 `FacadeRemoteImpl`
- 定义领域对象读写能力：建 `Repository`
- 实现仓储落库与查询：建 `RepositoryImpl`
- 只做数据库访问：建 `Mapper`
- 只做对象转换：建 `Converter`
- 只是内部复用辅助逻辑：建 `Helper` / `Factory` / `Resolver` / `Executor`
- 提供通用技术能力：建 `Service`

## 规则

- `Controller`：对外业务 HTTP 入口，命名 `{业务对象}{动作}Controller`，目录 `interfaces/controller/`
- `ProviderController`：对内服务 HTTP 入口，命名 `{业务对象}{动作}ProviderController`，目录 `interfaces/provider/`
- `Resolver`：接口层请求解析辅助对象，命名 `{业务对象}{动作}Resolver`，目录 `interfaces/resolver/`
- `ApplicationService`：业务用例编排入口，命名 `{业务对象}{动作}ApplicationService`、`{业务对象}QueryApplicationService`、`{业务对象}{场景}{动作}ApplicationService`，目录 `application/command/`、`application/query/`、`application/audit/`
- 应用层辅助类：内部辅助对象，不是业务用例入口，命名 `*Helper` / `*Factory` / `*Resolver` / `*Executor`，目录 `application/support/`
- `DomainService`：封装领域规则，命名 `{业务对象}DomainService`，目录 `domain/service/`
- `Repository`：领域仓储接口，定义领域对象读写能力，命名 `{业务对象}Repository`，目录 `domain/repository/`
- `RepositoryImpl`：仓储实现，负责落库、查询、组装，命名 `{业务对象}RepositoryImpl`，目录 `infra/repository/impl/`
- `Mapper`：持久化映射，只负责数据库访问，命名 `{业务对象}Mapper`，目录 `infra/repository/mapper/`
- `Converter`：对象转换，只负责对象转换，命名 `{业务对象}Converter`，目录 `infra/repository/converter/`
- `Facade`：跨域调用契约，只定义能力，不承担 HTTP 入口职责，命名 `{业务对象}{动作}Facade`，目录 `api/facade/`
- `FacadeLocalImpl`：单体模式门面实现，直接调用对方 `ApplicationService`，命名 `{业务对象}{动作}FacadeLocalImpl`，目录 `interfaces/facade/`
- `FacadeRemoteImpl`：微服务模式门面实现，通过 HTTP / RPC 调用对方服务，命名 `{业务对象}{动作}FacadeRemoteImpl`，目录 `infra/facade/remote/`
- `Service`：通用技术能力，命名 `{能力}Service`，目录对应技术模块的 `service/`
- `Controller`、`ProviderController` 直接调用本域 `ApplicationService`
- 跨域调用统一依赖 `Facade`，不直接依赖对方 `ApplicationService`
- `Audit` 是独立业务场景时，命名 `{业务对象}Audit{动作}ApplicationService`
- `Local`、`Remote` 是运行模式语义，不省略
- `infra` 层如果有更精确的后缀，不用业务 `*Service`

## 示例

- 推荐：`OrderCreateController`
- 推荐：`OrderQueryProviderController`
- 推荐：`TenantRequestResolver`
- 推荐：`OrderCreateApplicationService`
- 推荐：`OrderQueryApplicationService`
- 推荐：`OrderAuditCompensationApplicationService`
- 推荐：`OrderAuditReplayApplicationService`
- 推荐：`OrderDomainService`
- 推荐：`OrderRepository`
- 推荐：`OrderRepositoryImpl`
- 推荐：`OrderMapper`
- 推荐：`OrderConverter`
- 推荐：`UserReadFacade`
- 推荐：`UserReadFacadeLocalImpl`
- 推荐：`UserReadFacadeRemoteImpl`
- 推荐：`VerificationCodeService`
- 避免：`OrderService`
- 避免：`InventoryService`
- 避免：`InventoryAuditCompensationService`
- 避免：看不出所在层次的名字
