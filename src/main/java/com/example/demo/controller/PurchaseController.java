package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.OrderSummary;
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

	
	@GetMapping("/cart/cart")
	public String showCart(Model model, HttpSession session) {
		List<OrderSummary> orderSummaryList = new ArrayList<>();
		Users loginUser = (Users) session.getAttribute("user");

        // ゲストユーザーとログインユーザーの処理を分ける
        if (loginUser != null) {
            orderSummaryList = usersService.getCart(session);
            model.addAttribute("user", loginUser);
        } else {
            orderSummaryList = guestService.getCart(session);
        }
		System.out.println(orderSummaryList);
		model.addAttribute("orderSummaryList", orderSummaryList);
		model.addAttribute("isCartPage", true);
		return "cart/cart";
	}

}
