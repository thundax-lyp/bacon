# Architecture Reference

本文件存放低频、长篇、可按需读取的结构参考信息。

## Top-Level Layout

```text
bacon
├── pom.xml
├── bacon-app/
├── bacon-biz/
├── bacon-common/
├── deploy/
└── docs/
```

## Business Domain Layout

```text
bacon-biz/<domain>
├── <domain>-api
├── <domain>-interfaces
├── <domain>-application
├── <domain>-domain
└── <domain>-infra
```

## Standard In-Domain Package Layout

```text
application/
├── assembler
├── audit
├── command
├── executor
├── query
└── support

domain/
├── model
├── repository
└── service

interfaces/
├── controller
├── provider
├── facade
└── response

infra/
├── facade/remote
├── persistence
└── repository/impl
```

## Runtime Assembly

单体模式：

`application -> Facade -> LocalImpl -> peer application`

微服务模式：

`application -> Facade -> RemoteImpl -> remote service`

## Notes

- 这个文件不是实现红线文件。
- 实现前优先读 `ARCHITECTURE.md`。
- 只有当你需要目录树、装配示例、模块位置参考时，再读本文件。
