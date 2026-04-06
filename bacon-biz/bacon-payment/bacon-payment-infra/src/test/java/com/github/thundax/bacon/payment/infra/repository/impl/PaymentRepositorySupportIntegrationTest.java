package com.github.thundax.bacon.payment.infra.repository.impl;

import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.PaymentOrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditOperatorType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentNo;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentAuditLogMapper;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentCallbackRecordMapper;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentOrderMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import javax.sql.DataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentRepositorySupportIntegrationTest {

    private final AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(TestConfig.class);
    private final DataSource dataSource = context.getBean(DataSource.class);
    private final PaymentRepositorySupport paymentRepositorySupport = context.getBean(PaymentRepositorySupport.class);

    @BeforeEach
    void setUpSchema() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS bacon_payment_audit_log");
            statement.execute("DROP TABLE IF EXISTS bacon_payment_callback_record");
            statement.execute("DROP TABLE IF EXISTS bacon_payment_order");

            statement.execute("""
                    CREATE TABLE bacon_payment_order (
                        id varchar(64) NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        payment_no varchar(64) NOT NULL,
                        order_no varchar(64) NOT NULL,
                        user_id varchar(64) NOT NULL,
                        channel_code varchar(32) NOT NULL,
                        payment_status varchar(16) NOT NULL,
                        amount decimal(18,2) NOT NULL,
                        paid_amount decimal(18,2) NOT NULL DEFAULT 0.00,
                        subject varchar(255) NOT NULL,
                        created_at timestamp(3) NOT NULL,
                        updated_at timestamp(3) NOT NULL,
                        expired_at timestamp(3) NOT NULL,
                        paid_at timestamp(3) NULL,
                        closed_at timestamp(3) NULL,
                        PRIMARY KEY (id),
                        UNIQUE (payment_no),
                        UNIQUE (order_no)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE bacon_payment_callback_record (
                        id bigint NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        payment_no varchar(64) NOT NULL,
                        order_no varchar(64) NOT NULL,
                        channel_code varchar(32) NOT NULL,
                        channel_transaction_no varchar(128) NULL,
                        channel_status varchar(64) NOT NULL,
                        raw_payload varchar(2000) NOT NULL,
                        received_at timestamp(3) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);

            statement.execute("""
                    CREATE TABLE bacon_payment_audit_log (
                        id bigint NOT NULL,
                        tenant_id varchar(64) NOT NULL,
                        payment_no varchar(64) NOT NULL,
                        action_type varchar(64) NOT NULL,
                        before_status varchar(16) NULL,
                        after_status varchar(16) NULL,
                        operator_type varchar(32) NULL,
                        operator_id varchar(64) NULL,
                        occurred_at timestamp(3) NOT NULL,
                        PRIMARY KEY (id)
                    )
                    """);
        }
    }

    @Test
    void shouldPersistAndReadBackOrderCallbackAndAuditLog() {
        PaymentOrder paymentOrder = new PaymentOrder(null, TenantId.of("1001"), PaymentNo.of("PAY-IT-10001"), OrderNo.of("ORD-IT-10001"),
                UserId.of("2001"),
                PaymentChannelCode.MOCK, Money.of(new BigDecimal("88.80")), "integration-payment",
                Instant.parse("2026-03-27T10:30:00Z"), Instant.parse("2026-03-27T10:00:00Z"));
        paymentOrder.markPaying();

        PaymentOrder persistedOrder = paymentRepositorySupport.saveOrder(paymentOrder);
        PaymentCallbackRecord persistedCallback = paymentRepositorySupport.saveCallbackRecord(new PaymentCallbackRecord(
                null, TenantId.of("1001"), persistedOrder.getPaymentNo(), persistedOrder.getOrderNo(), PaymentChannelCode.MOCK,
                "TXN-IT-10001", PaymentChannelStatus.SUCCESS, "{\"tradeStatus\":\"SUCCESS\"}",
                Instant.parse("2026-03-27T10:01:00Z")));
        paymentRepositorySupport.saveAuditLog(new PaymentAuditLog(null, 1001L, persistedOrder.getPaymentNo(),
                PaymentAuditActionType.CREATE, null, PaymentStatus.PAYING, PaymentAuditOperatorType.SYSTEM, "0",
                Instant.parse("2026-03-27T10:00:00Z")));

        PaymentOrder reloadedOrder = paymentRepositorySupport.findOrderByPaymentNo(1001L, "PAY-IT-10001").orElseThrow();
        PaymentCallbackRecord reloadedCallback = paymentRepositorySupport
                .findCallbackByChannelTransactionNo(1001L, "MOCK", "TXN-IT-10001")
                .orElseThrow();

        assertNotNull(persistedOrder.getId());
        assertEquals(PaymentOrderId.of(1L), persistedOrder.getId());
        assertNotNull(persistedCallback.getId());
        assertEquals("ORD-IT-10001", reloadedOrder.getOrderNo().value());
        assertEquals("TXN-IT-10001", reloadedCallback.getChannelTransactionNo());
        assertEquals(1, paymentRepositorySupport.findAuditLogsByPaymentNo(1001L, persistedOrder.getPaymentNo().value()).size());
    }

    @Configuration(proxyBeanMethods = false)
    @MapperScan("com.github.thundax.bacon.payment.infra.persistence.mapper")
    static class TestConfig {

        @Bean
        DataSource dataSource() {
            JdbcDataSource dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:payment_repo_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE");
            dataSource.setUser("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
            MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
            factoryBean.setDataSource(dataSource);
            return factoryBean.getObject();
        }

        @Bean
        PaymentRepositorySupport paymentRepositorySupport(PaymentOrderMapper paymentOrderMapper,
                                                         PaymentCallbackRecordMapper paymentCallbackRecordMapper,
                                                         PaymentAuditLogMapper paymentAuditLogMapper,
                                                         IdGenerator idGenerator) {
            return new PaymentRepositorySupport(paymentOrderMapper, paymentCallbackRecordMapper, paymentAuditLogMapper,
                    idGenerator);
        }

        @Bean
        IdGenerator idGenerator() {
            return bizTag -> 1L;
        }
    }
}
