# AUTH LOGIN DESIGN

## 1. Purpose

本文档定义 `Auth` 域中用户/密码登录的固定设计。  
本设计要求密码登录必须同时经过验证码校验与 `RSA` 非对称解密。  
本文档直接指导 `mono-app` 与微服务模式下的统一实现。

## 2. Scope

当前范围：

- 用户/密码登录挑战获取
- 登录验证码校验
- 登录密码 `RSA` 公钥下发与私钥缓存
- 登录密码 `BCrypt` 验证
- `Auth` 调用 `UPMS` 读取登录凭据
- `mono-app` 与微服务双模式装配

不在当前范围：

- 短信登录改造
- 第三方登录改造
- 图形验证码图片生成与前端渲染
- `OAuth2` 协议端点改造

## 3. Bounded Context

- `Auth` 负责登录流程编排、验证码消费、`RSA` 私钥管理、会话签发、认证审计
- `UPMS` 负责用户主数据、身份标识、密码哈希主数据
- `common-core` 负责通用 `RSA` 加解密服务、验证码服务、`PasswordEncoder` 装配

职责边界固定如下：

- `Auth` 不持有用户密码哈希主数据
- `Auth` 不直接访问 `UPMS` 仓储实现
- `Auth` 只能通过 `UserReadFacade` 读取登录所需凭据
- `UPMS` 不参与验证码校验与登录会话签发

## 4. Module Mapping

- `bacon-common/bacon-common-core`
  - `RsaCryptoService`
  - `PasswordEncoderConfiguration`
  - `VerificationCodeService`
- `bacon-biz/bacon-upms/bacon-upms-api`
  - `UserReadFacade#getUserLoginCredential`
  - `UserLoginCredentialDTO`
- `bacon-biz/bacon-upms/bacon-upms-application`
  - 组装用户登录凭据读模型
- `bacon-biz/bacon-upms/bacon-upms-interfaces`
  - `mono-app` 模式下 `UserReadFacadeLocalImpl`
  - `Provider` 查询接口
- `bacon-biz/bacon-upms/bacon-upms-infra`
  - 微服务模式下 `UserReadFacadeRemoteImpl`
  - 演示用密码哈希种子
- `bacon-biz/bacon-auth/bacon-auth-application`
  - 密码登录挑战签发
  - 验证码校验
  - `RSA` 解密
  - `BCrypt` 验密
  - 登录会话签发
- `bacon-biz/bacon-auth/bacon-auth-interfaces`
  - 登录挑战接口
  - 密码登录接口

## 5. Core Domain Objects

### 5.1 Fixed Request Contracts

`PasswordLoginChallengeRequest`

- 当前不需要业务字段

`PasswordLoginRequest`

- `tenantId`
- `account`
- `password`
- `rsaKeyId`
- `captchaKey`
- `captchaCode`

说明：

- `password` 固定表示使用 `RSA` 公钥加密后的 `Base64` 密文
- 明文密码禁止进入 `Auth` HTTP 接口

### 5.2 Fixed Response Contracts

`PasswordLoginChallengeResponse`

- `captchaKey`
- `captchaCode`
- `captchaExpiresIn`
- `rsaKeyId`
- `rsaPublicKey`
- `rsaExpiresIn`

说明：

- 当前实现为后端联调与演示阶段，直接返回 `captchaCode`
- 后续接入图形验证码时，保留 `captchaKey` 语义，替换 `captchaCode` 的展示方式

`UserLoginCredentialDTO`

- `tenantId`
- `userId`
- `account`
- `phone`
- `status`
- `deleted`
- `identityType`
- `identityValue`
- `identityEnabled`
- `passwordHash`

## 6. Global Constraints

- 用户/密码登录固定要求验证码校验通过后才允许进入密码验密
- 用户/密码登录固定要求密码使用 `RSA` 公钥加密后传输
- `RSA` 密钥对固定为一次性登录挑战级别，不允许长时间复用
- `RSA` 私钥只允许保存在 `Auth` 侧缓存，不允许下发到客户端
- 验证码固定为一次性消费，校验成功后立即删除
- `RSA` 私钥固定为一次性消费，解密后立即删除
- 用户密码哈希算法固定为 `BCrypt`
- `BCrypt cost factor` 固定为 `12`
- 密码明文禁止进入日志、审计日志、异常消息
- `Auth` 与 `UPMS` 的跨域调用固定依赖 `UserReadFacade`

## 7. Functional Requirements

### 7.1 Password Login Challenge

功能对象：

- 密码登录挑战

功能能力：

- `Auth` 提供 `POST /login/password/challenge`
- 每次调用同时签发一个验证码挑战与一个 `RSA` 公钥
- 返回 `captchaKey` 与 `rsaKeyId` 作为后续登录请求的关联键

必要补充约束：

- `captchaKey` 固定使用 `UUID`
- `rsaKeyId` 固定使用 `UUID`
- `rsaPublicKey` 固定为 `X.509 + Base64` 编码
- `RSA` 位数固定为 `2048`

### 7.2 Password Login Verification

功能对象：

- 用户/密码登录

功能能力：

- `Auth` 接收 `tenantId + account + password + rsaKeyId + captchaKey + captchaCode`
- 先校验并消费验证码
- 再读取缓存中的 `RSA` 私钥并解密密码
- 再通过 `UserReadFacade#getUserLoginCredential` 获取用户凭据
- 最后执行 `BCrypt` 验密并签发会话

必要补充约束：

- 验证码错误时直接失败
- `RSA` 私钥不存在、过期、解密失败时直接失败
- 身份标识不存在、未启用、用户停用、用户已删除时直接失败
- 密码哈希不匹配时直接失败

### 7.3 Cross-Domain Read Contract

功能对象：

- 登录凭据读取

功能能力：

- `Auth` 固定调用 `UserReadFacade#getUserLoginCredential`
- `mono-app` 模式由 `UserReadFacadeLocalImpl` 直接调用 `UserApplicationService`
- 微服务模式由 `UserReadFacadeRemoteImpl` 调用 `UPMS Provider` HTTP 接口

必要补充约束：

- 固定按 `tenantId + identityType + identityValue` 查询
- 当前账号密码登录固定使用 `identityType = ACCOUNT`
- `DTO` 契约在 `mono-app` 与微服务模式下保持一致

## 8. Key Flows

### 8.1 获取登录挑战

1. 客户端调用 `POST /login/password/challenge`
2. `Auth` 生成验证码并写入 `VerificationCodeService`
3. `Auth` 生成 `RSA` 密钥对
4. `Auth` 将私钥写入缓存
5. `Auth` 返回 `captchaKey`、`captchaCode`、`rsaKeyId`、`rsaPublicKey`

### 8.2 用户/密码登录

1. 客户端使用 `rsaPublicKey` 加密密码
2. 客户端提交 `PasswordLoginRequest`
3. `Auth` 校验并消费验证码
4. `Auth` 读取并消费缓存中的 `RSA` 私钥
5. `Auth` 解密得到明文密码
6. `Auth` 通过 `UserReadFacade` 获取 `UserLoginCredentialDTO`
7. `Auth` 校验身份启用状态、用户状态、删除状态
8. `Auth` 使用 `PasswordEncoder` 执行 `BCrypt` 验密
9. `Auth` 创建 `AuthSession` 与 `RefreshTokenSession`
10. `Auth` 返回 `UserLoginResponse`

### 8.3 Mono And Micro Support

`mono-app`

1. `Auth` Application
2. `UserReadFacade`
3. `UserReadFacadeLocalImpl`
4. `UPMS` Application

微服务

1. `Auth` Application
2. `UserReadFacade`
3. `UserReadFacadeRemoteImpl`
4. `/providers/upms/user-credentials`
5. `UPMS` Application

## 9. Cache Rules

### 9.1 Verification Code Cache

- 缓存键：`verificationCode:LOGIN_PASSWORD_CAPTCHA:{captchaKey}`
- 缓存内容：验证码明文
- `TTL`：`300s`
- 失效触发：
  - 登录成功前验证码校验通过后立即删除
  - 超时自动失效

### 9.2 RSA Private Key Cache

- 缓存键：`loginPasswordPrivateKey:{rsaKeyId}`
- 缓存内容：`PKCS8 + Base64` 私钥
- `TTL`：`300s`
- 失效触发：
  - 密码解密后立即删除
  - 超时自动失效

## 10. Security Rules

- 密码明文仅允许存在于 `Auth` 服务内存中的瞬时变量
- 密码明文禁止落库
- 密码明文禁止进入缓存
- 密码明文禁止进入日志与审计日志
- 验证码错误与密码错误对外统一返回通用失败信息
- `RSA` 私钥缓存与验证码缓存必须使用不同键空间

## 11. Non-Functional Requirements

- 登录挑战接口与密码登录接口均应支持 `mono-app` 与微服务模式
- `UserReadFacade` 在两种运行模式下返回的字段语义必须一致
- 登录实现不允许绕过分层直接访问他域基础设施实现

## 12. Open Items

无
