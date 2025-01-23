package com.example.demo.entity;

import lombok.Data;

@Data
public class Address {
	public Integer addressId;
	public Integer userId;
	public String recipientName;
	public String kana;
	public String postalCode;
	public String city;
	public String addressLine1;
	public String addressLine2;
	public String phoneNumber;



}
