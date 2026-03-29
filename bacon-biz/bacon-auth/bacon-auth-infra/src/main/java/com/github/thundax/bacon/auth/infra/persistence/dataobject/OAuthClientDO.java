package com.github.thundax.bacon.auth.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_auth_oauth_client")
public class OAuthClientDO {

    private Long id;
    @TableField("client_id")
    private String clientId;
    @TableField("client_secret_hash")
    private String clientSecretHash;
    @TableField("client_name")
    private String clientName;
    @TableField("client_type")
    private String clientType;
    @TableField("grant_types")
    private String grantTypes;
    @TableField("scopes")
    private String scopes;
    @TableField("redirect_uris")
    private String redirectUris;
    @TableField("access_token_ttl_seconds")
    private Long accessTokenTtlSeconds;
    @TableField("refresh_token_ttl_seconds")
    private Long refreshTokenTtlSeconds;
    private Boolean enabled;
    private String contact;
    private String remark;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
}
