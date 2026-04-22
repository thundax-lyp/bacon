# API Annotation Matrix

## 1. Purpose

定义后台接口注解目标规范。  
要求：可被 AI 稳定识别、可自动检测、可 CI 门禁。  
本规范以目标态为准，不以存量现状为准。

## 2. Scope

1. `interfaces.controller`（BFF）
2. `interfaces.controller` 中回调接口（Callback）
3. `interfaces.provider`（Provider）
4. `api.facade`（跨域契约）

## 3. Selector

1. `BFF_SELECTOR`：`..interfaces.controller..` 且不命中 `CALLBACK_SELECTOR`
2. `CALLBACK_SELECTOR`：满足任一条件
- 类级 `@RequestMapping` 路径包含 `callback`
- 方法级 `@*Mapping` 路径包含 `callback`
- 类名以 `CallbackController` 结尾
3. `PROVIDER_SELECTOR`：`..interfaces.provider..`
4. `FACADE_SELECTOR`：`..api.facade..`

## 4. Exception Buckets

1. `EXC_AUTH_PUBLIC`：登录、换 token、会话刷新等公开入口
2. `EXC_OAUTH2_PROTOCOL`：OAuth2 协议端点
3. `EXC_CALLBACK_ENDPOINT`：外部回调端点

注解约定：

`@ApiAnnotationException(bucket = ApiAnnotationExceptionBucket.XXX)`

`ApiAnnotationExceptionBucket` 仅允许：

1. `AUTH_PUBLIC`
2. `OAUTH2_PROTOCOL`
3. `CALLBACK_ENDPOINT`

## 5. Rule Format

每条规则固定字段：

1. `Rule ID`
2. `Scope`
3. `Constraint`
4. `Detection`
5. `Violation Message`

固定报错格式：

`[RuleID] <scope> violates <constraint>: <found>`

## 6. Hard Rules

| Rule ID | Scope | Constraint | Detection | Violation Message |
| --- | --- | --- | --- | --- |
| `ANNO_COMMON_CLASS_BASE_REQUIRED` | `interfaces.controller` + `interfaces.provider` 入口类 | 必须同时声明 `@RestController @RequestMapping @Validated @Tag` | ArchUnit（`rg` 兜底） | `[ANNO_COMMON_CLASS_BASE_REQUIRED] <class> violates class base annotations required: <missingAnnotations>` |
| `ANNO_COMMON_METHOD_MAPPING_REQUIRED` | 上述入口类中的 HTTP 方法 | 必须且仅能有一个 HTTP 映射注解（`@Get/@Post/@Put/@Delete/@PatchMapping`） | ArchUnit | `[ANNO_COMMON_METHOD_MAPPING_REQUIRED] <class#method> violates method mapping required: <foundMappings>` |
| `ANNO_COMMON_REQUEST_PARAM_VALID_REQUIRED` | 上述入口类中 HTTP 方法的 `interfaces.request.*Request` 参数 | 必须显式声明 `@Valid`，触发 Bean Validation | ArchUnit | `[ANNO_COMMON_REQUEST_PARAM_VALID_REQUIRED] <class#method> violates request parameter Valid required: <parameterType>` |
| `ANNO_BFF_CLASS_WRAPPED_REQUIRED` | `BFF_SELECTOR` | 必须声明 `@WrappedApiController` | ArchUnit | `[ANNO_BFF_CLASS_WRAPPED_REQUIRED] <class> violates WrappedApiController required: <foundAnnotations>` |
| `ANNO_BFF_METHOD_OPERATION_REQUIRED` | `BFF_SELECTOR` 中 HTTP 方法 | 必须声明 `@Operation` | ArchUnit | `[ANNO_BFF_METHOD_OPERATION_REQUIRED] <class#method> violates Operation required: <foundAnnotations>` |
| `ANNO_BFF_PERMISSION_REQUIRED` | `BFF_SELECTOR` 中 HTTP 方法，排除标注 `@ApiAnnotationException(bucket in {AUTH_PUBLIC,OAUTH2_PROTOCOL,CALLBACK_ENDPOINT})` 的类/方法 | 必须声明 `@HasPermission` | ArchUnit | `[ANNO_BFF_PERMISSION_REQUIRED] <class#method> violates HasPermission required: <foundAnnotations>` |
| `ANNO_CALLBACK_OPERATION_REQUIRED` | `CALLBACK_SELECTOR` 中 HTTP 方法 | 必须声明 `@Operation` | ArchUnit | `[ANNO_CALLBACK_OPERATION_REQUIRED] <class#method> violates Operation required: <foundAnnotations>` |
| `ANNO_CALLBACK_PERMISSION_FORBIDDEN` | `CALLBACK_SELECTOR` 中 HTTP 方法 | 禁止 `@HasPermission` | ArchUnit | `[ANNO_CALLBACK_PERMISSION_FORBIDDEN] <class#method> violates HasPermission forbidden: <foundAnnotations>` |
| `ANNO_CALLBACK_SYSLOG_FORBIDDEN` | `CALLBACK_SELECTOR` 中 HTTP 方法 | 禁止 `@SysLog` | ArchUnit | `[ANNO_CALLBACK_SYSLOG_FORBIDDEN] <class#method> violates SysLog forbidden: <foundAnnotations>` |
| `ANNO_PROVIDER_PATH_PREFIX_REQUIRED` | `PROVIDER_SELECTOR` | 类级 `@RequestMapping` 必须以 `/providers/` 开头 | ArchUnit | `[ANNO_PROVIDER_PATH_PREFIX_REQUIRED] <class> violates provider path prefix required: <foundPath>` |
| `ANNO_PROVIDER_METHOD_OPERATION_REQUIRED` | `PROVIDER_SELECTOR` 中 HTTP 方法 | 必须声明 `@Operation` | ArchUnit | `[ANNO_PROVIDER_METHOD_OPERATION_REQUIRED] <class#method> violates Operation required: <foundAnnotations>` |
| `ANNO_PROVIDER_PERMISSION_FORBIDDEN` | `PROVIDER_SELECTOR` 中 HTTP 方法 | 禁止 `@HasPermission` | ArchUnit | `[ANNO_PROVIDER_PERMISSION_FORBIDDEN] <class#method> violates HasPermission forbidden: <foundAnnotations>` |
| `ANNO_FACADE_ENDPOINT_ANNOTATION_FORBIDDEN` | `FACADE_SELECTOR` 类与方法 | 禁止端点注解：`@RestController @RequestMapping @Get/@Post/@Put/@Delete/@PatchMapping` | ArchUnit | `[ANNO_FACADE_ENDPOINT_ANNOTATION_FORBIDDEN] <typeOrMethod> violates endpoint annotation forbidden: <foundAnnotations>` |
| `ANNO_FACADE_SECURITY_ANNOTATION_FORBIDDEN` | `FACADE_SELECTOR` 类与方法 | 禁止安全审计注解：`@HasPermission @SysLog` | ArchUnit | `[ANNO_FACADE_SECURITY_ANNOTATION_FORBIDDEN] <typeOrMethod> violates security annotation forbidden: <foundAnnotations>` |
| `ANNO_EXCEPTION_ANNOTATION_SCOPE_REQUIRED` | 声明 `@ApiAnnotationException` 的类/方法 | 仅允许位于 `interfaces.controller` 或 `interfaces.provider` | ArchUnit | `[ANNO_EXCEPTION_ANNOTATION_SCOPE_REQUIRED] <typeOrMethod> violates ApiAnnotationException scope required: <foundPackage>` |
| `ANNO_EXCEPTION_ANNOTATION_BUCKET_ENUM_REQUIRED` | 声明 `@ApiAnnotationException` 的类/方法 | `bucket` 必须是 `AUTH_PUBLIC/OAUTH2_PROTOCOL/CALLBACK_ENDPOINT` 之一 | 编译期枚举约束 + ArchUnit 反射校验 | `[ANNO_EXCEPTION_ANNOTATION_BUCKET_ENUM_REQUIRED] <typeOrMethod> violates ApiAnnotationException bucket enum required: <foundBucket>` |
| `ANNO_EXCEPTION_ANNOTATION_APPLIES_RULES_ONLY` | 声明 `@ApiAnnotationException` 的类/方法 | 仅对授权相关规则生效，不豁免路径/映射/Operation 等硬规则 | ArchUnit | `[ANNO_EXCEPTION_ANNOTATION_APPLIES_RULES_ONLY] <typeOrMethod> violates ApiAnnotationException applies-rules-only constraint: <foundBypassedRule>` |

## 7. Minimal Matrix

| Interface Type | Required | Forbidden |
| --- | --- | --- |
| BFF | `@RestController @RequestMapping @Validated @Tag @WrappedApiController`；方法级 `@Operation` + HTTP Mapping；管理接口 `@HasPermission`；`interfaces.request.*Request` 参数声明 `@Valid` | 无 |
| Callback | `@RestController @RequestMapping @Validated @Tag`；方法级 `@Operation` + HTTP Mapping；`interfaces.request.*Request` 参数声明 `@Valid` | `@HasPermission @SysLog` |
| Provider | `@RestController @RequestMapping(/providers/**) @Validated @Tag`；方法级 `@Operation` + HTTP Mapping；`interfaces.request.*Request` 参数声明 `@Valid` | `@HasPermission` |
| Facade | 无 | 端点注解、安全审计注解 |

## 8. CI Gate

1. `Hard Rules` 必须有自动化检测实现（ArchUnit 或等价）
2. PR 涉及接口入口变更时必须全量通过
3. 违规变更禁止合并
4. 豁免必须通过 `@ApiAnnotationException(bucket=...)` 声明
