package com.example.demo.entity;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class Cart {
	private Integer id;
	private Integer userId;
	private Integer productId;
	private Integer quantity;
	private BigDecimal unitPrice;
	
	private String productName;
	private String imageUrl;
	private Boolean isStockAvailable;
}
