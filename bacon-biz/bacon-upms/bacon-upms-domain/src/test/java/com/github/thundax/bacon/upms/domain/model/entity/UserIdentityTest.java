package com.github.thundax.bacon.upms.domain.model.entity;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import org.junit.jupiter.api.Test;

class UserIdentityTest {

    @Test
    void shouldAllowActiveIdentity() {
        UserIdentity identity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.ACTIVE);

        assertThatCode(identity::assertUsable).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDisabledIdentity() {
        UserIdentity identity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.DISABLED);

        assertThatThrownBy(identity::assertUsable)
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User identity is not usable");
    }

    @Test
    void shouldActivateAndDisableIdentity() {
        UserIdentity identity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.ACTIVE);

        identity.disable();
        org.assertj.core.api.Assertions.assertThat(identity.getStatus()).isEqualTo(UserIdentityStatus.DISABLED);

        identity.activate();
        org.assertj.core.api.Assertions.assertThat(identity.getStatus()).isEqualTo(UserIdentityStatus.ACTIVE);
    }

    @Test
    void shouldChangeAccount() {
        UserIdentity identity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.ACTIVE);

        identity.changeAccount("alice.new");

        org.assertj.core.api.Assertions.assertThat(identity.getIdentityValue()).isEqualTo("alice.new");
    }

    @Test
    void shouldChangePhone() {
        UserIdentity identity = UserIdentity.create(
                UserIdentityId.of(102L), UserId.of(201L), UserIdentityType.PHONE, "13800000001", UserIdentityStatus.ACTIVE);

        identity.changePhone("13900000002");

        org.assertj.core.api.Assertions.assertThat(identity.getIdentityValue()).isEqualTo("13900000002");
    }

    @Test
    void shouldRevokeIdentity() {
        UserIdentity identity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.ACTIVE);

        identity.revoke();

        org.assertj.core.api.Assertions.assertThat(identity.getStatus()).isEqualTo(UserIdentityStatus.DISABLED);
    }

    @Test
    void shouldMatchIdentityValue() {
        UserIdentity identity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.ACTIVE);

        org.assertj.core.api.Assertions.assertThat(identity.matches("alice")).isTrue();
        org.assertj.core.api.Assertions.assertThat(identity.matches("bob")).isFalse();
    }

    @Test
    void shouldReportIdentityTypePredicates() {
        UserIdentity usernameIdentity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.ACTIVE);
        UserIdentity phoneIdentity = UserIdentity.create(
                UserIdentityId.of(102L), UserId.of(201L), UserIdentityType.PHONE, "13800000001", UserIdentityStatus.ACTIVE);
        UserIdentity emailIdentity = UserIdentity.create(
                UserIdentityId.of(103L), UserId.of(201L), UserIdentityType.EMAIL, "alice@example.com", UserIdentityStatus.ACTIVE);

        org.assertj.core.api.Assertions.assertThat(usernameIdentity.isUsername()).isTrue();
        org.assertj.core.api.Assertions.assertThat(usernameIdentity.isPhone()).isFalse();
        org.assertj.core.api.Assertions.assertThat(usernameIdentity.isEmail()).isFalse();

        org.assertj.core.api.Assertions.assertThat(phoneIdentity.isPhone()).isTrue();
        org.assertj.core.api.Assertions.assertThat(phoneIdentity.isUsername()).isFalse();
        org.assertj.core.api.Assertions.assertThat(phoneIdentity.isEmail()).isFalse();

        org.assertj.core.api.Assertions.assertThat(emailIdentity.isEmail()).isTrue();
        org.assertj.core.api.Assertions.assertThat(emailIdentity.isUsername()).isFalse();
        org.assertj.core.api.Assertions.assertThat(emailIdentity.isPhone()).isFalse();
    }

    @Test
    void shouldReportWhetherIdentityCanLogin() {
        UserIdentity activeIdentity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.ACTIVE);
        UserIdentity disabledIdentity = UserIdentity.create(
                UserIdentityId.of(102L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.DISABLED);

        org.assertj.core.api.Assertions.assertThat(activeIdentity.canLogin()).isTrue();
        org.assertj.core.api.Assertions.assertThat(disabledIdentity.canLogin()).isFalse();
    }

    @Test
    void shouldRejectLoginWhenIdentityIsDisabled() {
        UserIdentity identity = UserIdentity.create(
                UserIdentityId.of(101L), UserId.of(201L), UserIdentityType.ACCOUNT, "alice", UserIdentityStatus.DISABLED);

        assertThatThrownBy(identity::assertLoginAllowed)
                .isInstanceOf(UpmsDomainException.class)
                .hasMessage("User identity login is not allowed");
    }
}
