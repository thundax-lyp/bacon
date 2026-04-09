package com.github.thundax.bacon.auth.domain.model.entity;

import com.github.thundax.bacon.auth.domain.model.enums.ClientStatus;
import com.github.thundax.bacon.auth.domain.model.valueobject.ClientCode;
import com.github.thundax.bacon.auth.domain.model.valueobject.ClientId;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 客户端领域实体。
 */
@Getter
@AllArgsConstructor
public class OAuthClient {

    /** 客户端主键。 */
    private ClientId id;
    /** 客户端标识。 */
    private ClientCode clientCode;
    /** 客户端密钥。 */
    private String clientSecret;
    /** 客户端名称。 */
    private String clientName;
    /** 客户端类型。 */
    private String clientType;
    /** 支持的授权类型集合。 */
    private Set<String> grantTypes;
    /** 支持的授权范围集合。 */
    private Set<String> scopes;
    /** 回调地址集合。 */
    private Set<String> redirectUris;
    /** 访问令牌有效期秒数。 */
    private long accessTokenTtlSeconds;
    /** 刷新令牌有效期秒数。 */
    private long refreshTokenTtlSeconds;
    /** 启用标记。 */
    private ClientStatus enabled;
    /** 联系方式。 */
    private String contact;
    /** 客户端备注。 */
    private String remark;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public OAuthClient(
            Long id,
            String clientId,
            String clientSecret,
            String clientName,
            String clientType,
            List<String> grantTypes,
            List<String> scopes,
            List<String> redirectUris,
            Long accessTokenTtlSeconds,
            Long refreshTokenTtlSeconds,
            ClientStatus enabled,
            String contact,
            String remark,
            Instant createdAt,
            Instant updatedAt) {
        this(
                id == null ? null : ClientId.of(id),
                clientId == null ? null : ClientCode.of(clientId),
                clientSecret,
                clientName,
                clientType,
                toLinkedHashSet(grantTypes),
                toLinkedHashSet(scopes),
                toLinkedHashSet(redirectUris),
                accessTokenTtlSeconds == null ? 0L : accessTokenTtlSeconds,
                refreshTokenTtlSeconds == null ? 0L : refreshTokenTtlSeconds,
                enabled,
                contact,
                remark,
                createdAt,
                updatedAt);
    }

    private static Set<String> toLinkedHashSet(List<String> values) {
        return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values);
    }

    public Long getIdValue() {
        return id == null ? null : id.value();
    }

    public String getClientCodeValue() {
        return clientCode == null ? null : clientCode.value();
    }

    public String getEnabledValue() {
        return enabled == null ? null : enabled.value();
    }

    public boolean isEnabled() {
        return ClientStatus.ENABLED == enabled;
    }
}
