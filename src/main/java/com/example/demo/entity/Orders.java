package com.example.demo.entity;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class Orders {
	private Integer orderId;
	private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private Integer couponId;
    private BigDecimal total;
    private Integer userId;
    private int paymentMethod;
    private boolean payStatus;
    private Date paymentConfirmationDate;
    private Integer addressId;
    private boolean deliveryStatus;
    private Date orderDate;
}
