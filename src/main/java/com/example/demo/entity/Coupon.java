package com.example.demo.entity;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class Coupon {
	private Integer couponId;
	private String code;
	private BigDecimal discountAmount;
	private Date expirationDate;
	private Integer status;
}
