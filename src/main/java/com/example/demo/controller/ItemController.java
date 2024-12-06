package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.dto.GoodsDto;
import com.example.demo.entity.Users;
import com.example.demo.form.GoodForm;
import com.example.demo.form.GoodsForm;
import com.example.demo.mapper.GoodsMapper;
import com.example.demo.service.GuestService;
import com.example.demo.service.UsersService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ItemController {
	// 商品リストをGoodFormに変換する共通メソッド
	private List<GoodForm> convertToGoodFormList(List<GoodsDto> goodsList) {
		List<GoodForm> goodFormList = new ArrayList<>();
		for (GoodsDto goods : goodsList) {
			GoodForm goodForm = new GoodForm();
			goodForm.setProductId(goods.getProductId());
			goodForm.setProductName(goods.getProductName());
			goodForm.setPrice(goods.getPrice());
			goodForm.setDescription(goods.getDescription());
			goodForm.setImageUrl(goods.getImageUrl());
			goodForm.setCategoryId(goods.getCategoryId());
			goodForm.setQuantity(goods.getQuantity() != null ? goods.getQuantity() : 0);//デフォルメでは0に設定
			goodFormList.add(goodForm);
		}
		return goodFormList;
	}

	private final GoodsMapper goodsMapper;
	private final UsersService usersService;
	private final GuestService guestService;

	public ItemController(GoodsMapper goodsMapper, UsersService usersService, GuestService guestService) {
		this.goodsMapper = goodsMapper;
		this.usersService = usersService;
		this.guestService = guestService;
	}

	@GetMapping("/category/branch/{id}")
	public String showBranch(HttpSession session, @PathVariable Integer id, Model model) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		if (loginUser != null) {
			model.addAttribute("user", loginUser);
		}

		//商品情報取得
		GoodsForm goodsForm = new GoodsForm();
		List<GoodsDto> pro = goodsMapper.findProductById(id);
		if (pro == null || pro.isEmpty()) {
			System.out.println("カテゴリーID " + id + " に一致する商品が見つかりませんでした。");
		} else {
			System.out.println("カテゴリーID " + id + " に一致する商品数: " + pro.size());
		}

		goodsForm.setGoodsFormList(convertToGoodFormList(pro));
		model.addAttribute("goodsForm", goodsForm);
		session.setAttribute("goodsForm", goodsForm);

		Integer totalItems = (Integer) session.getAttribute("totalItems");
		if (totalItems == null) {
			totalItems = 0; // null の場合は 0 に初期化
		}
		model.addAttribute("totalItems", totalItems);

		return "category/branch";
	}

	//「カートに入れる」ボタン押下
	@PostMapping(path = "/category/branch", params = "cart")
	public String addCart(HttpSession session, Model model, GoodsForm submitForm) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		if (loginUser != null) {
			model.addAttribute("user", loginUser);
		}
		System.out.println("ゲスト" + submitForm);

		int totalItems = 0;
		//カート追加
		if (loginUser != null) {
			totalItems = usersService.addCart(submitForm, session);
		} else {
			totalItems = guestService.addCart(submitForm, session);
		}
		String message = "カートに商品が追加されました";
		model.addAttribute("message", message);
		System.out.println("カートボタン押下" + totalItems);

		session.setAttribute("totalItems", totalItems);
		model.addAttribute("totalItems", totalItems);

		//商品情報取得
		GoodsForm goodsForm = (GoodsForm) session.getAttribute("goodsForm");
		model.addAttribute("goodsForm", goodsForm);

		return "category/branch";
	}

}
