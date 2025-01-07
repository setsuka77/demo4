package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Goods;
import com.example.demo.entity.Users;
import com.example.demo.form.GoodForm;
import com.example.demo.form.GoodsForm;
import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.mapper.OrderSummaryMapper;
import com.example.demo.repository.ItemRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class UsersService implements ItemRepository {

	//private final OrderSummaryMapper orderSummaryMapper;
	private final GoodsMapper goodsMapper;
	private final CartMapper cartMapper;

	// 商品IDを指定して、商品をリストから探すメソッド
	private Goods findProductById(List<Goods> products, Integer productId) {
		for (Goods product : products) {
			if (product.getProductId().equals(productId)) { // 商品IDが一致すれば
				return product;
			}
		}
		return null; // 商品が見つからなければnullを返す
	}

	//リストに商品があるとき更新
	private void updateCartQuantity(Cart existingItem, int quantityToAdd) {
		existingItem.setQuantity(existingItem.getQuantity() + quantityToAdd);
		cartMapper.updateCart(existingItem); // カート情報を更新
	}

	//新規登録
	private void addNewItemToCart(Integer userId, GoodForm submit) {
		Cart newItem = new Cart();
		newItem.setProductId(submit.getProductId());
		newItem.setUnitPrice(submit.getPrice());
		newItem.setQuantity(submit.getQuantity());
		newItem.setUserId(userId);
		cartMapper.addCart(newItem); // カートに新規追加
	}

	public UsersService(OrderSummaryMapper orderSummaryMapper, GoodsMapper goodsMapper, CartMapper cartMapper) {
		//this.orderSummaryMapper = orderSummaryMapper;
		this.goodsMapper = goodsMapper;
		this.cartMapper = cartMapper;
	}

	//カートに追加
	@Override
	@Transactional
	public int addCart(GoodsForm goodsForm, HttpSession session) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		Integer userId = loginUser.getId();

		// フォームデータから商品リストを取得
		List<GoodForm> submitList = goodsForm.getGoodsFormList();

		// 既存カート情報を取得
		List<Cart> existingCart = cartMapper.cartCheck(userId);
		Map<Integer, Cart> existingCartMap = existingCart.stream()
				.collect(Collectors.toMap(Cart::getProductId, item -> item));

		// カートに商品の追加または数量の更新
		for (GoodForm submit : submitList) {
			if (submit.getQuantity() != null && submit.getQuantity() > 0) {
				Cart existingItem = existingCartMap.get(submit.getProductId());

				if (existingItem != null) {
					// 既存のカートに同じ商品があれば数量を加算
					updateCartQuantity(existingItem, submit.getQuantity());
				} else {
					// 同じ商品がカートにない場合、新規追加
					addNewItemToCart(userId, submit);
				}
			}
		}
		// ユーザーのカートに入っている商品数を返す
		return cartMapper.getTotalItemsForUser(userId);
	}

	//カート情報を表示
	@Override
	@Transactional
	public List<Cart> getCart(HttpSession session) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		Integer userId = loginUser.getId();

		//セッションからカート情報を取得する
		Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("guestCart");
		if (cart != null) {
			// cartからproductIdを取り出してリストにする
			List<Integer> productIds = new ArrayList<>(cart.keySet());
			// 商品情報をデータベースから取得
			List<Goods> products = goodsMapper.getProductsByIds(productIds);

			//既存カート情報を取得
			List<Cart> existingCart = cartMapper.cartCheck(userId);
			Map<Integer, Cart> existingCartMap = existingCart.stream()
					.collect(Collectors.toMap(Cart::getProductId, item -> item));

			// 新規アイテムを格納するリストと更新リスト
			List<Cart> itemsToUpdate = new ArrayList<>();
			List<Cart> itemsToInsert = new ArrayList<>();

			for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
				Integer productId = entry.getKey();
				Integer quantity = entry.getValue();
				Goods product = findProductById(products, productId);

				if (product != null) {
					// DBにアイテムが存在するか確認
					Cart existingItem = existingCartMap.get(productId);

					if (existingItem != null) {
						// 既存アイテムがあれば、数量を更新するリストに追加
						existingItem.setQuantity(existingItem.getQuantity() + quantity);
						itemsToUpdate.add(existingItem);
					} else {
						// 新規アイテムの場合、挿入リストに追加
						Cart newItem = new Cart();
						newItem.setProductId(productId);
						newItem.setQuantity(quantity);
						newItem.setUnitPrice(product.getPrice());
						newItem.setUserId(userId);
						itemsToInsert.add(newItem);
					}
				}
			}

			// 一件ずつ更新を行う
			for (Cart itemToUpdate : itemsToUpdate) {
				cartMapper.updateCart(itemToUpdate); 
			}
			// 一件ずつ挿入を行う
			for (Cart itemToInsert : itemsToInsert) {
				cartMapper.addCart(itemToInsert); 
			}
			// セッションからゲストカートを削除
			session.removeAttribute("guestCart");
		}
		return cartMapper.cartCheck(userId); // 最終的にユーザーのカート情報を返す
	}

	//カート削除
	@Transactional
	@Override
	public List<Cart> deleteItem(Integer id, HttpSession session) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		Integer userId = loginUser.getId();
		//DB物理削除
		cartMapper.deleteCart(userId,id);
		
		return cartMapper.cartCheck(userId); // 最終的にユーザーのカート情報を返す
	}

}
