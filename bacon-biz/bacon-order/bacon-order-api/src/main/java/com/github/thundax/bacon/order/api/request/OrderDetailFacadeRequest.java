package com.github.thundax.bacon.order.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单详情门面请求。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;
}
