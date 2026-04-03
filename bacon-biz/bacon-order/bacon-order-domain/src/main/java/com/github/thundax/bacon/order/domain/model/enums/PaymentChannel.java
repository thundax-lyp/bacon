package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 支付渠道。
 */
public enum PaymentChannel {

    ALIPAY("ALIPAY"),
    WECHAT("WECHAT"),
    APPLE("APPLE"),
    MOCK("MOCK");

    private final String value;

    PaymentChannel(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static PaymentChannel fromValue(String value) {
        for (PaymentChannel channel : values()) {
            if (channel.value.equals(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unsupported payment channel: " + value);
    }
}
