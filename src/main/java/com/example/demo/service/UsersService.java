package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CartDto;
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
	public List<Cart> getCart(HttpSession session) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		Integer userId = loginUser.getId();

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
		cartMapper.deleteCart(userId, id);
		//トータル数更新
		Integer totalItems = cartMapper.getTotalItemsForUser(userId);
		session.setAttribute("totalItems", totalItems);

		return cartMapper.cartCheck(userId); // 最終的にユーザーのカート情報を返す
	}

	//セッションからカート情報を取得して保存
	@Transactional
	public Integer saveCart(Integer userId, HttpSession session) {
		List<Cart> cartList = (List<Cart>) session.getAttribute("guestCart");

		if (cartList != null) {
			// 商品IDのリストを作成
			List<Integer> productIds = cartList.stream()
					.map(Cart::getProductId)
					.collect(Collectors.toList());

			// 商品情報を一括取得しマップ化
			Map<Integer, Goods> productMap = goodsMapper.getProductsByIds(productIds).stream()
					.collect(Collectors.toMap(Goods::getProductId, Function.identity()));

			// 既存カートをマップ化
			Map<Integer, Cart> existingCartMap = cartMapper.cartCheck(userId).stream()
					.collect(Collectors.toMap(Cart::getProductId, Function.identity()));

			// カートアイテムの処理
			cartList.forEach(cartItem -> {
				Integer productId = cartItem.getProductId();
				Integer quantity = cartItem.getQuantity();
				Goods product = productMap.get(productId);

				if (product != null) {
					Cart existingItem = existingCartMap.get(productId);

					if (existingItem != null) {
						// 既存アイテムがあれば数量を更新
						existingItem.setQuantity(existingItem.getQuantity() + quantity);
						cartMapper.updateCart(existingItem);
					} else {
						// 新規アイテムを挿入
						Cart newItem = new Cart();
						newItem.setProductId(productId);
						newItem.setQuantity(quantity);
						newItem.setUnitPrice(product.getPrice());
						newItem.setUserId(userId);
						cartMapper.addCart(newItem);
					}
				}
			});

			// セッションからゲストカートを削除
			session.removeAttribute("guestCart");
		}

		// ユーザーのカートに入っている商品数を返す
		return cartMapper.getTotalItemsForUser(userId);
	}

	
	//数量変更
	@Transactional
	@Override
	public List<Cart> updateItemQuantity(CartDto cartDto, HttpSession session) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		Integer userId = loginUser.getId();
		//商品IDと数量を取得、更新
		Integer productId = cartDto.getProductId();
		Integer quantity = cartDto.getQuantity();
		cartMapper.updateQuantity(productId,quantity,userId);
		
		//トータル数更新
		Integer totalItems = cartMapper.getTotalItemsForUser(userId);
		session.setAttribute("totalItems", totalItems);

		return cartMapper.cartCheck(userId); // 最終的にユーザーのカート情報を返す
	}

}
