package com.example.demo.repository;

import java.util.List;

import com.example.demo.entity.OrderSummary;
import com.example.demo.form.GoodsForm;

import jakarta.servlet.http.HttpSession;

public interface ItemRepository {
	//カートに追加する
	 int addCart(GoodsForm goodsForm,HttpSession session);
	//カート情報を表示
	 List<OrderSummary> getCart(HttpSession session);
	//削除ボタン押下処理
}
