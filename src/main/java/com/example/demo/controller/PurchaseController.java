package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Users;
import com.example.demo.service.GuestService;
import com.example.demo.service.UsersService;

import jakarta.servlet.http.HttpSession;

@Controller
public class PurchaseController {
	private final UsersService usersService;
	private final GuestService guestService;

	public PurchaseController(UsersService usersService, GuestService guestService) {
		this.usersService = usersService;
		this.guestService = guestService;
	}

	//カート画面表示
	@GetMapping("/cart/cart")
	public String showCart(Model model, HttpSession session) {
		List<Cart> cartList = new ArrayList<>();
		Users loginUser = (Users) session.getAttribute("user");

        // ゲストユーザーとログインユーザーの処理を分ける
        if (loginUser != null) {
            cartList = usersService.getCart(session);
            model.addAttribute("user", loginUser);
        } else {
            cartList = guestService.getCart(session);
        }
		model.addAttribute("cartList", cartList);
		model.addAttribute("isCartPage", true);
		return "cart/cart";
	}
	
	//削除ボタン押下
	@PostMapping(path="/cart/cart")
	public String deleatCart(Model model,HttpSession session,@RequestParam("productId") Integer productId) {
		List<Cart> cartList = new ArrayList<>();
		Users loginUser = (Users) session.getAttribute("user");
		System.out.println("商品ID"+productId);
		
		// ゲストユーザーとログインユーザーの処理を分ける
        if (loginUser != null) {
        	cartList = usersService.deleteItem(productId,session);
            model.addAttribute("user", loginUser);
        } else {
        	cartList = guestService.deleteItem(productId,session);
        }
		model.addAttribute("cartList", cartList);
		model.addAttribute("isCartPage", true);		
		return "cart/cart";
	}
	
	
	

}
