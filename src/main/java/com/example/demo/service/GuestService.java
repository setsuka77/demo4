package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Goods;
import com.example.demo.entity.OrderSummary;
import com.example.demo.form.GoodForm;
import com.example.demo.form.GoodsForm;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.repository.ItemRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class GuestService implements ItemRepository {

	private final GoodsMapper goodsMapper;

	// 商品IDを指定して、商品をリストから探すメソッド
	private Goods findProductById(List<Goods> products, Integer productId) {
		for (Goods product : products) {
			if (product.getProductId().equals(productId)) { // 商品IDが一致すれば
				return product;
			}
		}
		return null; // 商品が見つからなければnullを返す
	}

	public GuestService(GoodsMapper goodsMapper) {
		this.goodsMapper = goodsMapper;
	}

	@Override
	public int addCart(GoodsForm goodsForm, HttpSession session) {
		// セッションからゲストのカートを取得または新規作成
		Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("guestCart");
		if (cart == null) {
			cart = new HashMap<>();
			session.setAttribute("guestCart", cart);
		}
		System.out.println("ゲスト" + goodsForm);

		// GoodsFormから商品情報を取得し、カートに追加
		for (GoodForm good : goodsForm.getGoodsFormList()) {
			Integer productId = good.getProductId();
			Integer quantity = good.getQuantity();
			if (quantity != null && quantity > 0) {
				// カートに商品を追加
				cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
			}
		}
		System.out.println("ゲストのほうに来てる");
		// カート内の合計アイテム数を計算
		int totalItems = 0;
		totalItems = cart.values().stream().mapToInt(Integer::intValue).sum();

		// 合計アイテム数を返す
		return totalItems;
	}

	@Override
	public List<OrderSummary> getCart(HttpSession session) {
		List<OrderSummary> orderSummaryList = new ArrayList<>();
		Map<Integer, Integer> guestCart = (Map<Integer, Integer>) session.getAttribute("guestCart");

		if (guestCart != null) {
			List<Integer> productIds = new ArrayList<>(guestCart.keySet());
			// 商品情報をデータベースから取得
			List<Goods> products = goodsMapper.getProductsByIds(productIds);

			for (Integer productId : productIds) {
				Goods product = findProductById(products, productId);
				if (product != null) {
					Integer quantity = guestCart.get(productId);
					OrderSummary orderSummary = new OrderSummary();
					orderSummary.setProductId(productId);
					orderSummary.setQuantity(quantity);
					orderSummary.setUnitPrice(product.getPrice());
					orderSummary.setProductName(product.getProductName());
					orderSummary.setImageUrl(product.getImageUrl());
					orderSummary.setUseFlag(true);
					orderSummaryList.add(orderSummary);
				}
			}
		}
		return orderSummaryList;
	}
}
