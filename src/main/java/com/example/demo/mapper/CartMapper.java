package com.example.demo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Cart;

@Mapper
public interface CartMapper {
	
	//ログインページのカート合計数検索用
	Integer getTotalItemsForUser(@Param("userId") int userId);
	
	//カート追加時更新用
	void updateCart(Cart cart);
	
	//カート追加時登録用
	void addCart(Cart cart);
	
	//カート内容取得
	List<Cart> cartCheck(@Param("userId") int userId);
	
	//カート内容削除
	void deleteCart(int userId,Integer productId);
	
	//数量変更
	void updateQuantity(Integer productId,Integer quantity,Integer userId);
	
}
