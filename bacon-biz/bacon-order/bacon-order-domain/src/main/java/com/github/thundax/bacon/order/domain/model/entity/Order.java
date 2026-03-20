package com.github.thundax.bacon.order.domain.model.entity;

public class Order {

    private final Long id;
    private final String orderNo;
    private final String customerName;

    public Order(Long id, String orderNo, String customerName) {
        this.id = id;
        this.orderNo = orderNo;
        this.customerName = customerName;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public String getCustomerName() {
        return customerName;
    }
}
