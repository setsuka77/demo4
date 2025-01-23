package com.example.demo.repository;

import java.util.List;

import com.example.demo.dto.CartDto;
import com.example.demo.entity.Cart;
import com.example.demo.form.GoodsForm;

import jakarta.servlet.http.HttpSession;

public interface ItemRepository {
	//カートに追加する
	 int addCart(GoodsForm goodsForm,HttpSession session);
	//カート情報を表示
	 List<Cart> getCart(HttpSession session);
	//削除ボタン押下処理
	 List<Cart> deleteItem(Integer id,HttpSession session);
	//数量変更処理
	 List<Cart> updateItemQuantity(CartDto cartDto,HttpSession session);
}
