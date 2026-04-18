package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserCredentialTest {

    @Test
    void shouldAllowActiveUnlockedUnexpiredCredential() {
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, Instant.parse("2099-01-01T00:00:00Z"));

        assertThatCode(() -> credential.assertVerifiable(Instant.parse("2026-01-01T00:00:00Z"))).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectInactiveCredential() {
        UserCredential credential = credential(UserCredentialStatus.DISABLED, null, null);

        assertThatThrownBy(() -> credential.assertVerifiable(Instant.parse("2026-01-01T00:00:00Z")))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User credential is not active");
    }

    @Test
    void shouldRejectLockedCredential() {
        UserCredential credential = credential(
                UserCredentialStatus.ACTIVE,
                Instant.parse("2026-01-02T00:00:00Z"),
                Instant.parse("2099-01-01T00:00:00Z"));

        assertThatThrownBy(() -> credential.assertVerifiable(Instant.parse("2026-01-01T00:00:00Z")))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User credential is locked");
    }

    @Test
    void shouldRejectCredentialAlreadyMarkedLocked() {
        UserCredential credential = UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                "{noop}secret",
                UserCredentialStatus.LOCKED,
                false,
                0,
                5,
                "bad password",
                null,
                Instant.parse("2099-01-01T00:00:00Z"),
                null);

        assertThatThrownBy(() -> credential.assertVerifiable(Instant.parse("2026-01-01T00:00:00Z")))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User credential is locked");
    }

    @Test
    void shouldRejectExpiredCredential() {
        UserCredential credential = credential(
                UserCredentialStatus.ACTIVE, null, Instant.parse("2025-12-31T23:59:59Z"));

        assertThatThrownBy(() -> credential.assertVerifiable(Instant.parse("2026-01-01T00:00:00Z")))
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User credential is expired");
    }

    @Test
    void shouldResetFailureStateWhenMarkedVerified() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        UserCredential credential = UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                "{noop}secret",
                UserCredentialStatus.LOCKED,
                false,
                3,
                5,
                "bad password",
                Instant.parse("2026-01-02T00:00:00Z"),
                Instant.parse("2099-01-01T00:00:00Z"),
                null);

        credential.markVerified(now);

        assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
        assertThat(credential.getFailedCount()).isZero();
        assertThat(credential.getLockReason()).isNull();
        assertThat(credential.getLockedUntil()).isNull();
        assertThat(credential.getLastVerifiedAt()).isEqualTo(now);
    }

    @Test
    void shouldIncreaseFailedCountWhenMarkedFailed() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, Instant.parse("2099-01-01T00:00:00Z"));

        credential.markFailed("bad password", now);

        assertThat(credential.getFailedCount()).isEqualTo(1);
        assertThat(credential.getLockReason()).isEqualTo("bad password");
        assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
    }

    @Test
    void shouldLockCredentialWhenFailedCountReachesLimit() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        UserCredential credential = UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                "{noop}secret",
                UserCredentialStatus.ACTIVE,
                false,
                4,
                5,
                null,
                null,
                Instant.parse("2099-01-01T00:00:00Z"),
                null);

        credential.markFailed("bad password", now);

        assertThat(credential.getFailedCount()).isEqualTo(5);
        assertThat(credential.getLockReason()).isEqualTo("bad password");
        assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.LOCKED);
    }

    @Test
    void shouldLockAndUnlockCredentialExplicitly() {
        Instant until = Instant.parse("2026-01-02T00:00:00Z");
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, Instant.parse("2099-01-01T00:00:00Z"));

        credential.lock("admin locked", until);
        assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.LOCKED);
        assertThat(credential.getLockReason()).isEqualTo("admin locked");
        assertThat(credential.getLockedUntil()).isEqualTo(until);

        credential.unlock();
        assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
        assertThat(credential.getLockReason()).isNull();
        assertThat(credential.getLockedUntil()).isNull();
    }

    @Test
    void shouldActivateAndDisableCredential() {
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, Instant.parse("2099-01-01T00:00:00Z"));

        credential.disable();
        assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.DISABLED);

        credential.activate();
        assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
    }

    @Test
    void shouldReportWhetherCredentialIsExpired() {
        UserCredential credential = credential(
                UserCredentialStatus.ACTIVE, null, Instant.parse("2026-01-01T00:00:00Z"));

        assertThat(credential.isExpired(Instant.parse("2025-12-31T23:59:59Z"))).isFalse();
        assertThat(credential.isExpired(Instant.parse("2026-01-01T00:00:00Z"))).isTrue();
        assertThat(credential.isExpired(Instant.parse("2026-01-01T00:00:01Z"))).isTrue();
    }

    @Test
    void shouldSetAndClearExpiry() {
        Instant expiresAt = Instant.parse("2026-01-01T00:00:00Z");
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, null);

        credential.expireAt(expiresAt);
        assertThat(credential.getExpiresAt()).isEqualTo(expiresAt);

        credential.clearExpiry();
        assertThat(credential.getExpiresAt()).isNull();
    }

    @Test
    void shouldSetAndClearPasswordChangeRequirement() {
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, null);

        credential.requirePasswordChange();
        assertThat(credential.isNeedChangePassword()).isTrue();

        credential.clearPasswordChangeRequirement();
        assertThat(credential.isNeedChangePassword()).isFalse();
    }

    @Test
    void shouldRejectPasswordChangeRequirementForNonPasswordCredential() {
        UserCredential credential = UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.TOTP,
                UserCredentialFactorLevel.PRIMARY,
                "123456",
                UserCredentialStatus.ACTIVE,
                false,
                0,
                5,
                null,
                null,
                null,
                null);

        assertThatThrownBy(() -> credential.requirePasswordChange())
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User credential password change requirement is only supported for password credentials");
        assertThatThrownBy(() -> credential.clearPasswordChangeRequirement())
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User credential password change requirement is only supported for password credentials");
    }

    @Test
    void shouldReplaceCredentialValue() {
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, null);

        credential.replaceCredentialValue("{noop}new-secret");

        assertThat(credential.getCredentialValue()).isEqualTo("{noop}new-secret");
    }

    @Test
    void shouldBindIdentity() {
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, null);

        credential.bindIdentity(UserIdentityId.of(302L));

        assertThat(credential.getIdentityId()).isEqualTo(UserIdentityId.of(302L));
    }

    @Test
    void shouldReplacePasswordAndResetCredentialState() {
        UserCredential credential = UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                "{noop}old-secret",
                UserCredentialStatus.LOCKED,
                false,
                4,
                5,
                "bad password",
                Instant.parse("2026-01-02T00:00:00Z"),
                Instant.parse("2026-03-01T00:00:00Z"),
                Instant.parse("2025-12-31T00:00:00Z"));

        credential.replacePassword("{noop}new-secret", true, Instant.parse("2026-06-01T00:00:00Z"));

        assertThat(credential.getCredentialValue()).isEqualTo("{noop}new-secret");
        assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
        assertThat(credential.isNeedChangePassword()).isTrue();
        assertThat(credential.getFailedCount()).isZero();
        assertThat(credential.getLockReason()).isNull();
        assertThat(credential.getLockedUntil()).isNull();
        assertThat(credential.getExpiresAt()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
        assertThat(credential.getLastVerifiedAt()).isEqualTo(Instant.parse("2025-12-31T00:00:00Z"));
    }

    @Test
    void shouldReportStatusPredicates() {
        UserCredential activeCredential = credential(UserCredentialStatus.ACTIVE, null, null);
        UserCredential disabledCredential = credential(UserCredentialStatus.DISABLED, null, null);

        assertThat(activeCredential.isActive()).isTrue();
        assertThat(activeCredential.isDisabled()).isFalse();
        assertThat(disabledCredential.isActive()).isFalse();
        assertThat(disabledCredential.isDisabled()).isTrue();
    }

    @Test
    void shouldReportWhetherCredentialIsLocked() {
        UserCredential lockedCredential = UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                "{noop}secret",
                UserCredentialStatus.LOCKED,
                false,
                0,
                5,
                "bad password",
                Instant.parse("2026-01-02T00:00:00Z"),
                null,
                null);
        UserCredential unlockedCredential = credential(UserCredentialStatus.ACTIVE, null, null);

        assertThat(lockedCredential.isLocked(Instant.parse("2026-01-01T00:00:00Z"))).isTrue();
        assertThat(lockedCredential.isLocked(Instant.parse("2026-01-03T00:00:00Z"))).isFalse();
        assertThat(unlockedCredential.isLocked(Instant.parse("2026-01-01T00:00:00Z"))).isFalse();
    }

    @Test
    void shouldReportCredentialTypeAndPasswordChangeRequirement() {
        UserCredential passwordCredential = credential(UserCredentialStatus.ACTIVE, null, null);
        UserCredential totpCredential = UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.TOTP,
                UserCredentialFactorLevel.PRIMARY,
                "123456",
                UserCredentialStatus.ACTIVE,
                false,
                0,
                5,
                null,
                null,
                null,
                null);

        passwordCredential.requirePasswordChange();

        assertThat(passwordCredential.isPasswordCredential()).isTrue();
        assertThat(passwordCredential.needsPasswordChange()).isTrue();
        assertThat(totpCredential.isPasswordCredential()).isFalse();
        assertThat(totpCredential.needsPasswordChange()).isFalse();
    }

    @Test
    void shouldMatchFactorLevel() {
        UserCredential credential = credential(UserCredentialStatus.ACTIVE, null, null);

        assertThat(credential.matchesFactorLevel(UserCredentialFactorLevel.PRIMARY)).isTrue();
        assertThat(credential.matchesFactorLevel(UserCredentialFactorLevel.SECONDARY)).isFalse();
    }

    @Test
    void shouldReportWhetherCredentialCanBeUsedForAuthentication() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        UserCredential activeCredential =
                credential(UserCredentialStatus.ACTIVE, null, Instant.parse("2099-01-01T00:00:00Z"));
        UserCredential lockedCredential = UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                "{noop}secret",
                UserCredentialStatus.LOCKED,
                false,
                0,
                5,
                "bad password",
                Instant.parse("2026-01-02T00:00:00Z"),
                Instant.parse("2099-01-01T00:00:00Z"),
                null);
        UserCredential expiredCredential =
                credential(UserCredentialStatus.ACTIVE, null, Instant.parse("2025-12-31T23:59:59Z"));
        UserCredential disabledCredential = credential(UserCredentialStatus.DISABLED, null, null);

        assertThat(activeCredential.canBeUsedForAuthentication(now)).isTrue();
        assertThat(lockedCredential.canBeUsedForAuthentication(now)).isFalse();
        assertThat(expiredCredential.canBeUsedForAuthentication(now)).isFalse();
        assertThat(disabledCredential.canBeUsedForAuthentication(now)).isFalse();
    }

    private static UserCredential credential(UserCredentialStatus status, Instant lockedUntil, Instant expiresAt) {
        return UserCredential.create(
                UserCredentialId.of(101L),
                UserId.of(201L),
                UserIdentityId.of(301L),
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                "{noop}secret",
                status,
                false,
                0,
                5,
                null,
                lockedUntil,
                expiresAt,
                null);
    }
}
