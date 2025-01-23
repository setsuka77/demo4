package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.entity.Address;

@Mapper
public interface ShoppingAddressesMapper {
	
	//ユーザーIdで取得
	List<Address> getAddress(Integer userId);
	//新規住所登録
	Integer insert(Address address);
}
