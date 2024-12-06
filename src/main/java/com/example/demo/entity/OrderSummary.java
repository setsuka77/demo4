package com.example.demo.entity;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderSummary {
	
	private Integer id;
	private Integer orderId;
	private Integer productId;
	private Integer quantity;
	private BigDecimal unitPrice;
	private Integer userId;
	private Boolean useFlag;
	
	private String productName;
	private String imageUrl;
	private Boolean isStockAvailable;
}
