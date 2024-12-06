package com.example.demo.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class Goods {
	private Integer productId;
	private String productName;
	private BigDecimal price;
	private String description;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private String imageUrl;
	private Integer categoryId;

}
