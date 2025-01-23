package com.example.demo.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.CartDto;
import com.example.demo.entity.Address;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Coupon;
import com.example.demo.entity.Users;
import com.example.demo.form.AddressForm;
import com.example.demo.service.GuestService;
import com.example.demo.service.PurchaseService;
import com.example.demo.service.UsersService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PurchaseController {
	private final UsersService usersService;
	private final GuestService guestService;
	private final PurchaseService purchaseService;

	public PurchaseController(UsersService usersService, GuestService guestService, PurchaseService purchaseService) {
		this.usersService = usersService;
		this.guestService = guestService;
		this.purchaseService = purchaseService;
	}

	//カート画面表示
	@GetMapping("/cart/cart")
	public String showCart(Model model, HttpSession session) {
		List<Cart> cartList = new ArrayList<>();
		Users loginUser = (Users) session.getAttribute("user");

		// ゲストユーザーとログインユーザーの処理を分ける
		if (loginUser != null) {
			cartList = usersService.getCart(session);
			// カートリストをID順にソート
			cartList.sort(Comparator.comparing(Cart::getId));
			model.addAttribute("user", loginUser);
		} else {
			cartList = guestService.getCart(session);
		}

		//合計数を取得
		Integer totalItems = (Integer) session.getAttribute("totalItems");
		model.addAttribute("totalItems", totalItems);

		model.addAttribute("cartList", cartList);
		model.addAttribute("isCartPage", true);
		return "cart/cart";
	}

	//削除ボタン押下
	@PostMapping(path = "/cart/cart")
	public String deleatCart(Model model, HttpSession session, @RequestParam("productId") Integer productId) {
		List<Cart> cartList = new ArrayList<>();
		Users loginUser = (Users) session.getAttribute("user");

		// ゲストユーザーとログインユーザーの処理を分ける
		if (loginUser != null) {
			cartList = usersService.deleteItem(productId, session);
			// カートリストをID順にソート
			cartList.sort(Comparator.comparing(Cart::getId));
			model.addAttribute("user", loginUser);
		} else {
			cartList = guestService.deleteItem(productId, session);
		}

		//合計数を取得
		Integer totalItems = (Integer) session.getAttribute("totalItems");
		model.addAttribute("totalItems", totalItems);

		model.addAttribute("cartList", cartList);
		model.addAttribute("isCartPage", true);
		return "cart/cart";
	}

	//数量変更処理
	@ResponseBody
	@RequestMapping(value = "/cart/update", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public ResponseEntity<Map<String, Object>> updateCart(HttpSession session, @RequestBody CartDto cartDto) {
		List<Cart> cartList = new ArrayList<>();
		Map<String, Object> response = new HashMap<>();
		Users loginUser = (Users) session.getAttribute("user");

		// ゲストユーザーとログインユーザーの処理を分ける
		if (loginUser != null) {
			cartList = usersService.updateItemQuantity(cartDto, session);
			// 更新されたカートリストをソート
			cartList.sort(Comparator.comparing(Cart::getId));
			response.put("user", loginUser);
		} else {
			cartList = guestService.updateItemQuantity(cartDto, session);
		}

		response.put("cartList", cartList);
		response.put("isCartPage", true);
		response.put("cartDto", cartDto);
		return ResponseEntity.ok(response);
	}

	// クーポン使用処理
	@ResponseBody
	@RequestMapping(value = "/cart/coupon", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public ResponseEntity<Map<String, Object>> applyCoupon(@RequestBody Map<String, String> request,
			HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		// couponCodeをリクエストから取得
		String couponCode = request.get("couponCode");

		// クーポンコードの処理をここに追加
		Optional<Coupon> optionalCoupon = Optional.ofNullable(purchaseService.selectCoupon(couponCode));
		if (optionalCoupon.isPresent()) {
			session.setAttribute("coupon", optionalCoupon.get());
			response.put("coupon", optionalCoupon.get());
		} else {
			response.put("message", "※無効なクーポンコードです");
		}
		
		return ResponseEntity.ok(response);
	}

	//購入手続きへ進むボタン押下
	@PostMapping(path = "/cart/cart", params = "confirm")
	public String confirmCart(Model model, HttpSession session) {
		//ユーザー情報取得
		Users loginUser = (Users) session.getAttribute("user");
		if (loginUser != null) {
			//住所選択、入力画面へ
			return "redirect:/product/address";
		} else {
			// 支払いフラグをセッションに保存
			session.setAttribute("isPayment", true);
			//ログイン画面へ
			return "login/login";
		}
	}

	//住所選択画面
	@GetMapping("/product/address")
	public String showAddressForm(Model model, HttpSession session) {
		//ユーザー情報取得
		Users loginUser = (Users) session.getAttribute("user");
		Integer userId = loginUser.getId();
		model.addAttribute("user", loginUser);

		//既存住所を取得
		List<Address> existing = purchaseService.getAddress(userId);
		model.addAttribute("addressList", existing);
		return "/product/address";
	}

	//支払い選択ボタン押下-住所選択処理-
	@PostMapping(path = "/product/address", params = "address")
	public String submitAddress(@ModelAttribute AddressForm addressForm, @RequestParam String address_option,
			HttpSession session) {
		//既存時の支払先保存
		Integer addressOptionInt = Integer.parseInt(address_option);
		session.setAttribute("useAddress", addressOptionInt);
		//ユーザー情報取得
		Users loginUser = (Users) session.getAttribute("user");
		Integer userId = loginUser.getId();
		//新規住所登録
		if (addressOptionInt == 0) {
			Integer addressId = purchaseService.insertAddress(addressForm, userId);
			session.setAttribute("useAddress", addressId);
		}
		return "redirect:/product/payment";
	}

	//支払い選択画面表示
	@GetMapping("/product/payment")
	public String showPayment(Model model, HttpSession session) {
		//ユーザー情報取得
		Users loginUser = (Users) session.getAttribute("user");
		model.addAttribute("user", loginUser);

		return "/product/payment";
	}

	//次へボタン押下-支払い選択処理-
	@PostMapping(path = "/product/payment", params = "pay")
	public String choicePay(Model model, HttpSession session, @RequestParam String payment_method) {
		Integer payment = Integer.parseInt(payment_method);
		//支払方法をセッションに保存
		session.setAttribute("pay", payment);

		return "redirect:/product/index";
	}

	//確認画面表示
	@GetMapping("/product/index")
	public String showConfirmation(Model model, HttpSession session) {
		//ユーザー情報取得
		Users loginUser = (Users) session.getAttribute("user");
		model.addAttribute("user", loginUser);

		return "/product/index";
	}

}
