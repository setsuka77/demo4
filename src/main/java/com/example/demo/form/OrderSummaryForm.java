package com.example.demo.form;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderSummaryForm {
	private Integer id;
	private Integer orderId;
	private Integer productId;
	private Integer quantity;
	private BigDecimal unitPrice;
	private Integer userId;
	private Boolean useFlag;

}
