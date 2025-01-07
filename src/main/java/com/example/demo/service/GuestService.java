package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Goods;
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

	//カートに追加(セッションで管理)
	@Override
	public int addCart(GoodsForm goodsForm, HttpSession session) {
		// セッションからゲストのカートを取得または新規作成
		Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("guestCart");
		if (cart == null) {
			cart = new HashMap<>();
			session.setAttribute("guestCart", cart);
		}

		// GoodsFormから商品情報を取得し、カートに追加
		for (GoodForm good : goodsForm.getGoodsFormList()) {
			Integer productId = good.getProductId();
			Integer quantity = good.getQuantity();
			if (quantity != null && quantity > 0) {
				// カートに商品を追加
				cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
			}
		}
		// カート内の合計アイテム数を計算
		int totalItems = 0;
		totalItems = cart.values().stream().mapToInt(Integer::intValue).sum();

		// 合計アイテム数を返す
		return totalItems;
	}

	//カート内一覧表示
	@Override
	public List<Cart> getCart(HttpSession session) {
		List<Cart> cartList = new ArrayList<>();
		Map<Integer, Integer> guestCart = (Map<Integer, Integer>) session.getAttribute("guestCart");

		if (guestCart != null) {
			List<Integer> productIds = new ArrayList<>(guestCart.keySet());
			// 商品情報をデータベースから取得
			List<Goods> products = goodsMapper.getProductsByIds(productIds);

			for (Integer productId : productIds) {
				Goods product = findProductById(products, productId);
				if (product != null) {
					Integer quantity = guestCart.get(productId);
					Cart newItem = new Cart();
					newItem.setProductId(productId);
					newItem.setQuantity(quantity);
					newItem.setUnitPrice(product.getPrice());
					newItem.setProductName(product.getProductName());
					newItem.setImageUrl(product.getImageUrl());
					newItem.setIsStockAvailable(quantity <= product.getStockQuantity());
					//セッションのリストに追加する
					cartList.add(newItem);
				}
			}
		}
		return cartList;
	}

	//カート削除
	@Override
	public List<Cart> deleteItem(Integer id, HttpSession session) {
		List<Cart> cartList = new ArrayList<>();
		Map<Integer, Integer> guestCart = (Map<Integer, Integer>) session.getAttribute("guestCart");

		if (guestCart != null) {
			// 商品IDが一致するものを削除
			if (guestCart.containsKey(id)) {
				guestCart.remove(id); // 一致する商品IDを削除
			}

			// 削除後のカート情報を再取得
			List<Integer> remainingProductIds = new ArrayList<>(guestCart.keySet());
			if (!remainingProductIds.isEmpty()) {
				// データベースから残りの商品の情報を取得
				List<Goods> products = goodsMapper.getProductsByIds(remainingProductIds);
				for (Goods product : products) {
					Cart newItem = new Cart();
					Integer quantity = guestCart.get(product.getProductId()); 
					newItem.setProductId(product.getProductId());
					newItem.setQuantity(quantity);
					newItem.setUnitPrice(product.getPrice());
					newItem.setProductName(product.getProductName());
					newItem.setImageUrl(product.getImageUrl());
					newItem.setIsStockAvailable(quantity <= product.getStockQuantity());
					cartList.add(newItem); 
				}
			}
		}
		return cartList;
	}

}
