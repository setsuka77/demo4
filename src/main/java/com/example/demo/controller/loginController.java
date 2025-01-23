package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entity.Users;
import com.example.demo.form.UserForm;
import com.example.demo.mapper.UsersMapper;
import com.example.demo.service.UsersService;

import jakarta.servlet.http.HttpSession;

@Controller
public class loginController {

	private final UsersMapper usersMapper;
	private final UsersService usersService;

	public loginController(UsersMapper usersMapper, UsersService usersService) {
		this.usersService = usersService;
		this.usersMapper = usersMapper;
	}

	/**
	 * ログイン画面　初期
	 * @return
	 */
	@GetMapping("/login/login")
	public String showLoginPage(Model model) {
		model.addAttribute("isLogin", true);
		return "login/login";
	}

	/**
	 * ログインボタン押下時
	 */
	@PostMapping(path = "/login/login", params = "login")
	public String login(HttpSession session, UserForm userForm, Model model) {
		Integer id = userForm.getId();
		String pass = userForm.getPass();
		Users user = usersMapper.findUser(id);

		if (user != null && user.getPass().equals(pass)) {
			// ログイン成功
			session.setAttribute("user", user);
			// セッションからカート情報を取得して保存
			Integer totalItems = usersService.saveCart(id, session);
			session.setAttribute("totalItems", totalItems);

			// 支払いフラグがセットされていれば住所選択画面に遷移
			Boolean isPayment = (Boolean) session.getAttribute("isPayment");
			if (isPayment != null && isPayment) {
				session.removeAttribute("isPayment"); // フラグを削除
				return "redirect:/product/address"; // 住所選択画面へ
			}

			// それ以外の一般的な遷移先
			return "redirect:/";
		} else {
			model.addAttribute("error", "ユーザーID、パスワードが間違ってます");
			return "login/login"; // ログイン画面に戻る
		}
	}

	/**
	 * アカウント作成リンク押下
	 */
	@GetMapping("/login/record")
	public String showUserRecord(Model model) {
		model.addAttribute("isLogin", true);
		return "login/record";
	}

	/**
	 * 登録ボタン押下
	 * @param session
	 * @param userForm
	 * @param model
	 * @return
	 */
	@Transactional
	@PostMapping(path = "/login/record", params = "record")
	public String userRecord(HttpSession session, UserForm userForm, Model model) {
		Integer id = userForm.getId();
		Users user = usersMapper.findUser(id);
		if (user != null) {
			model.addAttribute("error", "そのIDはすでに登録されています");
			return "login/record";
		}

		// UserForm を Users エンティティに変換
		Users newUser = new Users();
		newUser.setId(id);
		newUser.setPass(userForm.getPass());
		newUser.setName(userForm.getName());
		newUser.setEmail(userForm.getEmail());

		// データベースに新しいユーザーを挿入
		usersMapper.insert(newUser);

		// セッションに登録したユーザーをセット
		session.setAttribute("user", newUser);

		//セッションからカート情報を取得して保存
		Integer totalItems = usersService.saveCart(id, session);
		session.setAttribute("totalItems", totalItems);

		// 支払いフラグがセットされていれば住所選択画面に遷移
		Boolean isPayment = (Boolean) session.getAttribute("isPayment");
		if (isPayment != null && isPayment) {
			session.removeAttribute("isPayment"); // フラグを削除
			return "redirect:/product/address"; // 住所選択画面へ
		}

		// リダイレクトしてメインメニューに遷移
		return "redirect:/";
	}

}
