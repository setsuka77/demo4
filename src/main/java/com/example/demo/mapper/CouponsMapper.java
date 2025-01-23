package com.example.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.entity.Coupon;

@Mapper
public interface CouponsMapper {
	
	//クーポン検索
	Coupon findAll (String code, int status);
}
