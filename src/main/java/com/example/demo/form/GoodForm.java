package com.example.demo.form;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class GoodForm {
	private Integer productId;
	private String productName;
	private BigDecimal price;
	private String description;
	private Timestamp createdAt;
	private Timestamp updatedAt;
	private String imageUrl;
	private Integer categoryId;
	
	private Integer quantity;

}
