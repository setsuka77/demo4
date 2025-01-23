package com.example.demo.form;

import lombok.Data;

@Data
public class AddressForm {
	public Integer addressId;
	public Integer userId;
	public String recipientName;
	public String kana;
	public String postalCode;
	public String city;
	public String addressLine1;
	public String addressLine2;
	public String phoneNumber;
	

	private String fullAddress;
}
