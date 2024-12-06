package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Goods;
import com.example.demo.entity.OrderSummary;
import com.example.demo.entity.Users;
import com.example.demo.form.GoodForm;
import com.example.demo.form.GoodsForm;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.OrderSummaryMapper;
import com.example.demo.repository.ItemRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class UsersService implements ItemRepository {

	private final OrderSummaryMapper orderSummaryMapper;
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

	public UsersService(OrderSummaryMapper orderSummaryMapper, GoodsMapper goodsMapper) {
		this.orderSummaryMapper = orderSummaryMapper;
		this.goodsMapper = goodsMapper;
	}

	@Override
	@Transactional
	public int addCart(GoodsForm goodsForm, HttpSession session) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		Integer userId = loginUser.getId();
		// フォームデータから商品リストを取得
		List<GoodForm> submitList = goodsForm.getGoodsFormList();
		// 既存のカート情報を取得
		List<OrderSummary> existingCart = orderSummaryMapper.cartCheck(userId);

		for (GoodForm submit : submitList) {
			if (submit.getQuantity() != null && submit.getQuantity() > 0) {
				boolean found = false;

				// 既存カートに同じ商品が存在するか確認
				for (OrderSummary existingItem : existingCart) {
					if (existingItem.getProductId().equals(submit.getProductId())) {
						// 既存のカートに同じ商品があれば数量を加算
						existingItem.setQuantity(existingItem.getQuantity() + submit.getQuantity());
						orderSummaryMapper.updateCart(existingItem); // カート情報を更新
						found = true;
						break;
					}
				}
				// 同じ商品がカートにない場合、新規追加
				if (!found) {
					OrderSummary newItem = new OrderSummary();
					newItem.setProductId(submit.getProductId());
					newItem.setUnitPrice(submit.getPrice());
					newItem.setQuantity(submit.getQuantity());
					newItem.setUserId(userId);
					newItem.setUseFlag(false); // 必要に応じて設定
					orderSummaryMapper.addCart(newItem); // カートに新規追加
				}
			}
		}
		// ユーザーのカートに入っている商品数を返す
		return orderSummaryMapper.getTotalItemsForUser(userId);
	}

	@Override
	@Transactional
	public List<OrderSummary> getCart(HttpSession session) {
		List<OrderSummary> orderSummaryList = new ArrayList<>();
		//カート情報を取得する
		Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("guestCart");
		System.out.println("セッションから取得" + cart);

		// guestCartのnullチェック
		if (cart != null) {
			// cartからproductIdを取り出してリストにする
			List<Integer> productIds = new ArrayList<>(cart.keySet());
			// 商品情報をデータベースから取得
			List<Goods> products = goodsMapper.getProductsByIds(productIds);
			for (Integer productId : productIds) {
				// 商品情報を取得
				Goods product = findProductById(products, productId);
				if (product != null) {
					Integer quantity = cart.get(productId); // カートの数量を取得

					// OrderSummaryにまとめる
					OrderSummary orderSummary = new OrderSummary();
					orderSummary.setProductId(productId);
					orderSummary.setQuantity(quantity);
					orderSummary.setUnitPrice(product.getPrice());
					orderSummary.setProductName(product.getProductName());
					orderSummary.setImageUrl(product.getImageUrl());
					orderSummary.setUseFlag(false); // 必要に応じて設定

					System.out.println("セッションからリスト作成" + orderSummary);
					// 作成したOrderSummaryをリストに追加
					orderSummaryList.add(orderSummary);
				}
			}
		}

		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		// DBからのカート情報を取得
		List<OrderSummary> dbCart = orderSummaryMapper.cartCheck(loginUser.getId());
		System.out.println("dbから取得" + dbCart);
		// DBカートが存在する場合
	    if (dbCart != null) {
	        // セッションのカート情報をMapに変換
	        Map<Integer, OrderSummary> orderSummaryMap = new HashMap<>();
	        for (OrderSummary orderSummary : orderSummaryList) {
	            orderSummaryMap.put(orderSummary.getProductId(), orderSummary);
	        }

	        // DBカートとセッションカートをマージ
	        for (OrderSummary dbOrderSummary : dbCart) {
	            OrderSummary orderSummary = orderSummaryMap.get(dbOrderSummary.getProductId());
	            if (orderSummary != null) {
	                // 同じ商品があれば数量を加算
	                orderSummary.setQuantity(orderSummary.getQuantity() + dbOrderSummary.getQuantity());
	                // DBで更新
	                orderSummaryMapper.updateCart(orderSummary);
	            } else {
	                // 同じ商品がなければ、新規に追加
	                orderSummaryMapper.addCart(dbOrderSummary);
	                orderSummaryList.add(dbOrderSummary);
	            }
	        }
	    }
		System.out.println("DBに保存する完成形" + orderSummaryList);

		return orderSummaryList;
	}

}
