package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.CartDto;
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
		System.out.println(goodsForm);
		// セッションからゲストのカートを取得または新規作成
		List<Cart> cartList = (List<Cart>) session.getAttribute("guestCart");
		if (cartList == null) {
			cartList = new ArrayList<>();
			session.setAttribute("guestCart", cartList);
		}

		// GoodsFormから商品情報を取得し、カートに追加
		for (GoodForm good : goodsForm.getGoodsFormList()) {
			Integer quantity = good.getQuantity();
			if (quantity != null && quantity > 0) {
				// 既にカートに同じ商品IDが存在するかチェック
				Cart existingCartItem = cartList.stream()
						.filter(cart -> cart.getProductId().equals(good.getProductId()))
						.findFirst()
						.orElse(null); // 一致する商品がない場合はnull

				if (existingCartItem != null) {
					// 既存のカートアイテムがあれば、その数量を加算
					existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
				} else {
					// 新しい商品をカートに追加
					Cart newItem = new Cart();
					newItem.setProductId(good.getProductId());
					newItem.setQuantity(quantity);
					newItem.setUnitPrice(good.getPrice());
					newItem.setProductName(good.getProductName());
					newItem.setImageUrl(good.getImageUrl());
					newItem.setIsStockAvailable(good.getQuantity() <= good.getStockQuantity()); // 仮に商品数がquantityより多ければ在庫あり
				
					// カートに商品を追加
					cartList.add(newItem); // 追加順にリストに追加
				}
			}
		}
		// カート内の合計アイテム数を計算
		int totalItems = cartList.stream().mapToInt(Cart::getQuantity).sum();

		// 合計アイテム数を返す
		return totalItems;
	}

	// カート内一覧表示
	@Override
	public List<Cart> getCart(HttpSession session) {
		List<Cart> cartList = (List<Cart>) session.getAttribute("guestCart");
		if (cartList == null) {
			cartList = new ArrayList<>();
		}
		return cartList; // 追加順にリストを返す
	}

	//カート削除
	@Override
	public List<Cart> deleteItem(Integer id, HttpSession session) {
		List<Cart> cartList = (List<Cart>) session.getAttribute("guestCart");
		if (cartList != null) {
			cartList.removeIf(cart -> cart.getProductId().equals(id)); // 商品IDで削除
		}

		// カート内の合計アイテム数を再計算
		int totalItems = cartList.stream().mapToInt(Cart::getQuantity).sum();
		session.setAttribute("totalItems", totalItems); // 合計アイテム数をセッションに保存

		return cartList;
	}

	
	//数量変更
	@Override
	public List<Cart> updateItemQuantity(CartDto cartDto, HttpSession session) {
		List<Cart> cartList = (List<Cart>) session.getAttribute("guestCart");

		if (cartList == null) {
			return new ArrayList<>(); // カートが空または存在しない場合は空のリストを返す
		}

		// 商品IDと新しい数量を取得
		Integer productId = cartDto.getProductId();
		Integer newQuantity = cartDto.getQuantity();

		// 商品を探して数量を更新（Optionalとstreamを使用）
		cartList.stream()
				.filter(cart -> cart.getProductId().equals(productId))
				.findFirst() // 最初に一致する商品を探す
				.ifPresent(cart -> cart.setQuantity(newQuantity)); // 商品が見つかったら数量を更新

		// カート内の合計アイテム数を再計算
		int totalItems = cartList.stream().mapToInt(Cart::getQuantity).sum();
		session.setAttribute("totalItems", totalItems); // 合計アイテム数をセッションに保存

		// 更新されたカート情報を返す
		return cartList;
	}

}
