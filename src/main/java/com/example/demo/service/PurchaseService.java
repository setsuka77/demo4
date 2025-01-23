package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Address;
import com.example.demo.entity.Coupon;
import com.example.demo.form.AddressForm;
import com.example.demo.mapper.CouponsMapper;
import com.example.demo.mapper.ShoppingAddressesMapper;

@Service
public class PurchaseService {

	private final ShoppingAddressesMapper shoppingAddressesMapper;
	private final CouponsMapper couponsMapper;

	public PurchaseService(ShoppingAddressesMapper shoppingAddressesMapper, CouponsMapper couponsMapper) {
		this.shoppingAddressesMapper = shoppingAddressesMapper;
		this.couponsMapper = couponsMapper;
	}

	//既存の住所取得
	public List<Address> getAddress(Integer id) {
		return shoppingAddressesMapper.getAddress(id);
	}

	//新規住所登録
	public Integer insertAddress(AddressForm addressForm, Integer userId) {
		Address newAddress = new Address();
		newAddress.setUserId(userId);
		newAddress.setRecipientName(addressForm.getRecipientName());
		newAddress.setKana(addressForm.getKana());
		newAddress.setPostalCode(addressForm.getPostalCode());
		newAddress.setCity(addressForm.getCity());
		newAddress.setAddressLine1(addressForm.getAddressLine1());
		newAddress.setAddressLine2(addressForm.getAddressLine2());
		newAddress.setPhoneNumber(addressForm.getPhoneNumber());

		shoppingAddressesMapper.insert(newAddress);
		return newAddress.getAddressId(); // 自動生成されたIDを返す
	}

	//クーポン検索
	public Coupon selectCoupon(String couponCode) {
		Coupon coupon = couponsMapper.findAll(couponCode,0);
		return coupon;
	}

}
