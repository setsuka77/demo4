package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.GoodsDto;
import com.example.demo.entity.Goods;

@Mapper
public interface GoodsMapper {
	//カテゴリーIDによる商品情報取得
	List<GoodsDto> findProductById(@Param("id") Integer id);
	//商品IDによる商品情報取得
	List<Goods> getProductsByIds(List<Integer> productIds);
	
}
