package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Categories;
import com.example.demo.entity.Users;
import com.example.demo.mapper.CategoriesMapper;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainMenuController {

	private final CategoriesMapper categoriesMapper;

	public MainMenuController(CategoriesMapper categoriesMapper) {
		this.categoriesMapper=categoriesMapper;
	}

	/**
	 * ホームの初期画面
	 * @param model
	 * @return　ホーム画面
	 */
	@GetMapping("/")
	public String showMenu(Model model, HttpSession session) {
		// ユーザー情報の取得
		Users loginUser = (Users) session.getAttribute("user");
		//商品カテゴリー一覧
		List<Categories> cate = categoriesMapper.findCategory();
		model.addAttribute("categories",cate);
		
		if (loginUser != null) {
			model.addAttribute("user", loginUser);
		}
		Integer totalItems = (Integer) session.getAttribute("totalItems");
		if (totalItems == null) {
		    totalItems = 0; // null の場合は 0 に初期化
		}
		model.addAttribute("totalItems", totalItems);
		// カテゴリーのセッションをリセット
		session.removeAttribute("goodsForm");
		return "mainMenu/index";
	}
	
	
	@PostMapping("/resetSession")
    public String resetSession(HttpSession session, RedirectAttributes redirectAttributes) {
        // セッションを無効化してリセット
        session.invalidate();
        // セッションリセット後にリダイレクトするページ
        return "redirect:/";  // 例えばトップページにリダイレクト
    }
	
}
