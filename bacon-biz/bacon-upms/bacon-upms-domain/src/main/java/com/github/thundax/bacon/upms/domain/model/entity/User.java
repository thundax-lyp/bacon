package com.github.thundax.bacon.upms.domain.model.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户领域实体。
 */
@Getter
public class User {

    /** 用户主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 登录账号。 */
    private String account;
    /** 用户名称。 */
    private String name;
    /** 头像对象主键。 */
    private Long avatarObjectId;
    /** 手机号。 */
    private String phone;
    /** 密码哈希。 */
    private String passwordHash;
    /** 所属部门主键。 */
    private Long departmentId;
    /** 用户状态。 */
    private String status;
    /** 逻辑删除标记。 */
    private boolean deleted;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;

    public User(Long id, Long tenantId, String account, String name, String phone, String passwordHash,
                Long departmentId, String status, boolean deleted) {
        this(id, tenantId, account, name, null, phone, passwordHash, departmentId, status, deleted);
    }

    public User(Long id, Long tenantId, String account, String name, Long avatarObjectId, String phone,
                String passwordHash, Long departmentId, String status, boolean deleted) {
        this(id, tenantId, account, name, avatarObjectId, phone, passwordHash, departmentId, status, deleted,
                null, null, null, null);
    }

    public User(Long id, Long tenantId, String account, String name, String phone, String passwordHash,
                Long departmentId, String status, boolean deleted, String createdBy, LocalDateTime createdAt,
                String updatedBy, LocalDateTime updatedAt) {
        this(id, tenantId, account, name, null, phone, passwordHash, departmentId, status, deleted,
                createdBy, createdAt, updatedBy, updatedAt);
    }

    public User(Long id, Long tenantId, String account, String name, Long avatarObjectId, String phone,
                String passwordHash, Long departmentId, String status, boolean deleted, String createdBy,
                LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.account = account;
        this.name = name;
        this.avatarObjectId = avatarObjectId;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.departmentId = departmentId;
        this.status = status;
        this.deleted = deleted;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
